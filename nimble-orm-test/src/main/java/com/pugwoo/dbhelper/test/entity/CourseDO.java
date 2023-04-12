package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

@Data
@Table("t_course")
public class CourseDO extends IdableSoftDeleteBaseDO {

	@Column("student_id")
	private Long studentId;
	
	@Column("name")
	private String name;
	
	// 是否主课程
	@Column("is_main")
	private Boolean isMain;
	
}
