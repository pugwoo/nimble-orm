package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解仅用于读操作的select字段，不会影响写操作，也不会影响读操作的where及后续子句。
 * 注解上该注解的读取select字段，将不再包含父类的任何Column或RelatedColumn注解的字段。
 *
 * @deprecated 请使用@Table的sameTableNameAs属性代替，或者直接@Table写表名
 *             特别注意：在废弃该属性时，要注意有没有使用到dbHelper.getByKey的方法，如果有，应该在VO中加上主键Column注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface ExcludeInheritedColumn {

}
