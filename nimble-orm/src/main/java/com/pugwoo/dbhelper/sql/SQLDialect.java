package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;

/**
 * 提供各种数据库不同的方言表达
 */
public class SQLDialect {

    /**
     * 获得insert into语句中表达数据库默认值的关键字
     */
    public static String getInsertDefaultValue(DatabaseTypeEnum databaseType) {
        if (databaseType == DatabaseTypeEnum.CLICKHOUSE) {
            return "null"; // 支持全版本的clickhouse，22.2版本及以上才支持default
        } else { // 默认和mysql
            return "default";
        }
    }

}
