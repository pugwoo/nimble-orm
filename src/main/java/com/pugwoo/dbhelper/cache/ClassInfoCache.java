package com.pugwoo.dbhelper.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存类相关信息
 */
public class ClassInfoCache {

    private static final Map<Field, Method> fieldMethodMap = new HashMap<>();
    private static final Map<Field, Boolean> fieldMethodNullMap = new HashMap<>();

    /**
     * 获得field对应的method的缓存数据
     * @param field 字段
     * @return 不存在返回null
     */
    public synchronized static Method getFieldMethod(Field field) {
        Method method = fieldMethodMap.get(field);
        if (method != null) {
            return method;
        }

        Boolean isNull = fieldMethodNullMap.get(field);
        if (isNull != null && isNull) {
            return null;
        }

        String fieldName = field.getName();
        String setMethodName = "set" + firstLetterUpperCase(fieldName);

        try {
            method = field.getDeclaringClass().getMethod(setMethodName, field.getType());
        } catch (NoSuchMethodException e) {
            // 不需要打log，框架允许没有setter方法
        }

        if (method == null) {
            fieldMethodNullMap.put(field, true);
        } else {
            method.setAccessible(true);
            fieldMethodMap.put(field, method);
        }
        return method;
    }

    private static String firstLetterUpperCase(String str) {
        if (str == null || str.length() < 2) {
            return str;
        }
        String firstLetter = str.substring(0, 1).toUpperCase();
        return firstLetter + str.substring(1);
    }

}
