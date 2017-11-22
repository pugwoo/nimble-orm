package com.pugwoo.dbhelper.annotation;

import java.util.List;

/**
 * 关联字段自定义读取接口
 * @author pugwoo
 */
public interface IDBHelperDataService {

	/**
	 * 该接口适合于SOA或缓存的关联查询场景。
	 * 例如输入的是用户UserDO的id list，返回的就应该是对应的UserDO的对象集合。
	 * 
	 * @param values 该值是RelatedColumn.localColumn注解的字段的值的集合
	 * @return 返回值应该是RelatedColumn.remoteColumn注解的对象的集合
	 */
	List<Object> get(List<Object> values);
	
}
