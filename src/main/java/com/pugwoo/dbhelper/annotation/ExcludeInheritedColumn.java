package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解仅用于读操作。
 * 注解上该注解的读取，将不再读取父类的任何Column或RelatedColumn注解的字段。
 * 但是可以通过设置isWithKey属性，额外加上父类的key Column字段
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeInheritedColumn {

	/**
	 * 如果值为true，则加上被注解了ExcludeInheritedColumn的父类的key Column字段
	 * @return
	 */
	boolean isWithKey() default false;
	
}
