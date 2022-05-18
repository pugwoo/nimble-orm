package com.pugwoo.dbhelper.cache;

import com.pugwoo.dbhelper.utils.InnerCommonUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存类相关信息
 */
public class ClassInfoCache {

    private static final Map<Field, Method> fieldSetMethodMap = new ConcurrentHashMap<>();
    private static final Map<Field, Boolean> fieldSetMethodNullMap = new ConcurrentHashMap<>();

    /**
     * 获得field对应的method的缓存数据
     * @param field 字段
     * @return 不存在返回null
     */
    public static Method getFieldSetMethod(Field field) {
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

    private static final Map<Field, Method> fieldGetMethodMap = new ConcurrentHashMap<>();
    private static final Map<Field, Boolean> fieldGetMethodNullMap = new ConcurrentHashMap<>();

    /**
     * 获得field对应的method的缓存数据
     * @param field 字段
     * @return 不存在返回null
     */
    public static Method getFieldGetMethod(Field field) {
        Method method = fieldGetMethodMap.get(field);
        if (method != null) {
            return method;
        }

        Boolean isNull = fieldGetMethodNullMap.get(field);
        if (isNull != null && isNull) {
            return null;
        }

        String fieldName = field.getName();
        String getMethodName = "get" + InnerCommonUtils.firstLetterUpperCase(fieldName);

        try {
            method = field.getDeclaringClass().getMethod(getMethodName);
        } catch (NoSuchMethodException e) {
            // 不需要打log，框架允许没有getter方法
        }

        if (method == null) {
            fieldGetMethodNullMap.put(field, true);
        } else {
            method.setAccessible(true);
            fieldGetMethodMap.put(field, method);
        }
        return method;
    }

    private static final Map<Class<?>, List<Field>> classFieldMap = new ConcurrentHashMap<>();

    public static void putField(Class<?> clazz, List<Field> field) {
        classFieldMap.put(clazz, field);
    }

    public static List<Field> getField(Class<?> clazz) {
        return classFieldMap.get(clazz);
    }

}
