package com.pugwoo.dbhelper.sql;

import java.lang.reflect.Field;

/**
 * 自定义提供Where条件
 */
public interface CustomWhereProvider {

    /**
     * 提供查询条件，一些使用技巧：
     * <br/>
     * 1) 可以通过field获得@WhereColumn的对象
     * 2) 可以通过field获得成员属性的类型
     * 3) 可以通过obj和field获得成员属性的值
     *
     * @param obj 包含了@WhereColumn注解的POJO对象
     * @param field 注解了@WhereColumn的成员属性
     * @return 返回where查询条件
     */
    WhereSQL provide(Object obj, Field field);

}
