package com.pugwoo.dbhelper.test.vo;

import java.util.List;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.model.CourseDO;
import com.pugwoo.dbhelper.test.model.SchoolDO;
import com.pugwoo.dbhelper.test.model.StudentDO;
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
	
}
