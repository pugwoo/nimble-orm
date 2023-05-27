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
	 * 指定软删除表名，本表删除时，会将删除的数据插入到该表中，该表的结构必须和本表一致。<br>
	 */
	String softDeleteTable() default "";

	/**
	 * 当软删除表不是同一个数据库时，可以通过softDeleteDBHelper指定数据库
	 */
	String softDeleteDBHelperBean() default "";

	/**
	 * 表级别的插入数据默认值集合，这里填的是DBHelperContext中的默认值集合的名称<br>
	 * 如果字段级别有默认值，则它的优先级高于表级别的默认值<br>
	 */
	String insertDefaultValueMap() default "";

	/**
	 * 虚拟表SQL，从select开始的完整SQL，该方式只对查询操作有效。当非空时有效，此时value()表名失效。<br>
	 * <br>
	 * 说明：虚拟表也支持path路径，path路径的文件内容就是SQL。
	 * <br>
	 * 该方式适用于自定义表，接近于getRaw方法，有以下约束：<br>
	 * 1) getPage不会自动移除limit和加order by
	 * 2) 不支持自动处理软删除softDelete标记
	 * 3) 不支持getByKey/getByKeyList/getAllKey/getByExample
	 */
	String virtualTableSQL() default "";

	/**
	 * 作用同virtualTableSQL()注解，但是是一个classpath路径
	 */
	String virtualTablePath() default "";

	/**
	 * 表别名，默认是t，该别名目前仅对查询操作生效
	 */
	String alias() default "t";

	/**
	 * 表注释
	 */
	String comment() default "";
}
