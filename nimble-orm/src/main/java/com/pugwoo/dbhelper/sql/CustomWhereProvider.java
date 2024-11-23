package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.annotation.WhereColumn;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 自定义提供Where条件
 */
public interface CustomWhereProvider {

    /**
     * 提供查询条件，一些使用技巧：
     * <br/>
     * 1) 可以通过field获得成员属性的类型
     * 2) 可以通过obj和field获得成员属性的值
     *
     * @param obj 包含了@WhereColumn注解的POJO对象
     * @param whereColumn 注解对象
     * @param field 当@WhereColumn注解在成员属性上时，field是注解了@WhereColumn的成员属性
     * @param method 当@WhereColumn注解在成员方法上时，method是注解了@WhereColumn的成员方法
     * @return 返回where查询条件
     */
    WhereSQL provide(Object obj, WhereColumn whereColumn, Field field, Method method);

}
