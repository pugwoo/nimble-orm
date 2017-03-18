package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.PreHandleObject;

public abstract class P2_InsertOp extends P1_QueryOp {

	@Override
	public <T> int insert(T t) {
		return insert(t, false);
	}
	
	@Override @Transactional
	public int insert(List<?> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int sum = 0;
		for(Object obj : list) {
			sum += insert(obj, false);
		}
		return sum;
	}
	
	@Override
	public <T> int insertWithNull(T t) {
		return insert(t, true);
	}
	
	private <T> int insert(T t, boolean isWithNullValue) {
		PreHandleObject.preHandleInsert(t);
		
		List<Object> values = new ArrayList<Object>();
		String sql = SQLUtils.getInsertSQL(t, values, isWithNullValue);
		
		log(sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		Field autoIncrementField = DOInfoReader.getAutoIncrementField(t.getClass());
		if(autoIncrementField != null && rows == 1) {
			Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()",
					Long.class);
			DOInfoReader.setValue(autoIncrementField, t, id);
		}
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, values);
		return rows;
	}
	
	@Override
	public <T> int insertWhereNotExist(T t, String whereSql, Object... args) {
		return insertWhereNotExist(t, false, whereSql, args);
	}
	
	@Override
	public <T> int insertWithNullWhereNotExist(T t, String whereSql, Object... args) {
		return insertWhereNotExist(t, true, whereSql, args);
	}
	
	private <T> int insertWhereNotExist(T t, boolean isWithNullValue, String whereSql, Object... args) {
		if(whereSql == null || whereSql.isEmpty()) {
			return insert(t, isWithNullValue);
		}
		
		PreHandleObject.preHandleInsert(t);
		
		List<Object> values = new ArrayList<Object>();
		String sql = SQLUtils.getInsertWhereNotExistSQL(t, values, isWithNullValue, whereSql);
		
		if(args != null) {
			for(Object arg : args) {
				values.add(arg);
			}
		}
		
		log(sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		Field autoIncrementField = DOInfoReader.getAutoIncrementField(t.getClass());
		if(autoIncrementField != null && rows == 1) {
			Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()",
					Long.class);
			DOInfoReader.setValue(autoIncrementField, t, id);
		}
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, values);
		return rows;
	}
		
	@Override
	public <T> int insertWithNullInOneSQL(List<T> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		list.removeAll(Collections.singleton(null));
		
		SQLAssert.allSameClass(list);
		
		for(T t : list) {
			PreHandleObject.preHandleInsert(t);
		}
		
		List<Object> values = new ArrayList<Object>();
		String sql = SQLUtils.getInsertSQLWithNull(list, values);
				
		return jdbcExecuteUpdate(sql.toString(), values.toArray());
	}
	
	/**
	 * 使用jdbcTemplate模版执行update，不支持in (?)表达式
	 * @param sql
	 * @param args
	 * @return 实际修改的行数
	 */
	private int jdbcExecuteUpdate(String sql, Object... args) {
		log(sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), args);// 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, args);
		return rows;
	}
}
