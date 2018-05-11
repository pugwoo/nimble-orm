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
	public boolean beforeInsert(List<Object> list) {
		if(!list.isEmpty()) {
			System.out.println(">I> " + list.get(0).getClass().getSimpleName() + ",count:" + list.size() +
					"\n    data:" + JSON.toJson(list));
		}
		return true;
	}
	
	@Override
	public void afterInsert(List<Object> list, int affectedRows) {
		if(!list.isEmpty()) {
			System.out.println("<I< " + list.get(0).getClass().getSimpleName() + ",count:" + list.size()
	        + ",affectedRows:" + affectedRows +
			"\n    data:" + JSON.toJson(list));
		}
	}
	
	@Override
    public boolean beforeUpdate(List<Object> tList, String setSql, Object[] setSqlArgs) {
		if(!tList.isEmpty()) {
			System.out.println(">U> " + tList.get(0).getClass().getSimpleName() +
					"\n    data:" + JSON.toJson(tList));
		}
    	return true;
    }
    
	@Override
    public boolean beforeUpdateCustom(Class<?> clazz, String sql,
    		List<String> customsSets, List<Object> customsParams, Object[] args) {
		if(clazz.equals(StudentDO.class)) {
			customsSets.add("name=?");
			customsParams.add("beforeUpdateCustom" + new Date());
		}
		System.out.println(">U> " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + JSON.toJson(args));
    	return true;
    }
    
	@Override
    public void afterUpdate(List<Object> tList, int affectedRows) {
		if(!tList.isEmpty()) {
			System.out.println("<U< " + tList.get(0).getClass().getSimpleName() + ",affectedRows:" + affectedRows +
					"\n    data:" + JSON.toJson(tList));
		}
    }

	@Override
    public boolean beforeDelete(List<Object> tList) {
		if(!tList.isEmpty()) {
			System.out.println(">D> " + tList.get(0).getClass().getSimpleName() +
					"\n    data:" + JSON.toJson(tList));
		}
    	return true;
    }

	@Override
    public void afterDelete(List<Object> tList, int affectedRows) {
		if(!tList.isEmpty()) {
			System.out.println("<D< " + tList.get(0).getClass().getSimpleName() + ",affectedRows:" + affectedRows +
					"\n    data:" + JSON.toJson(tList));
		}
    }

}
