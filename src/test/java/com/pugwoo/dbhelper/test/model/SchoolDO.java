package com.pugwoo.dbhelper.test.model;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Table("t_school")
public class SchoolDO extends IdableSoftDeleteBaseDO {

	@Column("name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
