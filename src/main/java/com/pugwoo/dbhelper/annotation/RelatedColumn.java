package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2017年3月17日 22:15:11
 * @author pugwoo
 *
 * 关联的列，通常用于表数据关联(非join形式)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedColumn {

	/**
	 * 关联的列名，默认是通过关联的DO的id值去拿。
	 * @return
	 */
	String value() default "";
	
	/**
	 * 外部关联的表的相关字段，默认是id
	 */
	String remoteColumn() default "id";
	
	/**
	 * 要实现的外部接口
	 * @return
	 */
	Class<?> dataService() default void.class;

	// TODO 规划：支持插入、更新、删除级联操作，提供独立casecade接口
	
}
