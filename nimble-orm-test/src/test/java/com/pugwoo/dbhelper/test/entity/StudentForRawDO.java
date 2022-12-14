package com.pugwoo.dbhelper.test.entity;



import com.pugwoo.dbhelper.annotation.Column;

/**
 * 2020年12月16日 17:31:23 用于查询自定义SQL
 */
public class StudentForRawDO {

	@Column("id")
	private Long id;

	@Column(value = "name")
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
