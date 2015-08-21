package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2015年1月12日 16:34:32
 * 数据表列的信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

	/**
	 * 列名
	 * 
	 * @return
	 */
	String value();
	
	/**
	 * 是否主键
	 * 
	 * @return
	 */
	boolean isKey() default false;
	
	/**
	 * 主键是否自增
	 * 
	 * @return
	 */
	boolean isAutoIncrement() default false;
}
