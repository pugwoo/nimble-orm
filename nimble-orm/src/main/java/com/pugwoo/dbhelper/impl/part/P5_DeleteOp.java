package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NotAllowModifyException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.exception.SpringBeanNotMatchException;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.sql.WhereSQL;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.PreHandleObject;

import java.lang.reflect.Field;
import java.util.*;

public abstract class P5_DeleteOp extends P4_InsertOrUpdateOp {

	/////// 拦截器

	private <T> void doInterceptBeforeDelete(T t) {
		if (InnerCommonUtils.isEmpty(interceptors)) {
			return;
		}
		List<Object> tList = new ArrayList<>();
		tList.add(t);
		doInterceptBeforeDeleteList(tList);
	}

	private void doInterceptBeforeDeleteList(List<Object> tList) {
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
		doInterceptAfterDeleteList(tList, rows);
	}

	private void doInterceptAfterDeleteList(final List<Object> tList, final int rows) {
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
			sql = SQLUtils.getDeleteSQL(getDatabaseType(), t, values);
		} else { // 软删除
			// 对于软删除，当有拦截器时，可能使用者会修改数据以记录删除时间或删除人信息等，此时要先update该条数据
			if(InnerCommonUtils.isNotEmpty(interceptors)) {
				updateForDelete(t);
			}
			Column softDeleteColumn = softDelete.getAnnotation(Column.class);
			sql = SQLUtils.getSoftDeleteSQL(getDatabaseType(), t, softDeleteColumn, values);
		}

		Table table = DOInfoReader.getTable(t.getClass());
		if (!isHard && InnerCommonUtils.isNotBlank(table.softDeleteTable())) {
			// 将删除的数据存到另外一张表中
			DBHelper dbHelper = getDBHelper(table.softDeleteDBHelperBean());

			// 查回数据并插入到软删除表
			List<Object> keyParams = new ArrayList<>();
			String keysWhereSQL = SQLUtils.getKeysWhereSQL(getDatabaseType(), t, keyParams);
			Object dbT = getOne(t.getClass(), keysWhereSQL, keyParams.toArray());
			List<Object> all = new ArrayList<>();
			all.add(dbT);
			doInsertToDelTable(all, dbHelper, t.getClass(), table.softDeleteTable(), "");
		}

		int rows = namedJdbcExecuteUpdate(sql, values.toArray());

