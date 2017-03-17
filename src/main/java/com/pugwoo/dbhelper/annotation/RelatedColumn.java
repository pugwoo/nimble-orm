package com.pugwoo.dbhelper.annotation;

/**
 * 2017年3月17日 22:15:11
 * @author pugwoo
 *
 * 关联的列，通常用于表数据关联(非join形式)
 */
public @interface RelatedColumn {

	/**
	 * 关联的列名，默认是通过关联的DO的id值去拿。
	 * @return
	 */
	String value() default "";
	
	
}
