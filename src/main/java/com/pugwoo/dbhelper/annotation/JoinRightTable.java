package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * join的右表注解，被注解的字段类型必须有@Table注解。右表的别名默认为t2
 * @author pugwoo
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinRightTable {

	/**
	 * 表别名
	 * @return
	 */
	String alias() default "t2";
	
}
