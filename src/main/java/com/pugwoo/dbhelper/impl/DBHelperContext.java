package com.pugwoo.dbhelper.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程上下文，支持自定义表名等功能
 */
public class DBHelperContext {

    /**自定义表名，适合于分表场景*/
    private static ThreadLocal<Map<Class<?>, String>> _tableNames = new ThreadLocal<Map<Class<?>, String>>();

    /**关闭软删除*/
    private static ThreadLocal<Map<Class<?>, Boolean>> _turnOffSoftDelete = new ThreadLocal<Map<Class<?>, Boolean>>();

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

    /**
     * 查询指定类是否关闭了软删除
     * @param clazz
     * @return
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
     * @param clazz
     */
    public static void turnOffSoftDelete(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Map<Class<?>, Boolean> turnoff = _turnOffSoftDelete.get();
        if(turnoff == null) {
            turnoff = new HashMap<Class<?>, Boolean>();
            _turnOffSoftDelete.set(turnoff);
        }
        turnoff.put(clazz, true);
    }

    /**
     * 打开指定类的软删除设置
     * @param clazz
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
