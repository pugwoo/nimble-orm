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
	
	/**
	 * 软删除标记，如果注解了该数据，则对应的字段为软删除标记。<br>
	 * 这里【必须】提供一个数组，包含两个值，第一个值是未删除标记，第二个值是已删除标记。<br>
	 * <br>
	 * 一般软删除字段是数组，所以softDelete的值会直接设置到sql中。<br>
	 * 如果这个字段是字符串（情况很少），请这样写：softDelete = {"'NO'", "'YES'"}
	 * <br>
	 * 【错误的个数(不是两个)和空的String值将认为是无效的标记而失效。】<br>
	 * <br>
	 * 每个DO最多有一个softDelete字段表示软删除。可以不用软删除。<br>
	 * <br>
	 * 注：因为软删除标记一般不走索引，所以这里一律用String类型表达，并不会太影响数据库性能。<br>
	 * <br>
	 * 使用示例：<br>
	 * \@Column(value = "deleted", softDelete = {"0", "1"}) private Boolean deleted;<br>
	 * <br>
	 * @return
	 */
	String[] softDelete() default "";
	
	/**
	 * 当设置为true时，且原值为null时，更新时会自动设置Date，对应的类型必须是java.util.Date及其子类
	 * 
	 * @return
	 */
	boolean setTimeWhenInsert() default false;
	
	/**
	 * 当设置为true时，且原值为null时，更新时会自动设置Date，对应的类型必须是java.util.Date及其子类
	 * 
	 * @return
	 */
	boolean setTimeWhenUpdate() default false;
	
	/**
	 * 当设置了非空的字符串，且原值是null时，自动设置上值
	 * @return
	 */
	String insertDefault() default "";
	
}
