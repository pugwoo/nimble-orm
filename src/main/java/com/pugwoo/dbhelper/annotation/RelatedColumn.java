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
 * 为了解决这种情况，dbhelper采用一种折中方案，当类型不同时，都转化成string进行判断，同时给出WARN日志
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedColumn {
	
	/**
	 * 必须，本表关联的表字段名称（不是java属性名）。
	 * 说明：该字段只支持配置一个，只支持单字段关联。思考后决定不支持多个字段，因为多字段关联本身是不太合理的设计。
	 *      其次会导致IDBHelperDataService复杂化，而且也已经有了extraWhere的支持。
	 */
	String localColumn();
	
	/**
	 * 必须，外部关联的表字段名称（不是java属性名）
	 * 说明：该字段只支持配置一个，只支持单字段关联。思考后决定不支持多个字段，因为多字段关联本身是不太合理的设计。
	 *      其次会导致IDBHelperDataService复杂化，而且也已经有了extraWhere的支持。
	 */
	String remoteColumn();
	
	/**
	 * 当使用remoteColumn关联查询时，可以额外指定查询条件，
	 * extraWhere值为where开始(如果有where条件，必须带where关键字)的sql子句，不支持传递参数，
	 * 可以写order by，例如order by create_time。
	 * 不建议使用limit，因为在@RelatedColumn批量获取多个值时，由于limit的存在，会导致有些值关联不上，造成错误。
	 * 
	 * 重要：【当使用dataService时，该字段无效。】
	 * @return
	 */
	String extraWhere() default "";
	
	/**
	 * 外部查询数据接口，该接口必须继承实现IDBHelperDataService
	 * @return
	 */
	Class<?> dataService() default void.class;

}
