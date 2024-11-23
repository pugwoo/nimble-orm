package com.pugwoo.dbhelper.cache;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.annotation.SqlColumn;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存类相关信息
 */
public class ClassInfoCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfoCache.class);

    private static final Map<Field, Method> fieldSetMethodMap = new ConcurrentHashMap<>();
    private static final Map<Field, Boolean> fieldSetMethodNullMap = new ConcurrentHashMap<>();

    /**
     * 获得field对应的method的缓存数据
     * @param field 字段
     * @return 不存在返回null
     */
    public static Method getFieldSetMethod(Field field) {
        boolean isCacheEnable = DBHelperContext.isCacheEnabled();

        Method method = null;
        if (isCacheEnable) {
            method = fieldSetMethodMap.get(field);
            if (method != null) {
                return method;
            }
            Boolean isNull = fieldSetMethodNullMap.get(field);
            if (isNull != null && isNull) {
                return null;
            }
        }

        String fieldName = field.getName();
        String setMethodName = "set" + InnerCommonUtils.firstLetterUpperCase(fieldName);

        try {
            method = field.getDeclaringClass().getMethod(setMethodName, field.getType());
        } catch (NoSuchMethodException e) {
            // 不需要打log，框架允许没有setter方法
        }

        if (isCacheEnable) {
            if (method == null) {
                fieldSetMethodNullMap.put(field, true);
            } else {
                method.setAccessible(true);
                fieldSetMethodMap.put(field, method);
            }
        }

        return method;
    }

    // ==================================================================================

    private static final Map<Field, Method> fieldGetMethodMap = new ConcurrentHashMap<>();
    private static final Map<Field, Boolean> fieldGetMethodNullMap = new ConcurrentHashMap<>();

    /**
     * 获得field对应的method的缓存数据
     * @param field 字段
     * @return 不存在返回null
     */
    public static Method getFieldGetMethod(Field field) {
        boolean isCacheEnable = DBHelperContext.isCacheEnabled();

        Method method = null;
        if (isCacheEnable) {
            method = fieldGetMethodMap.get(field);
            if (method != null) {
                return method;
            }
            Boolean isNull = fieldGetMethodNullMap.get(field);
            if (isNull != null && isNull) {
                return null;
            }
        }

        String fieldName = field.getName();
        String getMethodName = "get" + InnerCommonUtils.firstLetterUpperCase(fieldName);

        try {
            method = field.getDeclaringClass().getMethod(getMethodName);
        } catch (NoSuchMethodException e) {
            // 不需要打log，框架允许没有getter方法
        }

        if (isCacheEnable) {
            if (method == null) {
                fieldGetMethodNullMap.put(field, true);
            } else {
                method.setAccessible(true);
                fieldGetMethodMap.put(field, method);
            }
        }
        return method;
    }

    // ==================================================================================

    private static final Map<Class<?>, List<Field>> classColumnFieldMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> classSqlColumnFieldMap = new ConcurrentHashMap<>();

    /**
     * 获得clazz的所有Column字段
     */
    public static List<Field> getColumnFields(Class<?> clazz) {
        List<Field> fields;
        boolean isCacheEnable = DBHelperContext.isCacheEnabled();

        if (isCacheEnable) {
            fields = classColumnFieldMap.get(clazz);
            if (fields != null) {
                return fields;
            }
        }

        List<Class<?>> classLink = DOInfoReader.getClassAndParentClasses(clazz);
        fields = getFieldsForColumn(classLink);

        if (isCacheEnable) {
            classColumnFieldMap.put(clazz, fields);
        }

        return fields;
    }

    /**
     * 获得clazz的所有SqlColumn字段
     */
    public static List<Field> getSqlColumnFields(Class<?> clazz) {
        List<Field> fields;
        boolean isCacheEnable = DBHelperContext.isCacheEnabled();

        if (isCacheEnable) {
            fields = classSqlColumnFieldMap.get(clazz);
            if (fields != null) {
                return fields;
            }
        }

        List<Class<?>> classLink = DOInfoReader.getClassAndParentClasses(clazz);
        fields = getFieldsForSqlColumn(classLink);

        if (isCacheEnable) {
            classSqlColumnFieldMap.put(clazz, fields);
        }

        return fields;
    }

    /**
     * 获取classLink多个类中，注解了annotationClazz的Field字段。<br>
     * 目前annotationClazz的取值有@Column <br>
     * 对于Column注解，如果子类和父类字段同名，那么子类将代替父类，父类的Field不会加进来；<br>
     * 如果子类出现相同的Field，那么也只拿第一个；一旦出现字段同名，那么进行error log，正常是不建议覆盖和同名操作的，风险很大 <br>
     */
    private static List<Field> getFieldsForColumn(List<Class<?>> classLink) {
        List<Field> result = new ArrayList<>();
        if(classLink == null || classLink.isEmpty()) {
            return result;
        }

        // 按父类优先的顺序来
        List<List<Field>> fieldList = new ArrayList<>();
        for (int i = classLink.size() - 1; i >= 0; i--) {
            Field[] fields = classLink.get(i).getDeclaredFields();
            fieldList.add(InnerCommonUtils.filter(fields, o -> o.getAnnotation(Column.class) != null));
        }

        Set<String> columnValues = new HashSet<>();
        // 从子类开始拿
        List<List<Field>> fieldListTmp = new ArrayList<>();
        for (int i = fieldList.size() - 1; i >= 0; i--) {
            List<Field> fields = fieldList.get(i);
            List<Field> fieldsTmp = new ArrayList<>();
            for (Field field : fields) {
                Column column = field.getAnnotation(Column.class);
                if (columnValues.contains(column.value())) {
                    // 如果子类已经存在，那么子类会覆盖父类
                    LOGGER.error("found duplicate field:{} in class(and its parents):{}, this field is ignored",
                            field.getName(), classLink.get(0).getName());
                    continue;
                }
                columnValues.add(column.value());

                fieldsTmp.add(field);
            }
            fieldListTmp.add(0, fieldsTmp);
        }

        for (List<Field> fields : fieldListTmp) { // 最终排序是父类先，再子类
            result.addAll(fields);
        }

        return result;
    }

    private static List<Field> getFieldsForSqlColumn(List<Class<?>> classLink) {
        List<Field> result = new ArrayList<>();
        if(classLink == null || classLink.isEmpty()) {
            return result;
        }

        List<Field> fieldList = new ArrayList<>();
        for (int i = classLink.size() - 1; i >= 0; i--) {
            Field[] fields = classLink.get(i).getDeclaredFields();
            fieldList.addAll(InnerCommonUtils.filter(fields, o -> o.getAnnotation(SqlColumn.class) != null));
        }

        return fieldList;
    }


    // ==================================================================================

    private static final Map<Class<?>, Table> classTableMap = new ConcurrentHashMap<>();

    public static Table getTable(Class<?> clazz) {
        Table table;
        boolean isCacheEnable = DBHelperContext.isCacheEnabled();

        if (isCacheEnable) {
            table = classTableMap.get(clazz);
            if (table != null) {
                return table;
            }
        }

        table = getAnnotationClass(clazz, Table.class);

        if (isCacheEnable && table != null) {
            classTableMap.put(clazz, table);
        }

        return table;
    }

    /**
     * 从指定类clazz再往其父类查找注解了annotationClass的类，如果已经找到了，就返回对应该注解，不再继续往上找
     * @return 找不到返回null
     */
    private static <T extends Annotation> T getAnnotationClass(Class<?> clazz,
                                                               Class<T> annotationClass) {
        Class<?> curClass = clazz;
        while (curClass != null) {
            T annotation = curClass.getAnnotation(annotationClass);
            if(annotation != null) {
                return annotation;
            }
            curClass = curClass.getSuperclass();
        }

        return null;
    }

    // ==================================================================================

    private static final Map<Class<?>, List<Field>> relatedColumnMap = new ConcurrentHashMap<>();

    public static List<Field> getRelatedColumns(Class<?> clazz) {
        boolean isCacheEnable = DBHelperContext.isCacheEnabled();

        List<Field> fields;
        if (isCacheEnable) {
            fields = relatedColumnMap.get(clazz);
            if (fields != null) {
                return fields;
            }
        }

        List<Class<?>> classLink = DOInfoReader.getClassAndParentClasses(clazz);

        // 父类优先
        fields = new ArrayList<>();
        for (int i = classLink.size() - 1; i >= 0; i--) {
            Field[] f = classLink.get(i).getDeclaredFields();
            for (Field field : f) {
                if (field.getAnnotation(RelatedColumn.class) != null) {
                    fields.add(field);
                }
            }
        }

        if (isCacheEnable) {
            relatedColumnMap.put(clazz, fields);
        }
        return fields;
    }

}
