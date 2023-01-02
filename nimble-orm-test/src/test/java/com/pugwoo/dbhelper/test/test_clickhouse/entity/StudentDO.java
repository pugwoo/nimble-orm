package com.pugwoo.dbhelper.test.test_clickhouse.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("t_student")
public class StudentDO {

	@Column(value = "id", isKey = true)
	private Long id;

	@Column(value = "deleted", softDelete = {"0", "1"})
	private Boolean deleted;

	@Column(value = "create_time", setTimeWhenInsert = true)
	private LocalDateTime createTime;

	@Column(value = "update_time", setTimeWhenUpdate = true, setTimeWhenInsert = true)
	private LocalDateTime updateTime;

	@Column(value = "delete_time", setTimeWhenDelete = true)
	private LocalDateTime deleteTime;
		
	@Column(value = "name")
	private String name;

	@Column(value = "age", insertValueScript = "0")
	private Integer age;
	
	@Column("intro") // ck不支持byte[]类型 TODO 看看怎么处理
	private byte[] intro;
	
	@Column("school_id")
	private Long schoolId;
	
	@Column(value = "school_snapshot", isJSON = true)
	private SchoolDO schoolSnapshot;
	
	@Column(value = "course_snapshot", isJSON = true)
	private List<CourseDO> courseSnapshot;
}
