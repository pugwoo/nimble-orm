package com.pugwoo.dbhelper.enums;

/**
 * 两表join的方式枚举
 * <br>
 * 其它类型的join，请使用字符串形式的join，详见`@JoinTable`的joinTypeAsString属性
 *
 * @author pugwoo
 */
public enum JoinTypeEnum {

	/**默认join*/
	JOIN("JOIN", "default join"),
	/**left join左连接*/
	LEFT_JOIN("LEFT JOIN", "left join"),
	/**right join右连接*/
	RIGHT_JOIN("RIGHT JOIN", "right join")
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
