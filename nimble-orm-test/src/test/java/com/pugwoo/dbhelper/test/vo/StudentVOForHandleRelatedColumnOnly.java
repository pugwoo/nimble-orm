package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.service.GetCourseByStudentIdServiceImpl;

import java.util.List;

/**
 * 这个例子演示了单独使用@RelatedColumn。
 * 这个类并没有@Table的注解，但是可以使用dbhelper提供的handleRelatedColumn方法查询数据，但要自行设置对应的@Column的值
 */
public class StudentVOForHandleRelatedColumnOnly {
	
	@Column(value = "id")
	private Long id;
	
	@Column("school_id")
	private Long schoolId;
	
	@RelatedColumn(localColumn = "school_id", remoteColumn = "id")
	private SchoolDO schoolDO;
	
	// @RelatedColumn(localColumn = "id", remoteColumn = "student_id")
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id", /*一定要写remoteColumn*/
			dataService = GetCourseByStudentIdServiceImpl.class)
	private List<CourseDO> courses;
	
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id", extraWhere = "where is_main=1")
	private List<CourseDO> mainCourses;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public SchoolDO getSchoolDO() {
		return schoolDO;
	}

	public void setSchoolDO(SchoolDO schoolDO) {
		this.schoolDO = schoolDO;
	}

	public List<CourseDO> getCourses() {
		return courses;
	}

	public void setCourses(List<CourseDO> courses) {
		this.courses = courses;
	}

	public List<CourseDO> getMainCourses() {
		return mainCourses;
	}

	public void setMainCourses(List<CourseDO> mainCourses) {
		this.mainCourses = mainCourses;
	}
	
}
