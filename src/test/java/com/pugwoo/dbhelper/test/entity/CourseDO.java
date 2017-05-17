package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Table("t_course")
public class CourseDO extends IdableSoftDeleteBaseDO {

	@Column("student_id")
	private Long studentId;
	
	@Column("name")
	private String name;
	
	// 是否主课程
	@Column("is_main")
	private Boolean isMain;

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsMain() {
		return isMain;
	}

	public void setIsMain(Boolean isMain) {
		this.isMain = isMain;
	}
	
}
