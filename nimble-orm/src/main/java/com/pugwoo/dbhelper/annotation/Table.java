package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2015年1月12日 16:19:46
 * 数据表信息注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

	/**
	 * 表名
	 */
	String value();

	/**
	 * 指定和另外一个DO/VO类同名，当指定别名时，value()值失效，建议将其留空，以免有歧义
	 */
	Class<?> sameTableNameAs() default void.class;

	/**
	 * 表别名，默认是t，该别名目前仅对查询操作生效
	 */
	String alias() default "t";

	/**
	 * 表注释
	 */
	String comment() default "";
}
