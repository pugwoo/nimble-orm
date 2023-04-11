package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

/**
 * 专门测试物理删除的DO
 */
@Data
@Table("t_student")
public class StudentHardDeleteDO {

	@Column(value = "id", isKey = true, isAutoIncrement = true)
	private Long id;
	
	@Column("name")
	private String name;
	
}
