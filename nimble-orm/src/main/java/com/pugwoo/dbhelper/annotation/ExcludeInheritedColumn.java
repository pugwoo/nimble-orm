package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解仅用于读操作的select字段，不会影响写操作，也不会影响读操作的where及后续子句。
 * 注解上该注解的读取select字段，将不再包含父类的任何Column或RelatedColumn注解的字段。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeInheritedColumn {

}
