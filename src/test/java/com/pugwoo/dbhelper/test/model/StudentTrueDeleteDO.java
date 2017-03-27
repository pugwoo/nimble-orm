package com.pugwoo.dbhelper.test.model;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

/**
 * 专门测试物理删除的DO
 */
@Table("t_student")
public class StudentTrueDeleteDO {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;
	
	@Column("name")
	private String name;

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
	
}
