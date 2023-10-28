package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 支持继承的方式
 */
@Data
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
	private LocalDateTime updateTime;

	@Column(value = "delete_time", setTimeWhenDelete = true)
	private LocalDateTime deleteTime;

}
