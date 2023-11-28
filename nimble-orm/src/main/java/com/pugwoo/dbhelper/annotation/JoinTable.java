package com.pugwoo.dbhelper.annotation;

import com.pugwoo.dbhelper.enums.JoinTypeEnum;

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
	 * join方式，默认join，可选：left join, right join等。
	 * 【注意】
	 * 对于软删除的处理：当是left join时，右表的软删除条件为 (t2.软删除字段=未删除 or t2.软删除字段 is null)。
	 *                 当是right join时，左表的软删除条件为 (t1.软删除字段=未删除 or t1.软删除字段 is null)
	 */
	JoinTypeEnum joinType() default JoinTypeEnum.JOIN;

	/**
	 * 字符串形式的join，当值不为空时，优先级高于joinType<br>
	 * 使用自定义join请自行处理好软删除的查询条件
	 */
	String joinTypeAsString() default "";

	/**
	 * join的关联条件，必须，left表别名为t1，right表别名为t2
	 */
	String on();

	/**
	 * 表注释
	 */
	String comment() default "";
	
}
