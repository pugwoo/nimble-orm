package com.pugwoo.dbhelper.enums;

import java.util.Objects;

/**
 * 数据库类型枚举值
 */
public enum DatabaseTypeEnum {

    UNKNOWN("UNKNOWN", "Unknown"),

    MYSQL("MYSQL", "MySQL"),

    CLICKHOUSE("CLICKHOUSE", "ClickHouse"),

    POSTGRESQL("POSTGRESQL", "Postgresql"),

    ;

    final private String code;
    final private String name;

    DatabaseTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DatabaseTypeEnum getByJdbcProtocol(String jdbcProtocol) {
        if ("mysql".equalsIgnoreCase(jdbcProtocol)) {
            return MYSQL;
        } else if ("clickhouse".equalsIgnoreCase(jdbcProtocol) || "ch".equalsIgnoreCase(jdbcProtocol)) {
            return CLICKHOUSE;
        } else if ("postgresql".equalsIgnoreCase(jdbcProtocol) || "pgsql".equalsIgnoreCase(jdbcProtocol)) {
            return POSTGRESQL;
        }
        return UNKNOWN;
    }

    public static DatabaseTypeEnum getByCode(String code) {
        for (DatabaseTypeEnum e : DatabaseTypeEnum.values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return UNKNOWN;
    }

    public static String getNameByCode(String code) {
        DatabaseTypeEnum e = getByCode(code);
        return e == null ? "" : e.getName();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}