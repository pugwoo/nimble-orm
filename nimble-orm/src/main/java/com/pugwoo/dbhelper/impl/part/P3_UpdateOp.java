package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.exception.CasVersionNotMatchException;
import com.pugwoo.dbhelper.exception.NotAllowModifyException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.json.NimbleOrmDateUtils;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.sql.WhereSQL;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.PreHandleObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class P3_UpdateOp extends P2_InsertOp {
	
	/////////////// 拦截器

	@SuppressWarnings("unchecked")
	private void doInterceptBeforeUpdate(Collection<?> tList, String setSql, List<Object> setSqlArgs) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue;
			if (tList instanceof List) {
				isContinue = interceptor.beforeUpdate((List<Object>) tList, setSql, setSqlArgs);
			} else {
				isContinue = interceptor.beforeUpdate(new ArrayList<>(tList), setSql, setSqlArgs);
			}

			if (!isContinue) {
				throw new NotAllowModifyException("interceptor class:" + interceptor.getClass());
			}
		}
	}

	private void doInterceptBeforeUpdate(Class<?> clazz, String sql,
			List<String> customsSets, List<Object> customsParams, List<Object> args) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeUpdateAll(clazz, sql, customsSets, customsParams, args);
			if (!isContinue) {
				throw new NotAllowModifyException("interceptor class:" + interceptor.getClass());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void doInterceptAfterUpdate(final Collection<?> tList, final int rows) {
		if (InnerCommonUtils.isEmpty(interceptors)) {
			return; // 内部实现尽量不调用executeAfterCommit
		}
		Runnable runnable = () -> {
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				if (tList instanceof List) {
					interceptors.get(i).afterUpdate((List<Object>) tList, rows);
				} else {
					interceptors.get(i).afterUpdate(new ArrayList<>(tList), rows);
				}
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}

	//////////////

	@Override
	public <T> int update(T t) throws NullKeyValueException {
		return _update(t, false, true, true, "");
	}
	
	@Override
	public <T> int update(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, false, true, true, postSql, args);
	}
	
	@Override
	public <T> int updateWithNull(T t) throws NullKeyValueException {
		return _update(t, true, true, true, "");
	}
	
	@Override
	public <T> int updateWithNull(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, true, true, true, postSql, args);
	}

	@Override
	public <T> int update(Collection<T> list) throws NullKeyValueException {
		return _update(list, true, true);
	}

	protected <T> int _update(Collection<T> list, boolean withInterceptor, boolean preHandleUpdate)
			throws NullKeyValueException {
		list = InnerCommonUtils.filterNonNull(list);
		if (withInterceptor) {
			doInterceptBeforeUpdate(list, null, null);
		}

		list = InnerCommonUtils.filterNonNull(list);
		if (InnerCommonUtils.isEmpty(list)) {
			return 0;
		}
		if (list.size() == 1) {
			return _update(list.iterator().next(), false, withInterceptor, preHandleUpdate, "");
		}

		boolean isSameClass = SQLAssert.isAllSameClass(list);
		Class<?> clazz = list.iterator().next().getClass();

		// pgsql的批量update，如果字段含有isJson字段，则降级为单条update，批量update目前driver会报错
		if (getDatabaseType() == DatabaseTypeEnum.POSTGRESQL && isSameClass) {
			List<Field> columns = DOInfoReader.getColumns(clazz);
			boolean hasJsonColumn = false;
			for (Field field : columns) {
				if (field.getAnnotation(Column.class).isJSON()) {
					hasJsonColumn = true;
					break;
				}
			}
			if (hasJsonColumn) {
				int rows = 0;
				for (T t : list) {
					if (t != null) {
						rows += _update(t, false, withInterceptor, preHandleUpdate, "");
					}
				}
				return rows;
			}
		}

		// 判断是否可以转化成批量update，使用update case when的方式
		// 可以批量update的条件：
		// 1) list里所有对象都是相同的类
		// 2) DO类有主键且只有一个主键(暂不支持多个主键，这种情况太少，以后有时间再支持)
		List<Field> keyColumns = DOInfoReader.getKeyColumns(clazz);

		int rows = 0;

		boolean isUpdateOneByOne = true;
		if (isSameClass && keyColumns.size() == 1) { // 满足批量条件
			try {
				List<Field> notKeyColumns = DOInfoReader.getNotKeyColumns(clazz);
				if(notKeyColumns.isEmpty()) {
					return 0; // not need to update
				}

				if (preHandleUpdate) {
					list.forEach(PreHandleObject::preHandleUpdate);
				}

				// 找到notKeyColumns中的casVersionColumn，并从notKeyColumns中移除
				Field casVersionColumn = null;
				for (Field field : notKeyColumns) {
					if (field.getAnnotation(Column.class).casVersion()) {
						if (casVersionColumn != null) {
							throw new RuntimeException("class:" + clazz.getName() + " has more than one casVersion column");
						} else {
							casVersionColumn = field;
						}
					}
				}
				if (casVersionColumn != null) {
					notKeyColumns.remove(casVersionColumn);
				}

				List<Object> params = new ArrayList<>();
				SQLUtils.BatchUpdateResultDTO batchUpdateSQL = SQLUtils.getBatchUpdateSQL(getDatabaseType(),
						list, params, casVersionColumn, keyColumns.get(0), notKeyColumns, clazz);
				if (InnerCommonUtils.isBlank(batchUpdateSQL.getSql())) {
					return 0; // not need to update, return actually update rows
				}

				// 对于postgresql，对于这种批量update方式，需要把java.util.Date的参数转换成LocalDateTime
				if (getDatabaseType() == DatabaseTypeEnum.POSTGRESQL) {
					for (int i = 0; i < params.size(); i++) {
						if (params.get(i) != null && params.get(i) instanceof java.util.Date) {
							params.set(i, NimbleOrmDateUtils.toLocalDateTime((java.util.Date) params.get(i)));
						}
					}
				}

				rows = namedJdbcExecuteUpdate(batchUpdateSQL.getSql(),
						batchUpdateSQL.getLogSql(), list.size(), batchUpdateSQL.getLogParams(), params.toArray());

				// 对于clickhouse,case when的批量update方式无法获取正在的修改条数，只能把1转成全部数量
				if (getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
					if (rows > 0) {
						rows = list.size();
					}
				}

				postHandleCasVersion(list, rows, casVersionColumn, clazz);
				isUpdateOneByOne = false; // 成功批量更新
			} catch (Exception e) {
				if (e.getMessage().contains("has more than one casVersion column")) {
					throw e;
				}
				LOGGER.warn("batch update error, class:{}, data size:{}, will fallback to update one by one",
						clazz.getName(), list.size(), e);
			}
		}

		if (isUpdateOneByOne) { // fallback to update one by one
			boolean isThrowCasVersionNotMatchException = false;
			List<T> casUpdateFailList = new ArrayList<>();
			for(T t : list) {
				if(t != null) {
					try {
						rows += _update(t, false, withInterceptor, preHandleUpdate, "");
					} catch (CasVersionNotMatchException e) {
						casUpdateFailList.add(t);
						isThrowCasVersionNotMatchException = true;
						// ignore exception log
					}
				}
			}
			if(isThrowCasVersionNotMatchException) {
				throw new CasVersionNotMatchException(rows, "update fail for class:"
						+ clazz.getName() + ", data:" + NimbleOrmJSON.toJsonNoException(casUpdateFailList));
			}
		}

		if (withInterceptor) {
			doInterceptAfterUpdate(list, rows);
		}
		return rows;
	}
	
	private <T> int _update(T t, boolean withNull, boolean withInterceptors, boolean preHandleUpdate,
			String postSql, Object... args) throws NullKeyValueException {
		if (t == null) {
			return 0;
		}

		if(DOInfoReader.getNotKeyColumns(t.getClass()).isEmpty()) {
			return 0; // not need to update
		}

		if (preHandleUpdate) {
			PreHandleObject.preHandleUpdate(t);
		}
		
		List<Object> tList = new ArrayList<>();
		tList.add(t);
		
		if(withInterceptors) {
			doInterceptBeforeUpdate(tList, null, null);
		}
		
		List<Object> values = new ArrayList<>();
		String sql = SQLUtils.getUpdateSQL(getDatabaseType(), t, values, withNull, postSql);
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		
		int rows = namedJdbcExecuteUpdate(sql, values.toArray());

		postHandleCasVersion(t, rows);

		if(withInterceptors) {
			doInterceptAfterUpdate(tList, rows);
		}
		
		return rows;
	}

	private void postHandleCasVersion(Object t, Field casVersionField) {
		Object casVersion = DOInfoReader.getValue(casVersionField, t);
		if(casVersion instanceof Integer) {
			Integer newVersion = ((Integer) casVersion) + 1;
			DOInfoReader.setValue(casVersionField, t, newVersion);
		} else if (casVersion instanceof Long) {
			Long newVersion = ((Long) casVersion) + 1;
			DOInfoReader.setValue(casVersionField, t, newVersion);
		}
		// 其它类型ignore，已经在update之前就断言casVersion必须是Integer或Long类型
	}

	private <T> void postHandleCasVersion(Collection<T> list, int rows, Field casVersionColumn, Class<?> clazz) {
		if (casVersionColumn == null) {
			return; // 没有casVersion列，不处理
		}
		if (list.size() != rows) {
			throw new CasVersionNotMatchException(rows, "update fail for class:"
					+ clazz.getName() + ", data:" + NimbleOrmJSON.toJsonNoException(list));
		} else {
			list.forEach(o -> postHandleCasVersion(o, casVersionColumn));
		}
	}

    /**
     * 后处理casVersion相关内容：
     * 1. 当DO类有注解casVersion但是数据库没有修改时抛出异常。
     * 2. 当DO类有注解casVersion且数据库提交成功时，自动设置casVersion+1
     */
	private void postHandleCasVersion(Object t, int rows) {
        Field casVersionField = DOInfoReader.getCasVersionColumn(t.getClass());
		if (casVersionField == null) {
			return; // 没有casVersion列，不处理
		}
		if(rows <= 0) {
			throw new CasVersionNotMatchException("update fail for class:"
					+ t.getClass().getName() + ", data:" + NimbleOrmJSON.toJsonNoException(t));
		} else {
			postHandleCasVersion(t, casVersionField);
		}
    }
	
	@Override
	public <T> int updateCustom(T t, String setSql, Object... args) throws NullKeyValueException {
		if(InnerCommonUtils.isBlank(setSql)) {
			return 0; // 不需要更新
		}
		
		List<Object> values = new ArrayList<>();
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		String sql = SQLUtils.getCustomUpdateSQL(getDatabaseType(), t, values, setSql); // 这里values里面的内容会在方法内增加
		
		List<Object> tList = new ArrayList<>();
		tList.add(t);
		
		doInterceptBeforeUpdate(tList, setSql, values);

		int rows = namedJdbcExecuteUpdate(sql, values.toArray());

		postHandleCasVersion(t, rows);

		doInterceptAfterUpdate(tList, rows);
		
		return rows;
	}
	
	@Override
	public <T> int updateAll(Class<T> clazz, String setSql, String whereSql, Object... args) {
		if(InnerCommonUtils.isBlank(setSql)) {
			return 0; // 不需要更新
		}
		
		List<Object> values;

		String sql = SQLUtils.getUpdateAllSQL(getDatabaseType(), clazz, setSql, whereSql, "");
		
		List<String> customsSets = new ArrayList<>();
		List<Object> customsParams = new ArrayList<>();
		
		List<Object> argsList = new ArrayList<>();
		if(args != null) {
			argsList.addAll(Arrays.asList(args));
		}
		doInterceptBeforeUpdate(clazz, sql, customsSets, customsParams, argsList);
		
		if(!customsSets.isEmpty()) { // 处理自定义加入set，需要重新生成sql
			values = new ArrayList<>();
			values.addAll(customsParams);
			values.addAll(argsList);
			
			StringBuilder sbSet = new StringBuilder();
			for(String s : customsSets) {
				sbSet.append(s).append(",");
			}
			sbSet.append(setSql.toLowerCase().startsWith("set ") ? setSql.substring(4) : setSql);
			
			setSql = sbSet.toString();
		} else {
			values = new ArrayList<>(argsList);
		}
		
		sql = SQLUtils.getUpdateAllSQL(getDatabaseType(), clazz, setSql, whereSql, "");

		return namedJdbcExecuteUpdate(sql, values.toArray());
	}

	@Override
	public <T> int updateAll(Class<T> clazz, String setSql, WhereSQL whereSQL) {
		return whereSQL == null ? updateAll(clazz, setSql, "")
				: updateAll(clazz, setSql, whereSQL.getSQL(), whereSQL.getParams());
	}
}
