package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

/**
 * 用于测速软删除时，设置软删除的deleted字段值为id的软删除字段
 */
@Data
@Table("t_student")
public class StudentDeleteSetIdDO {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;

	/**这一行的值id是关键*/
    @Column(value = "deleted", softDelete = {"0", "id"})
	private Boolean deleted;
	
	@Column("name")
	private String name;

}
