package com.pugwoo.dbhelper.enums;

/**
 * 特性枚举
 */
public enum FeatureEnum {

    /**
     * 如果计算列是sum()函数，那么则将sum函数包一层COALESCE(SUM(column),0)，将null值转成0.
     * 默认[开启]
     */
    AUTO_SUM_NULL_TO_ZERO,

    /**
     * 以info的级别log SQL，默认[关闭]，默认时用debug级别打印sql
     */
    LOG_SQL_AT_INFO_LEVEL,

    /**
     * 当DO类@Column注解的列不在数据库返回的列中时，是否抛出异常，默认[关闭]，即不抛出异常
     */
    THROW_EXCEPTION_IF_COLUMN_NOT_EXIST,

    /**
     * 分页是否自动加上order by子句，默认[开启]；分页强烈建议加上order by排序，否则分页结果可能不正确
     */
    AUTO_ADD_ORDER_FOR_PAGINATION,

}