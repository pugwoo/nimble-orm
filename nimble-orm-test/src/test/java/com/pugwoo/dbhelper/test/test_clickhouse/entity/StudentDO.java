package com.pugwoo.dbhelper.test.test_clickhouse.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Table(value = "t_student", insertDefaultValueMap = "clickhouse")
public class StudentDO {

	@Column(value = "id", isKey = true)
	private Long id;

	@Column(value = "deleted", softDelete = {"0", "1"})
	private Boolean deleted;

	@Column(value = "create_time") // 不要setTimeWhenUpdate/setTimeWhenInsert是为了测试表默认值
	private LocalDateTime createTime;

	@Column(value = "update_time") // 不要setTimeWhenUpdate/setTimeWhenInsert是为了测试表默认值
	private LocalDate updateTime;

	@Column(value = "delete_time")
	private LocalTime deleteTime;
		
	@Column(value = "name")
	private String name;

	@Column(value = "age")
	private Integer age;
	
	@Column("intro") // ck不支持byte[]类型
	private String intro;
	
	@Column("school_id")
	private Long schoolId;

	// clickhouse的json的null->默认值要自己处理
//	@Column(value = "school_snapshot", isJSON = true)
//	private SchoolDO schoolSnapshot;

	// clickhouse的json的null->默认值要自己处理
//	@Column(value = "course_snapshot", isJSON = true)
//	private List<CourseDO> courseSnapshot;

}
