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
	 */
	String alias() default "t2";

	/**
	 * 强制使用索引，格式为: index1,index2 <br>
	 * 说明: 该字符串将原样放入到FORCE INDEX() 括弧中。
	 */
	String forceIndex() default "";

}
