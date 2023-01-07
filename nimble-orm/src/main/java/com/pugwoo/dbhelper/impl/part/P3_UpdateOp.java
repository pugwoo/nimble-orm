package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.exception.CasVersionNotMatchException;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.PreHandleObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class P3_UpdateOp extends P2_InsertOp {
	
	/////////////// 拦截器
	private void doInterceptBeforeUpdate(List<Object> tList, String setSql, List<Object> setSqlArgs) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeUpdate(tList, setSql, setSqlArgs);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	private void doInterceptBeforeUpdate(Class<?> clazz, String sql,
			List<String> customsSets, List<Object> customsParams, List<Object> args) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeUpdateAll(clazz, sql, customsSets, customsParams, args);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	private void doInterceptAfterUpdate(final List<Object> tList, final int rows) {
		Runnable runnable = () -> {
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				interceptors.get(i).afterUpdate(tList, rows);
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}

	//////////////

	@Override
	public <T> int update(T t) throws NullKeyValueException {
		return _update(t, false, true, null);
	}
	
	@Override
	public <T> int update(T t, String postSql, Object... args) throws NullKeyValueException {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		return _update(t, false, true, postSql, args);
	}
	
	@Override
	public <T> int updateWithNull(T t) throws NullKeyValueException {
		return _update(t, true, true, null);
	}
	
	@Override
	public <T> int updateWithNull(T t, String postSql, Object... args) throws NullKeyValueException {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		return _update(t, true, true, postSql, args);
	}

	@Override
	public <T> int update(Collection<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
			return 0;
		}

		List<Object> tmpList = new ArrayList<>(list);
		doInterceptBeforeUpdate(tmpList, null, null);
		
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += _update(t, false, false, null);
			}
		}
		
		doInterceptAfterUpdate(tmpList, rows);
		return rows;
	}
	
	private <T> int _update(T t, boolean withNull, boolean withInterceptors,
			String postSql, Object... args) throws NullKeyValueException {
		
		if(DOInfoReader.getNotKeyColumns(t.getClass()).isEmpty()) {
			return 0; // not need to update
		}
		
		PreHandleObject.preHandleUpdate(t);
		
		List<Object> tList = new ArrayList<>();
		tList.add(t);
		
		if(withInterceptors) {
			doInterceptBeforeUpdate(tList, null, null);
		}
		
		List<Object> values = new ArrayList<>();
		String sql = SQLUtils.getUpdateSQL(t, values, withNull, postSql);
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		
		int rows = namedJdbcExecuteUpdate(sql, values.toArray());

		postHandleCasVersion(t, rows);

		if(withInterceptors) {
			doInterceptAfterUpdate(tList, rows);
		}
		
		return rows;
	}

    /**
     * 后处理casVersion相关内容：
     * 1. 当DO类有注解casVersion但是数据库没有修改时抛出异常。
     * 2. 当DO类有注解casVersion且数据库提交成功时，自动设置casVersion+1
     */
	private void postHandleCasVersion(Object t, int rows) {
        Field casVersionField = DOInfoReader.getCasVersionColumn(t.getClass());
        if(casVersionField != null) {
            if(rows <= 0) {
                throw new CasVersionNotMatchException("update fail for class:"
                        + t.getClass().getName() + ",data:" + NimbleOrmJSON.toJson(t));
            } else {
                Object casVersion = DOInfoReader.getValue(casVersionField, t);
                if(casVersion instanceof Integer) {
                    Integer newVersion = ((Integer) casVersion) + 1;
                    DOInfoReader.setValue(casVersionField, t, newVersion);
                } else if (casVersion instanceof Long) {
                    Long newVersion = ((Long) casVersion) + 1;
                    DOInfoReader.setValue(casVersionField, t, newVersion);
                }
                // 其它类型ignore，已经在update之前就断言casVersion必须是Integer或Long类型
            }
        }
    }
	
	@Override
	public <T> int updateCustom(T t, String setSql, Object... args) throws NullKeyValueException {
		if(setSql != null) {setSql = setSql.replace('\t', ' ');}
		if(InnerCommonUtils.isBlank(setSql)) {
			return 0; // 不需要更新
		}
		
		List<Object> values = new ArrayList<>();
		if(args != null) {
			values.addAll(Arrays.asList(args));
		}
		String sql = SQLUtils.getCustomUpdateSQL(t, values, setSql); // 这里values里面的内容会在方法内增加
		
		List<Object> tList = new ArrayList<>();
		tList.add(t);
		
		doInterceptBeforeUpdate(tList, setSql, values);

		int rows = namedJdbcExecuteUpdate(sql, values.toArray());

		postHandleCasVersion(t, rows);

		doInterceptAfterUpdate(tList, rows);
		
		return rows;
	}
	
	// ref: https://gist.github.com/PieterScheffers/189cad9510d304118c33135965e9cddb
	@Override
	public <T> int updateAll(Class<T> clazz, String setSql, String whereSql, Object... args) {
		if(setSql != null) {setSql = setSql.replace('\t', ' ');}
		if(InnerCommonUtils.isBlank(setSql)) {
			return 0; // 不需要更新
		}
		
		List<Object> values;

		String sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql, null);
		
		List<String> customsSets = new ArrayList<>();
		List<Object> customsParams = new ArrayList<>();
		
		List<Object> argsList = new ArrayList<>();
		if(args != null) {
			argsList.addAll(Arrays.asList(args));
		}
		doInterceptBeforeUpdate(clazz, sql, customsSets, customsParams, argsList);
		
		if(!customsSets.isEmpty()) { // 处理自定义加入set，需要重新生成sql
			values = new ArrayList<>();
			values.addAll(customsParams);
			values.addAll(argsList);
			
			StringBuilder sbSet = new StringBuilder();
			for(String s : customsSets) {
				sbSet.append(s).append(",");
			}
			sbSet.append(setSql.toLowerCase().startsWith("set ") ? setSql.substring(4) : setSql);
			
			setSql = sbSet.toString();
		} else {
			values = new ArrayList<>(argsList);
		}
		
		sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql, null);

		return namedJdbcExecuteUpdate(sql, values.toArray());
	}
	
}
