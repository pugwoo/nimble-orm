package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2017年3月17日 22:15:11
 * @author pugwoo
 *
 * 关联的列，通常用于表数据关联(非join形式)。
 * 
 * 注意，关联查询时，请务必确保关联字段在Java是相同类型，否则java的equals方法会判断为不相等。
 * 为了解决这种情况，dbhelper采用一种折中方案，当类型不同时，都转化成string进行判断，同时给出WARN日志。
 * 
 * 注意：当RelatedColumn没有数据时，且原字段值不为null，则不会修改该字段的值，相当于是一个默认值的功能。
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedColumn {
	
	/**
	 * 必须，本表关联的表字段名称（不是java属性名）。多个用逗号隔开。
	 */
	String localColumn();
	
	/**
	 * 必须，外部关联的表字段名称（不是java属性名）。多个用逗号隔开。
	 */
	String remoteColumn();

	/**
	 * 用于控制该RelateColumn是否启用的mvel脚本，其中使用变量t表示当前DO类实例。<br>
	 * 当为空或返回true时启用该RelatedColumn属性，当返回false或mvel脚本报错时不启用。<br>
	 * 说明：当正常返回false时，对于List的属性类型也会设置空List。但对于返回null或报错的情况，List属性值设置为null，更早暴露问题。
	 */
	String conditional() default "";
	
	/**
	 * 当使用remoteColumn关联查询时，可以额外指定查询条件，<br>
	 * extraWhere值为where开始(如果有where条件，必须带where关键字)的sql子句，不支持传递参数，<br>
	 * 可以写order by，例如order by create_time。<br>
	 * 不建议使用limit，当存在limit子句时，将不会用批量查询的方式，性能会急剧下降。<br>
	 * <br>
	 * 重要：【当使用dataService时，该字段无效。】<br>
	 */
	String extraWhere() default "";

	/**
	 * 指定数据源DBHelper bean名称，此时将使用该DBHelper实例进行查询。<br>
	 * 默认不需要指定，将使用当前DBHelper进行查询。
	 */
	String dbHelperBean() default "";
	
	/**
	 * 外部查询数据接口，该接口必须继承实现IDBHelperDataService
	 */
	Class<?> dataService() default void.class;

}
