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
	
	// 不支持枚举
//	@Column("type")
//	private StudentTypeEnum type;
	
	@Column("age")
	private Integer age;
	
	@Column("num")
	private byte[] num;
	
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

	public byte[] getNum() {
		return num;
	}

	public void setNum(byte[] num) {
		this.num = num;
	}

//	public StudentTypeEnum getType() {
//		return type;
//	}
//
//	public void setType(StudentTypeEnum type) {
//		this.type = type;
//	}

}
