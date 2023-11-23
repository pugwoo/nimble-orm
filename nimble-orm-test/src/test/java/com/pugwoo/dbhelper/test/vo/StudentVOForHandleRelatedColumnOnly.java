package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import lombok.Data;

import java.util.List;

/**
 * 这个例子演示了单独使用@RelatedColumn。
 * 这个类并没有@Table的注解，但是可以使用dbhelper提供的handleRelatedColumn方法查询数据，但要自行设置对应的@Column的值
 */
@Data
public class StudentVOForHandleRelatedColumnOnly {
	
	@Column(value = "id")
	private Long id;
	
	@Column("school_id")
	private Long schoolId;
	
	@RelatedColumn(localColumn = "school_id", remoteColumn = "id")
	private SchoolDO schoolDO;
	
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id")
	private List<CourseDO> courses;
	
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id", extraWhere = "where is_main=true")
	private List<CourseDO> mainCourses;
	
}
