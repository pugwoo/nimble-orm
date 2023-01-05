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
	 */
	String value();

	/**
	 * 列注释
	 */
	String comment() default "";
	
	/**
	 * 标记该列是否转成json字符串存入数据库，此时数据库列的类型必须可以存放字符串
	 */
	boolean isJSON() default false;
	
	/**
	 * 计算列。注意：计算列的别名为value属性的值，不需要再写上as 别名。
	 */
	String computed() default "";
	
	/**
	 * 是否主键
	 */
	boolean isKey() default false;
	
	/**
	 * 主键是否自增
	 */
	boolean isAutoIncrement() default false;

	/**
	 * 写入到数据库的最大字符串长度（字符数，不是字节数）<br>
	 * 当字段的值的字符串长度超过设置的值时，将自动截取前maxStringLength个字符。<br>
	 * 说明：如果值为负数表示不限制。该限制只对String类型生效，其它类型不生效。特别的，当值为0时，字符串将截断成空字符串。
	 */
	int maxStringLength() default -1;
	
	/**
	 * 插入时，当字段值未提供时，设置是否自动生成随机32字符，此时字段必须是String类型。<br>
	 * 这个功能用于随机生成主键。
	 */
	boolean setRandomStringWhenInsert() default false;
	
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
	 * 特别的，softDelete的第二个参数写为列名称时，可以在软删除时设置为该列的值
	 */
	String[] softDelete() default "";
	
	/**
	 * 当设置为true时，且原值为null时，更新时会自动设置当前日期时间，对应的类型必须是以下其中一种，对应精度约定如下：<br>
	 * java.util.Date 精度：秒<br>
	 * java.sql.Date 精度：秒<br>
	 * java.sql.Timestamp 精度：毫秒<br>
	 * java.sql.Time 精度：秒<br>
	 * java.time.LocalDateTime 精度：秒<br>
	 * java.time.LocalDate 精度：天<br>
	 * java.time.LocalTime 精度：秒<br>
	 * java.util.Calendar 精度：秒<br>
	 * java.time.Instant 精度：秒<br>
	 * java.time.ZonedDateTime 精度：秒
	 */
	boolean setTimeWhenInsert() default false;
	
	/**
	 * 当设置为true时，无论改值是不是null都会更新时会自动设置当前日期时间，
	 * （这是因为，从数据库全量查出来的updateTime是有值的，只能强制设置Date）<br>
	 * 特别的，软删除时，会设置该值为删除时的时间。<br>
	 * 对应的类型必须是以下其中一种，对应精度约定如下：<br>
	 * java.util.Date 精度：秒<br>
	 * java.sql.Date 精度：秒<br>
	 * java.sql.Timestamp 精度：毫秒<br>
	 * java.sql.Time 精度：秒<br>
	 * java.time.LocalDateTime 精度：秒<br>
	 * java.time.LocalDate 精度：天<br>
	 * java.time.LocalTime 精度：秒<br>
	 * java.util.Calendar 精度：秒<br>
	 * java.time.Instant 精度：秒<br>
	 * java.time.ZonedDateTime 精度：秒
	 */
	boolean setTimeWhenUpdate() default false;

	/**
	 * 当设置为true时，会自动设置当前日期时间<br>
	 * 对应的类型必须是以下其中一种，对应精度约定如下：<br>
	 * java.util.Date 精度：秒<br>
	 * java.sql.Date 精度：秒<br>
	 * java.sql.Timestamp 精度：毫秒<br>
	 * java.sql.Time 精度：秒<br>
	 * java.time.LocalDateTime 精度：秒<br>
	 * java.time.LocalDate 精度：天<br>
	 * java.time.LocalTime 精度：秒<br>
	 * java.util.Calendar 精度：秒<br>
	 * java.time.Instant 精度：秒<br>
	 * java.time.ZonedDateTime 精度：秒
	 */
	boolean setTimeWhenDelete() default false;

	/**
	 * 设置当字段从数据库读取的值为null时，设置为该脚本返回的值
	 */
	String readIfNullScript() default "";

	/**
	 * 当设置了非空字符串时，在对象插入数据库之前，会自动执行该mvel脚本获得值，并把值设置到DO中，再插入数据库。<br>
	 * mvel脚本中，可以通过t标识获取当前插入的对象。<br>
	 * 从1.2版本起，仅当原值是null时，才自动执行脚本并设置脚本返回的值。
	 */
	String insertValueScript() default "";

	/**
	 * 当设置了非空字符串时，在对象更新数据库之前，会自动执行该mvel脚本获得值，并把值设置到DO中，再插入数据库。<br>
	 * mvel脚本中，可以通过t标识获取当前插入的对象<br>
	 * 【特别注意】由于是更新，因此无论原值是否为null，该脚本都会执行并设置脚本返回的值。<br>
     * 【特别注意】对于updateAll方法，mvel脚本无法获得该变量t<br>
     * 说明：该脚本对于updateCustom和updateAll也是生效的。<br>
     * 说明：如果脚本的返回值为null，则等价于不设置值（不支持通过该脚本将数据库的值设置为null）。
	 * 【特别注意】对于updateCustom和updateAll方法，要求脚本返回的对象toString之后，数据库可以识别
	 */
	String updateValueScript() default "";

	/**
	 * 当设置了非空字符串时，在对象删除数据之前，会自动执行该mvel脚本获得值，并把值设置到DO中，再写入数据库（软删除时）。<br>
	 * 说明：无论是否是软删除，该脚本都会被执行。<br>
     * 【特别注意】对于delete(Class clazz, String postSql, Object... args)方法，有使用该脚本的DO类将使用逐个删除的方式进行删除，此时性能可能比较慢，对于快速批量删除的场景，请勿使用deleteValueScript脚本。
	 */
	String deleteValueScript() default "";

	/**
	 * 当查询、新增、修改、删除的mvel脚本执行出错时，是否忽略该错误，默认忽略并返回null。
	 * 如果设置为不忽略，则会在脚本执行出错时，抛出异常ScriptErrorException。
	 */
	boolean ignoreScriptError() default true;

	/**
	 * 乐观锁，当字段为乐观锁时，它必须是Integer或Long类型，默认初始值为1，
	 * 当乐观锁失败时抛出CasVersionNotMatchException。
	 * 该特性仅对update和updateCustom方法生效，对updateAll不生效。
	 * 限制一个DO类最多只能有一个casVersion字段。
	 * 新增和更新会自动设置该字段的值，因此不需要在数据库和程序中手动去修改该值。
	 */
	boolean casVersion() default false;

}
