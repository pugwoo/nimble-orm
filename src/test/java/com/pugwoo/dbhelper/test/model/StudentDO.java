package com.pugwoo.dbhelper.test.model;



import java.util.Date;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

/**
 * 2015年1月12日 15:20:09 这个是有注解的DO
 */
@Table("t_student")
public class StudentDO extends IdableSoftDeleteBaseDO {
		
	@Column(value = "create_time", setTimeWhenInsert = true)
	private Date createTime;
	
	@Column("name")
	private String name;
	
	// 不支持枚举
//	@Column("type")
//	private StudentTypeEnum type;
	
	@Column("age")
	private Integer age;
	
//	@Column("num") // 支持byte[]类型
	private byte[] num;
	
	@Override
	public String toString() {
		return "id:" + getId() + ",name:" + name + ",age:" + age;
	};

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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	

//	public StudentTypeEnum getType() {
//		return type;
//	}
//
//	public void setType(StudentTypeEnum type) {
//		this.type = type;
//	}

}
