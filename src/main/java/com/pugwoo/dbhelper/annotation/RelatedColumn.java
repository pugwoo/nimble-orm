package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2017年3月17日 22:15:11
 * @author pugwoo
 *
 * 关联的列，通常用于表数据关联(非join形式)。
 * 
 * 注意，关联查询时，请务必确保关联字段在Java是相同类型，否则java的equals方法会判断为不相等。
 * 
 * 为了解决这种情况，dbhelper采用一种折中方案，当类型不同时，都转化成string进行判断，同时给出WARN日志
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

}
