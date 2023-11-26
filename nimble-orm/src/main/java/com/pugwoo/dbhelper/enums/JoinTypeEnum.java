package com.pugwoo.dbhelper.enums;

/**
 * 两表join的方式枚举
 * @author pugwoo
 */
public enum JoinTypeEnum {

	/**默认join*/
	JOIN("JOIN", "default join"),
	/**left join左连接*/
	LEFT_JOIN("LEFT JOIN", "left join"),
	/**right join右连接*/
	RIGHT_JOIN("RIGHT JOIN", "right join"),
    /**STRAIGHT_JOIN强制左表为驱动表进行join*/
	STRAIGHT_JOIN("STRAIGHT_JOIN", "straight join"),
    /**full outer join，适用于postgresql*/
    FULL_OUTER_JOIN("FULL OUTER JOIN", "full outer join"),
	;
	
	private final String code;
	
	private final String name;
	
	JoinTypeEnum(String code, String name) {
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
