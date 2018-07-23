package com.pugwoo.dbhelper.enums;

/**
 * 两表join的方式枚举
 * @author pugwoo
 */
public enum JoinTypeEnum {

	/**默认join*/
	JOIN("join", "default join"),
	/**left join左连接*/
	LEFT_JOIN("left join", "left join"),
	/**right join右连接*/
	RIGHT_JOIN("right join", "right join");
	
	private String code;
	
	private String name;
	
	JoinTypeEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public static JoinTypeEnum getByCode(String code) {
		for(JoinTypeEnum e : JoinTypeEnum.values()) {
			if(code == null && e.getCode() ==null || code != null && code.equals(e.getCode())) {
				return e;
			}
		}
		return null;
	}
	
	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

}
