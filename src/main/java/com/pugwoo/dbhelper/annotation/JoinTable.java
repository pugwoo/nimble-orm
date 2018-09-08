package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pugwoo.dbhelper.enums.JoinTypeEnum;

/**
 * Join表的注解
 * 2017年4月17日 14:36:10
 * @author pugwoo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {

	/**
	 * join方式，默认join，可选：left join, right join等。
	 * 【注意】
	 * 对于软删除的处理：当是left join时，右表的软删除条件为 (t2.软删除字段=未删除 or t2.软删除字段 is null)。
	 *                 当是right join时，左表的软删除条件为 (t1.软删除字段=未删除 or t1.软删除字段 is null)
	 */
	JoinTypeEnum joinType() default JoinTypeEnum.JOIN;
	
	/**
	 * join的关联条件，必须，left表别名为t1，right表别名为t2
	 */
	String on();
	
}
