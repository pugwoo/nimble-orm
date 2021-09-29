package com.pugwoo.dbhelper.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程上下文，支持自定义表名等功能
 */
public class DBHelperContext {

    /**自定义表名，适合于分表场景*/
    private static final ThreadLocal<Map<Class<?>, String>> _tableNames = new ThreadLocal<>();

    /**关闭软删除*/
    private static final ThreadLocal<Map<Class<?>, Boolean>> _turnOffSoftDelete = new ThreadLocal<>();

    /**
     * 获得类对应的自定义表名，不存在返回null
     */
    public static String getTableName(Class<?> clazz) {
        Map<Class<?>, String> tableNames = _tableNames.get();
        if(tableNames == null) {
            return null;
        }

        return tableNames.get(clazz);
    }

    /**
     * 设置类对应的自定义表名
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
            tableNames = new HashMap<>();
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

    /**
     * 查询指定类是否关闭了软删除
     */
    public static boolean isTurnOffSoftDelete(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        Map<Class<?>, Boolean> turnoff = _turnOffSoftDelete.get();
        if(turnoff == null) {
            return false;
        }

        Boolean b = turnoff.get(clazz);
        return b != null && b;
    }

    /**
     * 关闭指定类的软删除设置，仅对当前线程有效
     */
    public static void turnOffSoftDelete(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Map<Class<?>, Boolean> turnoff = _turnOffSoftDelete.get();
        if(turnoff == null) {
            turnoff = new HashMap<>();
            _turnOffSoftDelete.set(turnoff);
        }
        turnoff.put(clazz, true);
    }

    /**
     * 打开指定类的软删除设置
     */
    public static void turnOnSoftDelete(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Map<Class<?>, Boolean> turnoff = _turnOffSoftDelete.get();
        if(turnoff == null) {
            return;
        }
        turnoff.remove(clazz);
    }

}
