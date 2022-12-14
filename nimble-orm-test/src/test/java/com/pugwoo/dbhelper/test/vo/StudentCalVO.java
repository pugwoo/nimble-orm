package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.ExcludeInheritedColumn;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.service.IGetCourseByStudentIdDataService;

import java.util.List;

/**
 * 关联上schoolDO
 * @author NICK
 */
@ExcludeInheritedColumn
public class StudentCalVO extends StudentDO {
	
	@RelatedColumn(localColumn = "school_id", remoteColumn = "id")
	private SchoolDO schoolDO;
	
	// @RelatedColumn(localColumn = "id", remoteColumn = "student_id")
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id", /*一定要写remoteColumn*/
			dataService = IGetCourseByStudentIdDataService.class)
	private List<CourseDO> courses;
	
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id", extraWhere = "where is_main=1")
	private List<CourseDO> mainCourses;

	// extraWhere不加where关键字，不推荐
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id", extraWhere = "is_main=1")
	private List<CourseDO> mainCourses2;

	// extraWhere不加where关键字，不推荐
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id",
			extraWhere = "is_main=1 order by name")
	private List<CourseDO> mainCourses3;

	@RelatedColumn(localColumn = "id", remoteColumn = "student_id",
			extraWhere = "order by name")
	private List<CourseDO> mainCourses4;
	
	// 计算列示例，生成的select字段为：CONCAT(name,'hi') AS nameWithHi
	@Column(value = "nameWithHi", computed = "CONCAT(name,'hi')")
	private String nameWithHi;

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

	public String getNameWithHi() {
		return nameWithHi;
	}

	public void setNameWithHi(String nameWithHi) {
		this.nameWithHi = nameWithHi;
	}

	public List<CourseDO> getMainCourses2() {
		return mainCourses2;
	}

	public void setMainCourses2(List<CourseDO> mainCourses2) {
		this.mainCourses2 = mainCourses2;
	}

	public List<CourseDO> getMainCourses3() {
		return mainCourses3;
	}

	public void setMainCourses3(List<CourseDO> mainCourses3) {
		this.mainCourses3 = mainCourses3;
	}

	public List<CourseDO> getMainCourses4() {
		return mainCourses4;
	}

	public void setMainCourses4(List<CourseDO> mainCourses4) {
		this.mainCourses4 = mainCourses4;
	}
}
