package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.sun.org.apache.xpath.internal.operations.Bool;
import jdk.nashorn.internal.ir.AccessNode;

/**
 * 用于测速软删除时，设置软删除的deleted字段值为id的软删除字段
 */
@Table("t_student")
public class StudentDeleteSetIdDO {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;

	/**这一行的值id是关键*/
    @Column(value = "deleted", softDelete = {"0", "id"})
	private Boolean deleted;
	
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

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
}
