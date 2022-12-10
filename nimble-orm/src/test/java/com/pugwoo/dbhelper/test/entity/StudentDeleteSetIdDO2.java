package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

/**
 * 用于测速软删除时，设置软删除的deleted字段值为id的软删除字段
 */
@Table("t_student")
public class StudentDeleteSetIdDO2 {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;

	/**这个是用于验证的*/
    @Column(value = "deleted")
	private Long deleted;
	
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

	public Long getDeleted() {
		return deleted;
	}

	public void setDeleted(Long deleted) {
		this.deleted = deleted;
	}
}
