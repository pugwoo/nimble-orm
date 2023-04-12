package com.pugwoo.dbhelper.impl;

import com.pugwoo.dbhelper.utils.InnerCommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * DBHelper线程上下文，支持: <br>
 * 1. 自定义表名<br>
 * 2. 线程级别的SQL注释
 */
public class DBHelperContext {

    /**自定义表名，适合于分表场景*/
    private static final ThreadLocal<Map<Class<?>, String>> tableNames = new ThreadLocal<>();

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
     * @param tableName 不需要加反引号`，如果为null表示清除自定义表名
     */
    public static void setTableName(Class<?> clazz, String tableName) {
        if(clazz == null) {
            return;
        }
        if (tableName == null) { // 清除表名
            Map<Class<?>, String> tableNames = DBHelperContext.tableNames.get();
            if (tableNames != null) {
                tableNames.remove(clazz);
            }
        }

        if(InnerCommonUtils.isBlank(tableName)) { // 空字符串不允许，不处理
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
