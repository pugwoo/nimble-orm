package com.pugwoo.dbhelper.test.interceptor;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.test.entity.StudentDO;

import java.util.List;

public class MyLogChangeInterceptor implements DBHelperInterceptor {

	@Override
	public boolean beforeSelect(Class<?> clazz, String sql, List<Object> args) {
		System.out.println(">S> " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + NimbleOrmJSON.toJson(args));
		return true;
	}

	@Override
	public <T> List<T> afterSelect(Class<?> clazz, String sql, List<Object> args, List<T> result, long count) {
		System.out.println("<S< " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + NimbleOrmJSON.toJson(args)
		    + "\n    total:" + count + ",size:" + result.size() + ",data:" + NimbleOrmJSON.toJson(result));
		return result;
	}
	
	@Override
	public boolean beforeInsert(List<Object> list) {
		if(!list.isEmpty()) {
			System.out.println(">I> " + list.get(0).getClass().getSimpleName() + ",count:" + list.size() +
					"\n    data:" + NimbleOrmJSON.toJson(list));
		}
		return true;
	}
	
	@Override
	public void afterInsert(List<Object> list, int affectedRows) {
		if(!list.isEmpty()) {
			System.out.println("<I< " + list.get(0).getClass().getSimpleName() + ",count:" + list.size()
	        + ",affectedRows:" + affectedRows +
			"\n    data:" + NimbleOrmJSON.toJson(list));
		}
	}
	
	@Override
    public boolean beforeUpdate(List<Object> tList, String setSql, List<Object> setSqlArgs) {
		if(!tList.isEmpty()) {
			System.out.println(">U> " + tList.get(0).getClass().getSimpleName() +
					"\n    data:" + NimbleOrmJSON.toJson(tList));
		}
    	return true;
    }
    
	@Override
    public boolean beforeUpdateAll(Class<?> clazz, String sql,
    		List<String> customsSets, List<Object> customsParams, List<Object> args) {
		if(clazz.equals(StudentDO.class)) {
			customsSets.add("school_snapshot=?");
			customsParams.add("{}");
		}
		System.out.println(">U> " + clazz.getSimpleName() + ",sql:" + sql + "\n    args:" + NimbleOrmJSON.toJson(args));
    	return true;
    }
    
	@Override
    public void afterUpdate(List<Object> tList, int affectedRows) {
		if(!tList.isEmpty()) {
			System.out.println("<U< " + tList.get(0).getClass().getSimpleName() + ",affectedRows:" + affectedRows +
					"\n    data:" + NimbleOrmJSON.toJson(tList));
		}
    }

	@Override
    public boolean beforeDelete(List<Object> tList) {
		if(!tList.isEmpty()) {
			System.out.println(">D> " + tList.get(0).getClass().getSimpleName() +
					"\n    data:" + NimbleOrmJSON.toJson(tList));
		}
    	return true;
    }

	@Override
    public void afterDelete(List<Object> tList, int affectedRows) {
		if(!tList.isEmpty()) {
			System.out.println("<D< " + tList.get(0).getClass().getSimpleName() + ",affectedRows:" + affectedRows +
					"\n    data:" + NimbleOrmJSON.toJson(tList));
		}
    }

}
