package com.pugwoo.dbhelper.impl.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.PreHandleObject;

public abstract class P3_UpdateOp extends P2_InsertOp {

	@Override
	public <T> int update(T t) throws NullKeyValueException {
		return _update(t, false, null);
	}
	
	@Override
	public <T> int update(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, false, postSql, args);
	}
	
	@Override
	public <T> int updateWithNull(T t) throws NullKeyValueException {
		return _update(t, true, null);
	}
	
	@Override
	public <T> int updateWithNull(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, true, postSql, args);
	}
	
	@Override @Transactional
	public <T> int updateWithNull(List<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += updateWithNull(t);
			}
		}
		return rows;
	}
	
	@Override @Transactional
	public <T> int update(List<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += update(t);
			}
		}
		return rows;
	}
	
	private <T> int _update(T t, boolean withNull, String postSql, Object... args) 
			throws NullKeyValueException {
		
		if(DOInfoReader.getNotKeyColumns(t.getClass()).isEmpty()) {
			return 0; // not need to update
		}
		
		PreHandleObject.preHandleUpdate(t);
		
		List<Object> values = new ArrayList<Object>();
		String sql = SQLUtils.getUpdateSQL(t, values, withNull, postSql);
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		
		return namedJdbcExecuteUpdate(sql, values.toArray());
	}
	
	@Override
	public <T> int updateCustom(T t, String setSql, Object... args) throws NullKeyValueException {
		if(setSql == null || setSql.trim().isEmpty()) {
			return 0; // 不需要更新
		}
		
		List<Object> values = new ArrayList<Object>();
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		
		String sql = SQLUtils.getCustomUpdateSQL(t, values, setSql);
		
		return jdbcExecuteUpdate(sql, values.toArray()); // 不会有in(?)表达式
	}
	
	@Override
	public <T> int updateAll(Class<T> clazz, String setSql, String whereSql, Object... args) {
		if(setSql == null || setSql.trim().isEmpty()) {
			return 0; // 不需要更新
		}
		
		String sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql);
		return namedJdbcExecuteUpdate(sql, args);
	}
	
}
