package com.pugwoo.dbhelper.enums;

/**
 * 默认值启用条件枚举
 */
public enum ValueConditionEnum {

    /**
     * 当值为null时
     */
    WHEN_NULL("WHEN_NULL", "当值为null时"),

    /**
     * 当值为empty时(仅String有效)
     */
    WHEN_EMPTY("WHEN_EMPTY", "当值为empty时(仅String有效)"),

    /**
     * 当值为blank时(仅String有效)
     */
    WHEN_BLANK("WHEN_BLANK", "当值为blank时(仅String有效)"),

    ;

    final private String code;

    final private String name;

    ValueConditionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}