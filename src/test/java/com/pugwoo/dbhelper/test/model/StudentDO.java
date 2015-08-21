package com.pugwoo.dbhelper.test.model;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

/**
 * 2015年1月12日 15:20:09 这个是有注解的DO
 */
@Table("t_student")
public class StudentDO {
	
	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;
	
	@Column("name")
	private String name;
	
	@Column("age")
	private Integer age;

	@Override
	public String toString() {
		return "id:" + id + ",name:" + name + ",age:" + age;
	};

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

}
