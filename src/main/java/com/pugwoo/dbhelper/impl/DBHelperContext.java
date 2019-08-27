package com.pugwoo.dbhelper.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程上下文，支持自定义表名等功能
 */
public class DBHelperContext {

    /**自定义表名，适合于分表场景*/
    private static ThreadLocal<Map<Class<?>, String>> _tableNames = new ThreadLocal<Map<Class<?>, String>>();

    /**
     * 获得类对应的自定义表名，不存在返回null
     * @param clazz
     * @return
     */
    public static String getTableName(Class<?> clazz) {
        Map<Class<?>, String> tableNames = _tableNames.get();
        if(tableNames == null) {
            return null;
        }

        String tableName = tableNames.get(clazz);
        if(tableName == null) {
            return null;
        }
        return tableName;
    }

    /**
     * 设置类对应的自定义表名
     * @param clazz
     * @param tableName 不需要加反引号`
     */
    public static void setTableName(Class<?> clazz, String tableName) {
        if(clazz == null || tableName == null) {
            return;
        }
        tableName = tableName.trim();
        if(tableName.isEmpty()) {
            return;
        }

        Map<Class<?>, String> tableNames = _tableNames.get();
        if(tableNames == null) {
            tableNames = new HashMap<Class<?>, String>();
            _tableNames.set(tableNames);
        }
        tableNames.put(clazz, tableName);
    }

    /**
     * 清空自定义表名
     */
    public static void resetTableName() {
        _tableNames.set(null);
    }

}
