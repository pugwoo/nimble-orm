package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Table("t_school")
public class SchoolDO extends IdableSoftDeleteBaseDO {

	@Column("name")
	private String name;
	
}
