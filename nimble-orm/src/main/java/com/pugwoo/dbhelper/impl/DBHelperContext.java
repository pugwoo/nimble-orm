package com.pugwoo.dbhelper.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * DBHelper线程上下文，支持: <br>
 * 1. 自定义表名<br>
 * 2. 开启和关闭指定DO类的软删除<br>
 * 3. 线程级别的SQL注释
 */
public class DBHelperContext {

    /**自定义表名，适合于分表场景*/
    private static final ThreadLocal<Map<Class<?>, String>> tableNames = new ThreadLocal<>();

    /**关闭软删除*/
    private static final ThreadLocal<Map<Class<?>, Boolean>> turnOffSoftDelete = new ThreadLocal<>();

    /**全局的SQL注释*/
    private static String globalComment;
    /**线程上下文注释*/
    private static final ThreadLocal<String> comment = new ThreadLocal<>();

    /**
     * 动态获得类对应的自定义表名，不存在返回null
     */
    public static String getTableName(Class<?> clazz) {
        Map<Class<?>, String> tableNames = DBHelperContext.tableNames.get();
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

        Map<Class<?>, String> tableNames = DBHelperContext.tableNames.get();
        if(tableNames == null) {
            tableNames = new HashMap<>();
            DBHelperContext.tableNames.set(tableNames);
        }
        tableNames.put(clazz, tableName);
    }

    /**
     * 清空自定义表名
     */
    public static void resetTableName() {
        tableNames.set(null);
    }

    /**
     * 查询指定类是否关闭了软删除
     */
    public static boolean isTurnOffSoftDelete(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        Map<Class<?>, Boolean> turnoff = turnOffSoftDelete.get();
        if(turnoff == null) {
            return false;
        }

        Boolean b = turnoff.get(clazz);
        return b != null && b;
    }

    /**
     * 关闭指定类的软删除设置，仅对当前线程有效
     */
    public static void turnOffSoftDelete(Class<?> ...clazz) {
        if (clazz == null) {
            return;
        }
        for (Class<?> c : clazz) {
            turnOffSoftDelete(c);
        }
    }

    private static void turnOffSoftDelete(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Map<Class<?>, Boolean> turnoff = turnOffSoftDelete.get();
        if(turnoff == null) {
            turnoff = new HashMap<>();
            turnOffSoftDelete.set(turnoff);
        }
        turnoff.put(clazz, true);
    }

    /**
     * 打开指定类的软删除设置
     */
    public static void turnOnSoftDelete(Class<?> ...clazz) {
        if (clazz == null) {
            return;
        }
        for (Class<?> c : clazz) {
            turnOnSoftDelete(c);
        }
    }

    private static void turnOnSoftDelete(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Map<Class<?>, Boolean> turnoff = turnOffSoftDelete.get();
        if(turnoff == null) {
            return;
        }
        turnoff.remove(clazz);
    }

    public static void setGlobalComment(String comment) {
        DBHelperContext.globalComment = comment;
    }

    public static String getGlobalComment() {
        return DBHelperContext.globalComment;
    }

    /**
     * 设置线程上下文的SQL注释
     */
    public static void setThreadLocalComment(String comment) {
        DBHelperContext.comment.set(comment);
    }

    /**
     * 获取线程上下文的SQL注释
     */
    public static String getThreadLocalComment() {
        return DBHelperContext.comment.get();
    }

}
