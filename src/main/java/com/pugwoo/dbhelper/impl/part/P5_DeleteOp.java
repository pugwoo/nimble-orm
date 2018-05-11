package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.MustProvideConstructorException;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;

public abstract class P5_DeleteOp extends P4_InsertOrUpdateOp {
	
	/////// 拦截器
	protected <T> void doInterceptBeforeDelete(Class<?> clazz, List<T> tList) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeDelete(clazz, tList);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}

	protected <T> void doInterceptAfterDelete(final Class<?> clazz, final List<T> tList, final int rows) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = interceptors.size() - 1; i >= 0; i--) {
					interceptors.get(i).afterDelete(clazz, tList, rows);
				}
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}

	///////////

	@Override
	public <T> int deleteByKey(T t) throws NullKeyValueException {
		Field softDelete = DOInfoReader.getSoftDeleteColumn(t.getClass());
		
		List<Object> values = new ArrayList<Object>();

		List<T> tList = new ArrayList<T>();
		tList.add(t);
		doInterceptBeforeDelete(t.getClass(), tList);
		
		String sql = null;
		
		if(softDelete == null) { // 物理删除
			sql = SQLUtils.getDeleteSQL(t, values);
		} else { // 软删除
			// 对于软删除，当有拦截器时，可能使用者会修改数据以记录删除时间或删除人信息等，此时要先update该条数据
			if(interceptors != null && !interceptors.isEmpty()) {
				update(t);
			}
			Column softDeleteColumn = softDelete.getAnnotation(Column.class);
			sql = SQLUtils.getSoftDeleteSQL(t, softDeleteColumn, values);
		}
		
		int rows = jdbcExecuteUpdate(sql, values.toArray());

		doInterceptAfterDelete(t.getClass(), tList, rows);
		
		return rows;
	}
	
	@Override @Transactional
	public <T> int deleteByKey(List<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) return 0;
		
		boolean batchDelete = false;
		Field keyField = null;
		if(SQLAssert.isAllSameClass(list)) {
			List<Field> keyFields = DOInfoReader.getKeyColumns(list.get(0).getClass());
			if(keyFields.size() == 1) {
				keyField = keyFields.get(0);
				batchDelete = true;
			}
		}
		
		if(batchDelete) {
			Class<?> clazz = list.get(0).getClass();
			List<Object> keys = new ArrayList<Object>();
			for(T t : list) {
				Object key = DOInfoReader.getValue(keyField, t);
				if(key != null) keys.add(key);
			}
			doInterceptBeforeDelete(clazz, list);
			
			Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除
			
			String sql = null;
			String where = "where `" + keyField.getAnnotation(Column.class).value() + "` in (?)";
			if(softDelete == null) { // 物理删除
				sql = SQLUtils.getCustomDeleteSQL(clazz, where);
			} else { // 软删除
				sql = SQLUtils.getCustomSoftDeleteSQL(clazz, where);
			}
			
			int rows = namedJdbcExecuteUpdate(sql, keys);
			
			doInterceptAfterDelete(clazz, list, rows);
			return rows;
		} else {
			int rows = 0;
			for(T t : list) {
				rows += deleteByKey(t);
			}
			return rows;
		}
	}
		
	@Override
	public <T> int deleteByKey(Class<T> clazz, Object keyValue) 
			throws NullKeyValueException, MustProvideConstructorException {
		if(keyValue == null) {
			throw new NullKeyValueException();
		}

		Field keyField = DOInfoReader.getOneKeyColumn(clazz);
		
		try {
			T t = (T) clazz.newInstance();
			DOInfoReader.setValue(keyField, t, keyValue);
			return deleteByKey(t);
		} catch (InstantiationException e) {
			throw new MustProvideConstructorException();
		} catch (IllegalAccessException e) {
			throw new MustProvideConstructorException();
		}
	}
	
	@Override @Transactional
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		if(postSql == null || postSql.trim().isEmpty()) { // warning: very dangerous
			// 不支持缺省条件来删除。如果需要全表删除，请直接运维人员truncate表。
			throw new InvalidParameterException("delete postSql is blank. it's very dangerous"); 
		}

		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除
		String sql = null;
		
		if(interceptors == null || interceptors.isEmpty()) { // 没有配置拦截器，则直接删除
			if(softDelete == null) { // 物理删除
				sql = SQLUtils.getCustomDeleteSQL(clazz, postSql);
			} else { // 软删除
				sql = SQLUtils.getCustomSoftDeleteSQL(clazz, postSql);
			}
			return namedJdbcExecuteUpdate(sql, args);
		} else { // 配置了拦截器，则先查出key，再删除
			List<T> allKey = getAllKey(clazz, postSql, args);
			return deleteByKey(allKey);
		}
	}
	
}
