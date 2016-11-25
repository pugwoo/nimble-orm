package com.pugwoo.dbhelper.test.model;

import com.pugwoo.dbhelper.annotation.Column;

/**
 * 支持继承的方式
 */
public class IdableSoftDeleteBaseDO {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;
	
	/**
	 * 软删除标记为，0 未删除，1已删除
	 */
	@Column(value = "deleted", softDelete = {"0", "1"})
	private Boolean deleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
