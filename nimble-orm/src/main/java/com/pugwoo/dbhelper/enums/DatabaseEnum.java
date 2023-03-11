package com.pugwoo.dbhelper.enums;

import java.util.Objects;

/**
 * 数据库类型枚举值
 */
public enum DatabaseEnum {

    UNKNOWN("UNKNOWN", "Unknown"),

    MYSQL("MYSQL", "MySQL"),

    CLICKHOUSE("CLICKHOUSE", "ClickHouse"),
    ;

    final private String code;
    final private String name;

    DatabaseEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DatabaseEnum getByJdbcProtocol(String jdbcProtocol) {
        if ("mysql".equalsIgnoreCase(jdbcProtocol)) {
            return MYSQL;
        } else if ("clickhouse".equalsIgnoreCase(jdbcProtocol)
                || "ch".equalsIgnoreCase(jdbcProtocol)) {
            return CLICKHOUSE;
        }
        return UNKNOWN;
    }

    public static DatabaseEnum getByCode(String code) {
        for (DatabaseEnum e : DatabaseEnum.values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return UNKNOWN;
    }

    public static String getNameByCode(String code) {
        DatabaseEnum e = getByCode(code);
        return e == null ? "" : e.getName();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}