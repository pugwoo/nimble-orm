package com.pugwoo.dbhelper;

import com.pugwoo.dbhelper.annotation.RelatedColumn;

import java.util.List;

/**
 * 关联字段自定义读取接口。
 *
 * @author pugwoo
 */
public interface DBHelperDataService {

	/**
	 * 关联查询RelatedColumn的外部数据服务接口。<br>
	 * 该接口适合于SOA或缓存的关联查询场景。<br>
	 *
	 * @param values 该值是RelatedColumn注解的字段的值的集合；
	 *               当localColumn的值为1个列时，该集合的元素是单个类型
	 *               当localColumn的值为大于1列时，该集合的元素是List
	 * @param relatedColumn 该接口实际使用关联到的relatedColumn注解实例
	 * @param localDOClass RelatedColumn注解对应的本地DO类
	 * @param remoteDOClass RelatedColumn注解对应的远程DO类
	 * @return 返回值应该是RelatedColumn.remoteColumn注解的对象的集合；
	 *               当localColumn的值为1个列时，该集合的元素是单个类型
	 *               当localColumn的值为大于1列时，该集合的元素是List
	 */
	List<?> get(List<Object> values, RelatedColumn relatedColumn,
				Class<?> localDOClass, Class<?> remoteDOClass);

}