		doInterceptAfterDelete(t, rows);
		return rows;
	}

	private DBHelper getDBHelper(String dbHelperBean) {
		DBHelper dbHelper = this;
		if (InnerCommonUtils.isNotBlank(dbHelperBean)) {
			Object bean = applicationContext.getBean(dbHelperBean);
			if (!(bean instanceof DBHelper)) {
				throw new SpringBeanNotMatchException("cannot find DBHelper bean: " + dbHelperBean
						+ " or it is not type of SpringJdbcDBHelper");
			} else {
				dbHelper = (DBHelper) bean;
			}
		}
		return dbHelper;
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

		boolean batchDelete = false; // 当所有list都是一个类型，且key是1个
		Field keyField = null;
		if(SQLAssert.isAllSameClass(list)) {
			List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);
			if(keyFields.size() == 1) {
				keyField = keyFields.get(0);
				batchDelete = true;
			}
		}

		if(batchDelete) {
			PreHandleObject.preHandleDeleteList(list);

			if (isUseDeleteValueScript(clazz)) {
				try {
					_update(list, false, false);
				} catch (NoKeyColumnAnnotationException e) {
					LOGGER.error("update data before delete fail, please make sure that class {} have a key column, data:{}",
							clazz, NimbleOrmJSON.toJsonNoException(list), e);
				} catch (Exception e1) {
					LOGGER.error("update data before delete fail, data:{}", NimbleOrmJSON.toJsonNoException(list), e1);
				}
			}

			List<Object> listTmp = new ArrayList<>(list);
			doInterceptBeforeDeleteList(listTmp);

			List<Object> keys = new ArrayList<>();
			for(T t : list) {
				Object key = DOInfoReader.getValue(keyField, t);
				if(key != null) {
					keys.add(key);
				}
			}
			Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除

			String sql;
			String where = SQLUtils.getDeleteWhereSqlByKeyField(getDatabaseType(), keyField);
			if(isHard || softDelete == null) { // 物理删除
				sql = SQLUtils.getCustomDeleteSQL(getDatabaseType(), clazz, where);
			} else { // 软删除
				sql = SQLUtils.getCustomSoftDeleteSQL(getDatabaseType(), clazz, where, softDelete);
			}

			Table table = DOInfoReader.getTable(clazz);
			if (!isHard && InnerCommonUtils.isNotBlank(table.softDeleteTable())) {
				// 查回数据
				List<?> all = getAll(clazz, where, keys);
				// 将删除的数据存到另外一张表中
				DBHelper dbHelper = getDBHelper(table.softDeleteDBHelperBean());
				doInsertToDelTable(all, dbHelper, clazz, table.softDeleteTable(), "");
			}

			int rows = namedJdbcExecuteUpdate(sql, keys);

			doInterceptAfterDeleteList(listTmp, rows);

			// 对于clickhouse,批量方式无法获取修改条数，只能把1转成全部数量
			if (getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
				if (rows > 0) {
					rows = list.size();
				}
			}

			return rows;
		} else {
			int rows = 0;
			for(T t : list) {
				rows += delete(t); // delete(t)中已经做了preHandleDelete和软删除到另外一张表
			}
			return rows;
		}
	}

	@Override
	public <T> int deleteHard(Class<T> clazz, String postSql, Object... args) {
		return _delete(clazz, true, postSql, args);
	}

	@Override
	public <T> int deleteHard(Class<T> clazz, WhereSQL whereSQL) {
		return whereSQL == null ? deleteHard(clazz, "") : deleteHard(clazz, whereSQL.getSQL(), whereSQL.getParams());
	}

	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		return _delete(clazz, false, postSql, args);
	}

	@Override
	public <T> int delete(Class<T> clazz, WhereSQL whereSQL) {
		return whereSQL == null ? delete(clazz, "") : delete(clazz, whereSQL.getSQL(), whereSQL.getParams());
	}

	@SuppressWarnings("unchecked")
	private <T> int _delete(Class<T> clazz, boolean isHard, String postSql, Object... args) {
		if(InnerCommonUtils.isBlank(postSql)) { // warning: very dangerous
			// 不支持缺省条件来删除。如果需要全表删除，请明确传入where 1=1
			throw new InvalidParameterException("delete postSql is blank. it's very dangerous. use `WHERE 1=1` to confirm delete all");
		}

		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除
		String sql;

		List<T> all = null;

		if (isUseDeleteValueScript(clazz)) {
			all = super.getAll(clazz, postSql, args);
			PreHandleObject.preHandleDeleteList(all);
			try {
				_update(all, false, false);
			} catch (NoKeyColumnAnnotationException e) {
				LOGGER.error("update data before delete fail, please make sure that class {} have a key column, data:{}",
						clazz, NimbleOrmJSON.toJsonNoException(all), e);
			} catch (Exception e1) {
				LOGGER.error("update data before delete fail, data:{}", NimbleOrmJSON.toJsonNoException(all), e1);
			}
		}

		if (interceptors != null && !interceptors.isEmpty()) {
			if (all == null) {
				all = getAll(clazz, postSql, args);
			}
			doInterceptBeforeDeleteList((List<Object>) all);
		}

		Table table = DOInfoReader.getTable(clazz);
		if (!isHard && InnerCommonUtils.isNotBlank(table.softDeleteTable())) {
			// 查回数据
			if (all == null) {
				all = getAll(clazz, postSql, args);
			}
			// 将删除的数据存到另外一张表中
			DBHelper dbHelper = getDBHelper(table.softDeleteDBHelperBean());
			doInsertToDelTable(all, dbHelper, clazz, table.softDeleteTable(), postSql, args);
		}

		if(isHard || softDelete == null) { // 物理删除
			sql = SQLUtils.getCustomDeleteSQL(getDatabaseType(), clazz, postSql);
		} else { // 软删除
			sql = SQLUtils.getCustomSoftDeleteSQL(getDatabaseType(), clazz, postSql, softDelete);
		}
		int rows = namedJdbcExecuteUpdate(sql, args);

		if (interceptors != null && !interceptors.isEmpty()) {
			doInterceptAfterDeleteList((List<Object>)all, rows);
		}
		return rows;
	}

	private void doInsertToDelTable(List<?> all, DBHelper dbHelper,
									Class<?> clazz, String softDeleteTableName,
									String postSql, Object... args) {
		try {
			if (all == null || all.isEmpty()) {
                LOGGER.error("soft delete insert to table:{} error, data is null, postSql:{}, args:{}",
						softDeleteTableName, postSql, NimbleOrmJSON.toJsonNoException(args));
			} else {
				Map<Class<?>, String> tableNames = new HashMap<>();
				tableNames.put(clazz, softDeleteTableName);
				DBHelper.withTableNames(tableNames, () -> {
					int rows = dbHelper.insert(all);
					if (rows != all.size()) {
                        LOGGER.error("soft delete insert to table:{} error, rows={}, data: {}",
								softDeleteTableName, rows, NimbleOrmJSON.toJsonNoException(all));
					}
				});
			}
		} catch (Exception e) {
            LOGGER.error("soft delete insert to table:{} error, data: {}",
					softDeleteTableName, NimbleOrmJSON.toJsonNoException(all), e);
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
		String sql = SQLUtils.getUpdateSQL(getDatabaseType(), t, values, false, "");
		if (sql != null) {
			namedJdbcExecuteUpdate(sql, values.toArray()); // ignore update result
		}
	}
}
