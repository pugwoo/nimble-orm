package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于注解描述的where条件，所有条件以and的形式连接。
 * 约定字段生效的条件：
 * 1) String类型，当不为null且isEmpty为false时生效
 * 2) Map和Collection类型，当不为null且包含至少1个元素时生效
 * 3) 其它类型不为null时生效
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WhereColumn {

    /**
     * where条件的表达式，使用问号?或:fieldVariableName 作为占位符
     */
    String value();

    /**
     * 逻辑或的分组名称，相同分组名称会归为同一组or条件，并加上括号()
     */
    String orGroupName() default "";

    /**
     * 自定义where条件提供者，实现CustomWhereProvider接口，将从Spring容器中拿到
     */
    Class<?> customWhereProvider() default void.class;

}
