package com.pugwoo.dbhelper;

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
	 * @param values 该值是RelatedColumn.localColumn注解的字段的值的集合，元素类型为localClass
	 * @param localClass RelatedColumn.localColumn注解对应的类
	 * @param localColumn RelatedColumn.localColumn注解对应的值
	 * @param remoteClass RelatedColumn.remoteColumn注解对应的类
	 * @param remoteColumn RelatedColumn.remoteColumn注解对应的值
	 * @return 返回值应该是RelatedColumn.remoteColumn注解的对象的集合，元素类型必须是remoteClass
	 */
	List<?> get(List<Object> values,
			Class<?> localClass, String localColumn,
			Class<?> remoteClass, String remoteColumn);
	
}
