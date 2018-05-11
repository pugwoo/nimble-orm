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
	public <T> boolean beforeInsert(List<T> list) {
		if(!list.isEmpty()) {
			System.out.println(">I> " + list.get(0).getClass().getSimpleName() + ",count:" + list.size() +
					"\n    data:" + JSON.toJson(list));
		}
		return true;
	}
	
	@Override
	public <T> void afterInsert(List<T> list, int affectedRows) {
		if(!list.isEmpty()) {
			System.out.println("<I< " + list.get(0).getClass().getSimpleName() + ",count:" + list.size()
	        + ",affectedRows:" + affectedRows +
			"\n    data:" + JSON.toJson(list));
		}
	}
	
	@Override
    public <T> boolean beforeUpdate(List<T> tList, String setSql, Object[] setSqlArgs) {
		if(!tList.isEmpty()) {
			System.out.println(">U> " + tList.get(0).getClass().getSimpleName() +
					"\n    data:" + JSON.toJson(tList));
		}
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
    public <T> void afterUpdate(List<T> tList, int affectedRows) {
		if(!tList.isEmpty()) {
			System.out.println("<U< " + tList.get(0).getClass().getSimpleName() + ",affectedRows:" + affectedRows +
					"\n    data:" + JSON.toJson(tList));
		}
    }

	@Override
    public <T> boolean beforeDelete(Class<?> clazz, List<T> tList) {
		System.out.println(">D> " + clazz.getSimpleName() +
				"\n    data:" + JSON.toJson(tList));
    	return true;
    }

	@Override
    public <T> void afterDelete(Class<?> clazz, List<T> tList, int affectedRows) {
		System.out.println("<D< " + clazz.getSimpleName() + ",affectedRows:" + affectedRows +
				"\n    data:" + JSON.toJson(tList));
    }

}
