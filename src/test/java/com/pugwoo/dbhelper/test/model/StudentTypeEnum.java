package com.pugwoo.dbhelper.test.model;

/**
 * 学生类型枚举
 * 
 * 注意：不支持枚举作为DO的成员变量
 */
public enum StudentTypeEnum {

	undergraduate("undergraduate", "本科生"),
	postgraduate("postgraduate", "研究所");
	
	private String code;
	
	private String name;
	
	private StudentTypeEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public static StudentTypeEnum getByCode(String code) {
		for(StudentTypeEnum e : StudentTypeEnum.values()) {
			if(code == e.getCode() || code != null && code.equals(e.getCode())) {
				return e;
			}
		}
		return null;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
