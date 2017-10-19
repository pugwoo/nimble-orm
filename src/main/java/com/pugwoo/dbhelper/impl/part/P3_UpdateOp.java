package com.pugwoo.dbhelper.impl.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.PreHandleObject;

public abstract class P3_UpdateOp extends P2_InsertOp {
	
	/////////////// 拦截器
	private <T> void doInterceptBeforeUpdate(Class<?> clazz, Object t) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeUpdate(clazz, t);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	private void doInterceptBeforeUpdate(Class<?> clazz, String sql, Object[] args) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeUpdateCustom(clazz, sql, args);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	private <T> void doInterceptAfterUpdate(Class<?> clazz, Object t, int rows) {
		for (int i = interceptors.size() - 1; i >= 0; i--) {
			interceptors.get(i).afterUpdate(clazz, t, rows);
		}
	}
	private void doInterceptAfterUpdate(Class<?> clazz, String sql, Object[] args, int rows) {
		for (int i = interceptors.size() - 1; i >= 0; i--) {
			interceptors.get(i).afterUpdateCustom(clazz, sql, args, rows);
		}
	}
	//////////////

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
		
		doInterceptBeforeUpdate(t.getClass(), t);
		List<Object> values = new ArrayList<Object>();
		String sql = SQLUtils.getUpdateSQL(t, values, withNull, postSql);
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		
		int rows = namedJdbcExecuteUpdate(sql, values.toArray());
		doInterceptAfterUpdate(t.getClass(), t, rows);
		
		return rows;
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
		String sql = SQLUtils.getCustomUpdateSQL(t, values, setSql); // 这里values里面的内容会在方法内增加
		
		doInterceptBeforeUpdate(t.getClass(), sql, values.toArray());
		int rows = jdbcExecuteUpdate(sql, values.toArray()); // 不会有in(?)表达式
		doInterceptAfterUpdate(t.getClass(), sql, values.toArray(), rows);
		
		return rows;
	}
	
	@Override
	public <T> int updateAll(Class<T> clazz, String setSql, String whereSql, Object... args) {
		if(setSql == null || setSql.trim().isEmpty()) {
			return 0; // 不需要更新
		}
		
		String sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql);
		
		doInterceptBeforeUpdate(clazz, sql, args);
		int rows = namedJdbcExecuteUpdate(sql, args);
		doInterceptAfterUpdate(clazz, sql, args, rows);
		
		return rows;
	}
	
}
