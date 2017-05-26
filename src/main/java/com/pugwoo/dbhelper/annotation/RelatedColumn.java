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
 * 
 * 为了解决这种情况，dbhelper采用一种折中方案，当类型不同时，都转化成string进行判断，同时给出WARN日志
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedColumn {

	/**
	 * 必须，关联的列名。对应于主表中@Column注解的value值
	 * @return
	 */
	String value() default "";
	
	/**
	 * 必须，外部关联的表的相关字段，默认是id
	 */
	String remoteColumn() default "id";
	
	/**
	 * 当使用remoteColumn关联查询时，可以额外指定查询条件，
	 * extraWhere值为where开始(如果有where条件，必须带where关键字)的sql子句，不支持传递参数，
	 * 可以写order by和limit等子句。例如：
	 * where star=1 limit 5 或
	 * order by id limit 5 等写法
	 * 
	 * 当使用dataService时，该字段无效。
	 * @return
	 */
	String extraWhere() default "";
	
	/**
	 * 外部查询数据接口，该接口必须继承实现IDBHelperDataService
	 * @return
	 */
	Class<?> dataService() default void.class;

}
