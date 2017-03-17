package com.pugwoo.dbhelper.test.model;

import java.util.Date;

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
	
	@Column(value = "create_time", setTimeWhenInsert = true)
	private Date createTime;
	
	@Column(value = "update_time", setTimeWhenUpdate = true, setTimeWhenInsert = true)
	private Date updateTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
