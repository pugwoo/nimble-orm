package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.json.JSON;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.PreHandleObject;

public abstract class P3_UpdateOp extends P2_InsertOp {
	
	/////////////// 拦截器
	private <T> void doInterceptBeforeUpdate(List<Object> tList, String setSql, Object[] setSqlArgs) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeUpdate(tList, setSql, setSqlArgs);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	private void doInterceptBeforeUpdate(Class<?> clazz, String sql,
			List<String> customsSets, List<Object> customsParams, Object[] args) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeUpdateCustom(clazz, sql, customsSets, customsParams, args);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	private <T> void doInterceptAfterUpdate(final List<Object> tList, final int rows) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = interceptors.size() - 1; i >= 0; i--) {
					interceptors.get(i).afterUpdate(tList, rows);
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
		return _update(t, false, null);
	}
	
	@Override
	public <T> int update(T t, String postSql, Object... args) throws NullKeyValueException {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		return _update(t, false, postSql, args);
	}
	
	@Override
	public <T> int updateWithNull(T t) throws NullKeyValueException {
		return _update(t, true, null);
	}
	
	@Override
	public <T> int updateWithNull(T t, String postSql, Object... args) throws NullKeyValueException {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
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
		
		List<Object> tList = new ArrayList<Object>();
		tList.add(t);
		
		doInterceptBeforeUpdate(tList, null, null);
		List<Object> values = new ArrayList<Object>();
		String sql = SQLUtils.getUpdateSQL(t, values, withNull, postSql);
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		
		int rows = namedJdbcExecuteUpdate(sql, values.toArray());
		
		doInterceptAfterUpdate(tList, rows);
		
		return rows;
	}
	
	@Override
	public <T> int updateCustom(T t, String setSql, Object... args) throws NullKeyValueException {
		if(setSql != null) {setSql = setSql.replace('\t', ' ');}
		if(setSql == null || setSql.trim().isEmpty()) {
			return 0; // 不需要更新
		}
		
		List<Object> values = new ArrayList<Object>();
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		String sql = SQLUtils.getCustomUpdateSQL(t, values, setSql); // 这里values里面的内容会在方法内增加
		
		List<String> customsSets = new ArrayList<String>();
		List<Object> customsParams = new ArrayList<Object>();
		
		List<Object> tList = new ArrayList<Object>();
		tList.add(t);
		
		doInterceptBeforeUpdate(tList, setSql, args);
		
		if(!customsSets.isEmpty()) { // 处理自定义加入set，需要重新生成sql
			values = new ArrayList<Object>();
			if(args != null) {
				values.addAll(Arrays.asList(args));
			}
			values.addAll(customsParams);
			StringBuilder sbSet = new StringBuilder(setSql);
			for(String s : customsSets) {
				sbSet.append(",").append(s);
			}
			
			sql = SQLUtils.getCustomUpdateSQL(t, values, sbSet.toString());
		}
		
		int rows = jdbcExecuteUpdate(sql, values.toArray()); // 不会有in(?)表达式
		
		doInterceptAfterUpdate(tList, rows);
		
		return rows;
	}
	
	// ref: https://gist.github.com/PieterScheffers/189cad9510d304118c33135965e9cddb
	@Override @Transactional
	public <T> int updateAll(Class<T> clazz, String setSql, String whereSql, Object... args) {
		if(setSql != null) {setSql = setSql.replace('\t', ' ');}
		if(setSql == null || setSql.trim().isEmpty()) {
			return 0; // 不需要更新
		}
		
		List<Object> values = new ArrayList<Object>();
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		String sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql, null);
		
		List<String> customsSets = new ArrayList<String>();
		List<Object> customsParams = new ArrayList<Object>();
		
		doInterceptBeforeUpdate(clazz, sql, customsSets, customsParams, args);
		
		if(!customsSets.isEmpty()) { // 处理自定义加入set，需要重新生成sql
			values = new ArrayList<Object>();
			values.addAll(customsParams);
			if(args != null) {
				values.addAll(Arrays.asList(args));
			}
			
			StringBuilder sbSet = new StringBuilder();
			for(String s : customsSets) {
				sbSet.append(s).append(",");
			}
			sbSet.append(setSql.toLowerCase().startsWith("set ") ? setSql.substring(4) : setSql);
			
			setSql = sbSet.toString();
		} else {
			if(args != null) {
				values.addAll(Arrays.asList(args));
			}
		}
		
		int rows = 0;
		if(interceptors != null && !interceptors.isEmpty()) { // 查询出修改的所有列
			jdbcTemplate.execute("SET @uids := NULL");
			List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);
			StringBuilder selectUids = new StringBuilder("(SELECT @uids := CONCAT_WS(',',");
			for(Field key : keyFields) {
				selectUids.append("`" + key.getAnnotation(Column.class).value() + "`").append(",");
			}
			selectUids.append("@uids))");
			sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql, selectUids.toString());
			rows = namedJdbcExecuteUpdate(sql, values.toArray());
			String ids = jdbcTemplate.queryForObject("SELECT @uids", String.class);
			if(ids != null && !ids.trim().isEmpty()) {
				String strs[] = ids.split(",");
				int size = strs.length / keyFields.size();
				List<Object> result = new ArrayList<Object>();
				for(int i = 0; i < size; i++) {
					T t = null;
					try {
						t = clazz.newInstance();
					} catch (Exception e) {
						LOGGER.error("newInstance class {} fail", clazz, e);
					}
					if(t == null) {continue;}
					for(int j = 0; j < keyFields.size(); j++) {
						DOInfoReader.setValue(keyFields.get(j), t, strs[i * keyFields.size() + j]);
					}
					boolean succ = getByKey(t);
					if(!succ) {
						LOGGER.error("getByKey fail for t:{}", JSON.toJson(t));
					}
					result.add(t);
				}
				doInterceptAfterUpdate(result, rows);
			}
		} else {
			sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql, null);
		    rows = namedJdbcExecuteUpdate(sql, values.toArray());
		}
		return rows;
	}
	
}
