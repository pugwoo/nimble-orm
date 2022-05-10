package com.pugwoo.dbhelper.cache;

import com.pugwoo.dbhelper.utils.InnerCommonUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存类相关信息
 */
public class ClassInfoCache {

    private static final Map<Field, Method> fieldSetMethodMap = new HashMap<>();
    private static final Map<Field, Boolean> fieldSetMethodNullMap = new HashMap<>();

    /**
     * 获得field对应的method的缓存数据
     * @param field 字段
     * @return 不存在返回null
     */
    public synchronized static Method getFieldSetMethod(Field field) {
        Method method = fieldSetMethodMap.get(field);
        if (method != null) {
            return method;
        }

        Boolean isNull = fieldSetMethodNullMap.get(field);
        if (isNull != null && isNull) {
            return null;
        }

        String fieldName = field.getName();
        String setMethodName = "set" + InnerCommonUtils.firstLetterUpperCase(fieldName);

        try {
            method = field.getDeclaringClass().getMethod(setMethodName, field.getType());
        } catch (NoSuchMethodException e) {
            // 不需要打log，框架允许没有setter方法
        }

        if (method == null) {
            fieldSetMethodNullMap.put(field, true);
        } else {
            method.setAccessible(true);
            fieldSetMethodMap.put(field, method);
        }
        return method;
    }

}
