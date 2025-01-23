package com.pugwoo.dbhelper.impl;

import com.pugwoo.dbhelper.impl.part.P0_JdbcTemplateOp;
import com.pugwoo.dbhelper.model.RunningSqlData;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DBHelper线程上下文，支持: <br>
 * 1. 自定义表名<br>
 * 2. 线程级别的SQL注释
 */
public class DBHelperContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBHelperContext.class);

    /**自定义表名，适合于分表场景*/
    private static final ThreadLocal<Map<Class<?>, String>> tableNames = new ThreadLocal<>();

    /**全局的SQL注释*/
    private static String globalComment;
    /**线程上下文注释*/
    private static final ThreadLocal<String> comment = new ThreadLocal<>();

    /**是否开启缓存，默认开启*/
    private static boolean isCacheEnabled = true;

    /**存放类对应的插入数据默认值的集合，以集合为单位提供给Table注解使用<br>
     * 内置的集合有：<br>
     * 1) clickhouse 适用于clickhouse数据库<br>
     */
    private static final Map<String, Map<Class<?>, Object>> insertDefaultValueMap = new ConcurrentHashMap<>();

    /*初始化类对应的默认值*/
    static {
        {
            Map<Class<?>, Object> clickhouse = defaultValueMap();
            insertDefaultValueMap.put("clickhouse", clickhouse);
        }
        {
            Map<Class<?>, Object> mysql = defaultValueMap();
            insertDefaultValueMap.put("mysql", mysql);
        }
    }

    private static Map<Class<?>, Object> defaultValueMap() {
        Map<Class<?>, Object> map = new ConcurrentHashMap<>();
        map.put(Integer.class, 0);
        map.put(Long.class, 0L);
        map.put(Float.class, 0.0f);
        map.put(Double.class, 0.0d);
        map.put(Boolean.class, false);
        map.put(String.class, "");
        map.put(java.util.Date.class, new java.util.Date(0));
        map.put(java.sql.Date.class, new java.sql.Date(0));
        map.put(java.sql.Timestamp.class, new java.sql.Timestamp(0));
        map.put(java.sql.Time.class, new java.sql.Time(0));
        map.put(BigDecimal.class, new BigDecimal(0));
        map.put(BigInteger.class, new BigInteger("0"));
        map.put(LocalDate.class, LocalDate.of(1970, 1, 1));
        map.put(LocalDateTime.class, LocalDateTime.of(1970, 1, 1, 0, 0, 0));
        map.put(LocalTime.class, LocalTime.of(0, 0, 0));
        return map;
    }

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

    /**全局开启缓存*/
    public static synchronized void enableCache() {
        isCacheEnabled = true;
    }

    public static synchronized void disableCache() {
        isCacheEnabled = false;
    }

    public static boolean isCacheEnabled() {
        return isCacheEnabled;
    }

    public static Map<Class<?>, Object> getInsertDefaultValueMap(String insertDefaultValueMapName) {
        if (InnerCommonUtils.isBlank(insertDefaultValueMapName)) {
            return null;
        }
        Map<Class<?>, Object> map = insertDefaultValueMap.get(insertDefaultValueMapName);
        if (map == null) {
            LOGGER.error("getInsertDefaultValueMap is not exist, insertDefaultValueMapName:{}", insertDefaultValueMapName);
        }
        return map;
    }

    /**
     * 设置插入时的默认值
     * @param insertDefaultValueMapName 插入默认值的集合名称
     * @param clazz 类的类型
     * @param value 默认值
     */
    public static void setInsertDefaultValue(String insertDefaultValueMapName, Class<?> clazz, Object value) {
        if (InnerCommonUtils.isBlank(insertDefaultValueMapName)) {
            LOGGER.error("setInsertDefaultValue insertDefaultValueMapName is blank, ignore set");
            return;
        }
        if (clazz == null) {
            LOGGER.error("setInsertDefaultValue clazz is null, ignore set");
            return;
        }

        Map<Class<?>, Object> map = insertDefaultValueMap.get(insertDefaultValueMapName);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            insertDefaultValueMap.put(insertDefaultValueMapName, map);
        }
        map.put(clazz, value);
    }

    /**
     * 获取正在运行的SQL
     */
    public static Collection<RunningSqlData> getRunningSql() {
        return P0_JdbcTemplateOp.runningSqlMap.values();
    }

}
