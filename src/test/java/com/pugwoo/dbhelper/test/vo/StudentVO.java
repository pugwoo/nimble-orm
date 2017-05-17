package com.pugwoo.dbhelper.test.vo;

import java.util.List;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.service.IGetCourseByStudentIdDataService;

/**
 * 关联上schoolDO
 * @author NICK
 */
public class StudentVO extends StudentDO {
	
	@RelatedColumn(value = "school_id", remoteColumn = "id")
	private SchoolDO schoolDO;
	
	// @RelatedColumn(value = "id", remoteColumn = "student_id")
	@RelatedColumn(value = "id", remoteColumn = "student_id", /*一定要写remoteColumn*/
			dataService = IGetCourseByStudentIdDataService.class)
	private List<CourseDO> courses;
	
	@RelatedColumn(value = "id", remoteColumn = "student_id", extraWhere = "where is_main=1")
	private List<CourseDO> mainCourses;

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
