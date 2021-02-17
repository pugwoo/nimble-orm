package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;
import com.pugwoo.dbhelper.utils.PreHandleObject;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class P2_InsertOp extends P1_QueryOp {
	
	//////// 拦截器 BEGIN
	private void doInterceptBeforeInsert(Object t) {
		List<Object> list = new ArrayList<Object>();
		list.add(t);
		doInterceptBeforeInsertList(list);
	}
	private void doInterceptBeforeInsertList(List<Object> list) {
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
		doInterceptAfterInsertList(list, rows);
	}
	private void doInterceptAfterInsertList(final List<Object> list, final int rows) {
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
	/////// 拦截器 END

	@Override
	public <T> int insert(T t) {
		return insert(t, false, true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int insert(List<?> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		
		doInterceptBeforeInsertList((List<Object>) list);
		
		int sum = 0;
		for(Object obj : list) {
			sum += insert(obj, false, false);
		}
		
		doInterceptAfterInsertList((List<Object>)list, sum);
		return sum;
	}
	
	@Override
	public <T> int insertWithNull(T t) {
		return insert(t, true, true);
	}
	
	private <T> int insert(T t, boolean isWithNullValue, boolean withInterceptor) {
		PreHandleObject.preHandleInsert(t);
		
		final List<Object> values = new ArrayList<Object>();
		
		if(withInterceptor) {
			doInterceptBeforeInsert(t);
		}
		
		final String sql = SQLUtils.getInsertSQL(t, values, isWithNullValue);
		log(sql);
		
		final long start = System.currentTimeMillis();

		int rows = 0;
		Field autoIncrementField = DOInfoReader.getAutoIncrementField(t.getClass());
		if (autoIncrementField != null) {
			GeneratedKeyHolder holder = new GeneratedKeyHolder();
			rows = jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					for (int i = 0; i < values.size(); i++) {
						statement.setObject(i + 1, values.get(i));
					}
					return statement;
				}
			}, holder);

			if(rows > 0) {
				long primaryKey = holder.getKey().longValue();
				DOInfoReader.setValue(autoIncrementField, t, primaryKey);
			}

		} else {
			rows = jdbcTemplate.update(sql, values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		}

		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, values);
		
		if(withInterceptor) {
			doInterceptAfterInsert(t, rows);
		}
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
			return insert(t, isWithNullValue, true);
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

		int rows;
		Field autoIncrementField = DOInfoReader.getAutoIncrementField(t.getClass());
		if (autoIncrementField != null) {
			rows = namedJdbcExecuteUpdateWithReturnId(autoIncrementField, t, sql, values.toArray());
		} else {
			rows = namedJdbcExecuteUpdate(sql, values.toArray());
		}

		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, values);
		
		doInterceptAfterInsert(t, rows);
		return rows;
	}

	private int namedJdbcExecuteUpdateWithReturnId(Field autoIncrementField, Object t, String sql, Object... args) {
		log(sql);
		long start = System.currentTimeMillis();
		List<Object> argsList = new ArrayList<Object>(); // 不要直接用Arrays.asList，它不支持clear方法
		if(args != null) {
			argsList.addAll(Arrays.asList(args));
		}

		KeyHolder keyHolder = new GeneratedKeyHolder();

		int rows = namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql, argsList),
				new MapSqlParameterSource(NamedParameterUtils.transParam(argsList)),
				keyHolder); // 因为有in (?) 所以使用namedParameterJdbcTemplate

		if(rows > 0) {
			long primaryKey = keyHolder.getKey().longValue();
			DOInfoReader.setValue(autoIncrementField, t, primaryKey);
		}

		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, argsList);
		return rows;
	}

}
