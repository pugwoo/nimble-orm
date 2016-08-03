package com.pugwoo.dbhelper.test.model;

import com.pugwoo.dbhelper.annotation.Column;

/**
 * 支持继承的方式
 */
public class IdableBaseDO {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
