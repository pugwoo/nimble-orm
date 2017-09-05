package com.pugwoo.dbhelper.test.interceptor;

import java.util.List;

import com.pugwoo.dbhelper.DBHelperInterceptor;

public class MyLogChangeInterceptor extends DBHelperInterceptor {

	@Override
	public boolean beforeSelect(Class<?> clazz, String sql, Object[] args) {
		System.out.println("in intercptor, class=" + clazz + ",sql:" + sql + ",args:" + args);
		return true;
	}

	@Override
	public <T> List<T> afterSelect(Class<?> clazz, List<T> result, int count) {
		System.out.println("in interceptor, class=" + clazz + ",result size:" + result.size()
		   + ",count:" + count);
		return result;
	}

}
