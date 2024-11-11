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
@Target({ElementType.FIELD}) // TODO 后续支持注解在方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface WhereColumn {

    /**
     * where条件的表达式<br>
     * 如果使用WhereSQL，请使用问号?表示当前参数值，可以有多个?，但这个问号都会只表示该值<br>
     */
    String value();

    /**
     * 逻辑或的分组名称，相同分组名称会归为同一组or条件，并加上括号()。说明，空白字符会自动trim成空字符串。
     */
    String orGroupName() default "";

    /**
     * 自定义where条件提供者，实现CustomWhereProvider接口，将从Spring容器中拿到
     */
    // Class<?> customWhereProvider() default void.class; // TODO 待实现

}
