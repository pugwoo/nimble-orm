package com.pugwoo.dbhelper.test.interceptor;

import java.util.List;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.wooutils.json.JSON;

public class MyLogChangeInterceptor extends DBHelperInterceptor {

	@Override
	public boolean beforeSelect(Class<?> clazz, String sql, Object[] args) {
		System.out.println(">>> " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + JSON.toJson(args));
		return true;
	}

	@Override
	public <T> List<T> afterSelect(Class<?> clazz, String sql, Object[] args, List<T> result, int count) {
		System.out.println("<<< " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + JSON.toJson(args)
		    + "\n    total:" + count + ",size:" + result.size() + ",data:" + JSON.toJson(result));
		return result;
	}
	
	

}
