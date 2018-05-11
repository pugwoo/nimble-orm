package com.pugwoo.dbhelper.test.interceptor;

import java.util.Date;
import java.util.List;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.json.JSON;
import com.pugwoo.dbhelper.test.entity.StudentDO;

public class MyLogChangeInterceptor extends DBHelperInterceptor {

	@Override
	public boolean beforeSelect(Class<?> clazz, String sql, Object[] args) {
		System.out.println(">S> " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + JSON.toJson(args));
		return true;
	}

	@Override
	public <T> List<T> afterSelect(Class<?> clazz, String sql, Object[] args, List<T> result, int count) {
		System.out.println("<S< " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + JSON.toJson(args)
		    + "\n    total:" + count + ",size:" + result.size() + ",data:" + JSON.toJson(result));
		return result;
	}
	
	@Override
	public <T> boolean beforeInsert(Class<?> clazz, List<T> list) {
		System.out.println(">I> " + clazz.getSimpleName() + ",count:" + list.size() +
				"\n    data:" + JSON.toJson(list));
		return true;
	}
	
	@Override
	public <T> void afterInsert(Class<?> clazz, List<T> list, int affectedRows) {
		System.out.println("<I< " + clazz.getSimpleName() + ",count:" + list.size()
		        + ",affectedRows:" + affectedRows +
				"\n    data:" + JSON.toJson(list));
	}
	
	@Override
    public <T> boolean beforeUpdate(Class<?> clazz, T t, String setSql, Object[] setSqlArgs) {
		System.out.println(">U> " + clazz.getSimpleName() +
				"\n    data:" + JSON.toJson(t));
    	return true;
    }
    
	@Override
    public <T> boolean beforeUpdateCustom(Class<?> clazz, String sql,
    		List<String> customsSets, List<Object> customsParams, Object[] args) {
		if(clazz.equals(StudentDO.class)) {
			customsSets.add("name=?");
			customsParams.add("beforeUpdateCustom" + new Date());
		}
		System.out.println(">U> " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + JSON.toJson(args));
    	return true;
    }
    
	@Override
    public <T> void afterUpdate(Class<?> clazz, List<T> tList, int affectedRows) {
		System.out.println("<U< " + clazz.getSimpleName() + ",affectedRows:" + affectedRows +
				"\n    data:" + JSON.toJson(tList));
    }

	@Override
    public <T> boolean beforeDelete(Class<?> clazz, T t) {
		System.out.println(">D> " + clazz.getSimpleName() +
				"\n    data:" + JSON.toJson(t));
    	return true;
    }

	@Override
    public <T> void afterDelete(Class<?> clazz, T t, int affectedRows) {
		System.out.println("<D< " + clazz.getSimpleName() + ",affectedRows:" + affectedRows +
				"\n    data:" + JSON.toJson(t));
    }

}
