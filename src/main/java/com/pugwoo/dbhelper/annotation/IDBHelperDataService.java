package com.pugwoo.dbhelper.annotation;

import java.util.List;

/**
 * 关联字段自定义读取接口
 * @author pugwoo
 */
public interface IDBHelperDataService {

	/**
	 * 要实现的接口
	 * @param values
	 * @return
	 */
	List<Object> get(List<Object> values);
	
}
