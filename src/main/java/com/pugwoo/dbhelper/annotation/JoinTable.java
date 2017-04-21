package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Join表的注解
 * 2017年4月17日 14:36:10
 * @author pugwoo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {

	/**
	 * join方式，默认join，可选：left join, right join等
	 * @return
	 */
	String joinType() default "join";
	
	/**
	 * join的关联条件，必须，left表别名为t1，right表别名为t2
	 * @return
	 */
	String on();
	
}
