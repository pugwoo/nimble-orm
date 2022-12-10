package com.pugwoo.dbhelper.test.entity;



import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.util.List;

/**
 * 2015年1月12日 15:20:09 这个是有注解的DO
 */
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public byte[] getIntro() {
		return intro;
	}

	public void setIntro(byte[] intro) {
		this.intro = intro;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public SchoolDO getSchoolSnapshot() {
		return schoolSnapshot;
	}

	public void setSchoolSnapshot(SchoolDO schoolSnapshot) {
		this.schoolSnapshot = schoolSnapshot;
	}

	public List<CourseDO> getCourseSnapshot() {
		return courseSnapshot;
	}

	public void setCourseSnapshot(List<CourseDO> courseSnapshot) {
		this.courseSnapshot = courseSnapshot;
	}
	
}
