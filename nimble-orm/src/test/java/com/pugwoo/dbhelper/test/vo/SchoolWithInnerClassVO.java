package com.pugwoo.dbhelper.test.vo;

import java.util.List;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;

/**
 * 这里演示从学校的角度，拿这个学校的所有学生，以及学生的所有课程
 */
public class SchoolWithInnerClassVO extends SchoolDO {

	// 重点说明：内部类必须是public，否则dbhelper访问不到
	public static class StudentVO extends StudentDO {
		
		@RelatedColumn(localColumn = "id", remoteColumn = "student_id")
		private List<CourseDO> courses;

		public List<CourseDO> getCourses() {
			return courses;
		}
	}
	
	@RelatedColumn(localColumn = "id", remoteColumn = "school_id")
	private List<StudentVO> students;

	public List<StudentVO> getStudents() {
		return students;
	}
	
}
