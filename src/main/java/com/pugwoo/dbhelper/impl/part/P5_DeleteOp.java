package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.MustProvideconstructorException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;

public abstract class P5_DeleteOp extends P4_InsertOrUpdateOp {

	@Override
	public <T> int deleteByKey(T t) throws NullKeyValueException {
		Field softDelete = DOInfoReader.getSoftDeleteColumn(t.getClass());
		
		List<Object> values = new ArrayList<Object>();
		String sql = null;
		
		if(softDelete == null) { // 物理删除
			sql = SQLUtils.getDeleteSQL(t, values);
		} else { // 软删除
			Column softDeleteColumn = DOInfoReader.getColumnInfo(softDelete);
			sql = SQLUtils.getSoftDeleteSQL(t, softDeleteColumn, values);
		}

		return jdbcExecuteUpdate(sql, values.toArray());
	}
		
	@Override
	public <T> int deleteByKey(Class<T> clazz, Object keyValue) 
			throws NullKeyValueException, MustProvideconstructorException {
		if(keyValue == null) {
			throw new NullKeyValueException();
		}

		Field keyField = DOInfoReader.getOneKeyColumn(clazz);
		
		try {
			T t = (T) clazz.newInstance();
			DOInfoReader.setValue(keyField, t, keyValue);
			return deleteByKey(t);
		} catch (InstantiationException e) {
			throw new MustProvideconstructorException();
		} catch (IllegalAccessException e) {
			throw new MustProvideconstructorException();
		}
	}
	
	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		if(postSql == null || postSql.trim().isEmpty()) { // warning: very dangerous
			// 不支持缺省条件来删除。如果需要全表删除，请直接运维人员truncate表。
			throw new InvalidParameterException(); 
		}
		
		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除

		String sql = null;
		if(softDelete == null) { // 物理删除
			sql = SQLUtils.getCustomDeleteSQL(clazz, postSql);
		} else { // 软删除
			sql = SQLUtils.getCustomSoftDeleteSQL(clazz, postSql);
		}

		return namedJdbcExecuteUpdate(sql, args);
	}
	
}
