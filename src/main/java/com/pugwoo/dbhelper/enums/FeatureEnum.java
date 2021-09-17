package com.pugwoo.dbhelper.enums;

/**
 * 特性枚举
 */
public enum FeatureEnum {

    /**
     * 如果计算列是sum()函数，那么则将sum函数包一层COALESCE(SUM(column),0)，将null值转成0.
     *
     * 默认开启
     */
    AUTO_SUM_COALESCE_TO_ZERO
    ;

}