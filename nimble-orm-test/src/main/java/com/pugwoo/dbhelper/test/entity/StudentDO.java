package com.pugwoo.dbhelper.test.entity;



import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

import java.util.List;

/**
 * 2015年1月12日 15:20:09 这个是有注解的DO
 */
@Data
@Table("t_student")
public class StudentDO extends IdableSoftDeleteBaseDO {
		
	@Column(value = "name", maxStringLength = 32)
	private String name;
	
//  不支持枚举
//	@Column("type")
//	private StudentTypeEnum type;
	
	@Column(value = "age", insertValueScript = "0")
	private Integer age;
	
	@Column("intro") // 支持byte[]类型
	private byte[] intro;
	
	@Column("school_id")
	private Long schoolId;
	
	@Column(value = "school_snapshot", isJSON = true)
	private SchoolDO schoolSnapshot;
	
	@Column(value = "course_snapshot", isJSON = true)
	private List<CourseDO> courseSnapshot;
	
	@Override
	public String toString() {
		return "id:" + getId() + ",name:" + name + ",age:" + age;
	};

}
