package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.PreHandleObject;

public abstract class P2_InsertOp extends P1_QueryOp {
	
	//////// 拦截器
	private void doInterceptBeforeInsert(Object t) {
		List<Object> list = new ArrayList<Object>();
		list.add(t);
		doInterceptBeforeInsert(list);
	}
	private <T> void doInterceptBeforeInsert(List<T> list) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeInsert(list);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	private void doInterceptAfterInsert(Object t, int rows) {
		List<Object> list = new ArrayList<Object>();
		list.add(t);
		doInterceptAfterInsert(list, rows);
	}
	private <T> void doInterceptAfterInsert(final List<T> list, final int rows) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = interceptors.size() - 1; i >= 0; i--) {
					interceptors.get(i).afterInsert(list, rows);
				}
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}
	////////////

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
		
		doInterceptBeforeInsert(t);
		
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
		
		doInterceptAfterInsert(t, rows);
		return rows;
	}
	
	@Override
	public <T> int insertWhereNotExist(T t, String whereSql, Object... args) {
		if(whereSql != null) {whereSql = whereSql.replace('\t', ' ');}
		return insertWhereNotExist(t, false, whereSql, args);
	}
	
	@Override
	public <T> int insertWithNullWhereNotExist(T t, String whereSql, Object... args) {
		if(whereSql != null) {whereSql = whereSql.replace('\t', ' ');}
		return insertWhereNotExist(t, true, whereSql, args);
	}
	
	private <T> int insertWhereNotExist(T t, boolean isWithNullValue, String whereSql, Object... args) {
		if(whereSql == null || whereSql.isEmpty()) {
			return insert(t, isWithNullValue);
		}
		
		PreHandleObject.preHandleInsert(t);
		
		List<Object> values = new ArrayList<Object>();
		
		doInterceptBeforeInsert(t);
		
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
		
		doInterceptAfterInsert(t, rows);
		return rows;
	}

}
