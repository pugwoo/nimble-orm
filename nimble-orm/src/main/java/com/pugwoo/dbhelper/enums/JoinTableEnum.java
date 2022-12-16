package com.pugwoo.dbhelper.enums;

/**
 * joinTable的表枚举，左表还是右表
 * @author pugwoo
 */
public enum JoinTableEnum {

	/**默认值，不启用*/
	NOT_USE("NOT_USE", "NOT_USE"),

	/**join左表*/
	LEFT("LEFT", "Join Left Table"),
	/**join右表*/
	RIGHT("RIGHT", "Join Right Table")

	;

	private final String code;

	private final String name;

	JoinTableEnum(String code, String name) {
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
