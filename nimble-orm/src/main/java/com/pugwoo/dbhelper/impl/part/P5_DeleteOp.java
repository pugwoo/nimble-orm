package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NotAllowModifyException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.exception.SpringBeanNotMatchException;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.PreHandleObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class P5_DeleteOp extends P4_InsertOrUpdateOp {

	/////// 拦截器

	private <T> void doInterceptBeforeDelete(T t) {
		if (InnerCommonUtils.isEmpty(interceptors)) {
			return;
		}
		List<Object> tList = new ArrayList<>();
		tList.add(t);
		doInterceptBeforeDelete(tList);
	}

	private void doInterceptBeforeDelete(List<Object> tList) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeDelete(tList);
			if (!isContinue) {
				throw new NotAllowModifyException("interceptor class:" + interceptor.getClass());
			}
		}
	}

	private <T> void doInterceptAfterDelete(final T t, final int rows) {
		if (InnerCommonUtils.isEmpty(interceptors)) {
			return;
		}
		List<Object> tList = new ArrayList<>();
		tList.add(t);
		doInterceptAfterDelete(tList, rows);
	}

	private void doInterceptAfterDelete(final List<Object> tList, final int rows) {
		if (InnerCommonUtils.isEmpty(interceptors)) {
			return; // 内部实现尽量不调用executeAfterCommit
		}
		Runnable runnable = () -> {
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				interceptors.get(i).afterDelete(tList, rows);
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}

	/////////// END 拦截器

	@Override
	public <T> int delete(T t) throws NullKeyValueException {
		return _delete(t, false);
	}

	@Override
	public <T> int deleteHard(T t) throws NullKeyValueException {
		return _delete(t, true);
	}

	private <T> int _delete(T t, boolean isHard) throws NullKeyValueException {
		if (t == null) {
			throw new InvalidParameterException("delete t is null");
		}

		PreHandleObject.preHandleDelete(t);

		doInterceptBeforeDelete(t);

		Field softDelete = DOInfoReader.getSoftDeleteColumn(t.getClass());

		String sql;
		List<Object> values = new ArrayList<>();
		if (isHard || softDelete == null) { // 物理删除
			sql = SQLUtils.getDeleteSQL(t, values);
		} else { // 软删除
			// 对于软删除，当有拦截器时，可能使用者会修改数据以记录删除时间或删除人信息等，此时要先update该条数据
			if(InnerCommonUtils.isNotEmpty(interceptors)) {
				updateForDelete(t);
			}
			Column softDeleteColumn = softDelete.getAnnotation(Column.class);
			sql = SQLUtils.getSoftDeleteSQL(t, softDeleteColumn, values);
		}

		Table table = DOInfoReader.getTable(t.getClass());
		if (!isHard && InnerCommonUtils.isNotBlank(table.softDeleteTable())) { // 将删除的数据存到另外一张表中
			DBHelper dbHelper = this;
			if (InnerCommonUtils.isNotBlank(table.softDeleteDBHelperBean())) {
				Object bean = applicationContext.getBean(table.softDeleteDBHelperBean());
				if (!(bean instanceof DBHelper)) {
					throw new SpringBeanNotMatchException("cannot find DBHelper bean: " + table.softDeleteDBHelperBean()
							+ " or it is not type of SpringJdbcDBHelper");
				} else {
					dbHelper = (DBHelper) bean;
				}
			}

			// 查回数据并插入到软删除表
			List<Object> keyParams = new ArrayList<>();
			String keysWhereSQL = SQLUtils.getKeysWhereSQL(t, keyParams);
			Object dbT = getOne(t.getClass(), keysWhereSQL, keyParams.toArray());
			try {
				if (dbT == null) {
					LOGGER.error("soft delete insert to table:" + table.softDeleteTable() + " error, data is null, key:{}",
							NimbleOrmJSON.toJson(keyParams));
				} else {
					String oldTableNames = DBHelperContext.getTableName(t.getClass());
					DBHelperContext.setTableName(t.getClass(), table.softDeleteTable());
					int rows = dbHelper.insert(dbT);
					DBHelperContext.setTableName(t.getClass(), oldTableNames);

					if (rows != 1) {
						LOGGER.error("soft delete insert to table:" + table.softDeleteTable() + " error, rows={}, data: {}",
								rows, NimbleOrmJSON.toJson(dbT));
					}
				}
			} catch (Exception e) {
				LOGGER.error("soft delete insert to table:" + table.softDeleteTable() + " error, data: {}",
						NimbleOrmJSON.toJson(dbT), e);
			}
		}

		int rows = jdbcExecuteUpdate(sql, values.toArray()); // 不会有in(?)表达式

		doInterceptAfterDelete(t, rows);
		return rows;
	}

	@Override
	public <T> int deleteHard(Collection<T> list) throws NullKeyValueException {
		return _delete(list, true);
	}

	@Override
	public <T> int delete(Collection<T> list) throws NullKeyValueException {
		return _delete(list, false);
	}

	private <T> int _delete(Collection<T> list, boolean isHard) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
			return 0;
		}

		Class<?> clazz = null; // 当batchDelete时使用
		for (T t : list) {
			if (t != null) {
				clazz = t.getClass();
			}
		}

		boolean batchDelete = false; // 当所有list都是一个类型，且key是1个，且没有用到deleteValueScript时
		Field keyField = null;
		if(SQLAssert.isAllSameClass(list)) {
			List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);
			if(keyFields.size() == 1) {
				keyField = keyFields.get(0);

				boolean isUseDeleteValueScript = false;
				List<Field> fields = DOInfoReader.getColumns(clazz);
				for(Field field : fields) {
					Column column = field.getAnnotation(Column.class);
					if(InnerCommonUtils.isNotBlank(column.deleteValueScript())) {
						isUseDeleteValueScript = true;
						break;
					}
				}

				if(!isUseDeleteValueScript) {
					batchDelete = true;
				}
			}
		}

		if(batchDelete) {
			for(T t : list) {
				PreHandleObject.preHandleDelete(t);
			}

			List<Object> keys = new ArrayList<>();
			for(T t : list) {
				Object key = DOInfoReader.getValue(keyField, t);
				if(key != null) {
					keys.add(key);
				}
			}

			List<Object> listTmp = new ArrayList<>(list);
			doInterceptBeforeDelete(listTmp);

			Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除

			String sql;
			String where = "where " + SQLUtils.getColumnName(keyField) + " in (?)";
			if(isHard || softDelete == null) { // 物理删除
				sql = SQLUtils.getCustomDeleteSQL(clazz, where);
			} else { // 软删除
				sql = SQLUtils.getCustomSoftDeleteSQL(clazz, where, softDelete);
			}

			int rows = namedJdbcExecuteUpdate(sql, keys);

			doInterceptAfterDelete(listTmp, rows);
			return rows;
		} else {
			int rows = 0;
			for(T t : list) {
				rows += delete(t); // deleteByKey中已经做了preHandleDelete
			}
			return rows;
		}
	}

	@Override
	public <T> int deleteHard(Class<T> clazz, String postSql, Object... args) {
		return _delete(clazz, true, postSql, args);
	}

	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		return _delete(clazz, false, postSql, args);
	}

	private <T> int _delete(Class<T> clazz, boolean isHard, String postSql, Object... args) {
		if(InnerCommonUtils.isBlank(postSql)) { // warning: very dangerous
			// 不支持缺省条件来删除。如果需要全表删除，请明确传入where 1=1
			throw new InvalidParameterException("delete postSql is blank. it's very dangerous. use WHERE 1=1 to confirm delete all");
		}

		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除
		String sql;

		if((interceptors == null || interceptors.isEmpty()) && !isUseDeleteValueScript(clazz)) { // 没有配置拦截器，则直接删除
			if(isHard || softDelete == null) { // 物理删除
				sql = SQLUtils.getCustomDeleteSQL(clazz, postSql);
			} else { // 软删除
				sql = SQLUtils.getCustomSoftDeleteSQL(clazz, postSql, softDelete);
			}
			return namedJdbcExecuteUpdate(sql, args);
		} else { // 配置了拦截器，则先查出key，再删除
			List<T> allKey = getAllKey(clazz, postSql, args);
			return delete(allKey);
		}
	}

	private static boolean isUseDeleteValueScript(Class<?> clazz) {
		List<Field> columns = DOInfoReader.getColumns(clazz);
		for (Field field : columns) {
			if (InnerCommonUtils.isNotBlank(field.getAnnotation(Column.class).deleteValueScript())) {
				return true;
			}
		}
		return false;
	}

	private <T> void updateForDelete(T t) throws NullKeyValueException {
		List<Object> values = new ArrayList<>();
		String sql = SQLUtils.getUpdateSQL(t, values, false, null);
		if (sql != null) {
			// 没有in (?)，因此用jdbcExecuteUpdate
			jdbcExecuteUpdate(sql, values.toArray()); // ignore update result
		}
	}
}
