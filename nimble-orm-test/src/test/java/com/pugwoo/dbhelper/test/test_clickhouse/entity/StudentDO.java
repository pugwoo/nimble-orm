package com.pugwoo.dbhelper.test.test_clickhouse.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;

import java.util.Date;
import java.util.List;

/**
 * 2015年1月12日 15:20:09 这个是有注解的DO
 */
@Table("t_student")
public class StudentDO {

	@Column(value = "id", isKey = true)
	private Long id;

	@Column(value = "deleted", softDelete = {"0", "1"})
	private Boolean deleted;

	@Column(value = "create_time", setTimeWhenInsert = true)
	private Date createTime;

	@Column(value = "update_time", setTimeWhenUpdate = true, setTimeWhenInsert = true)
	private Date updateTime;

	@Column(value = "delete_time", setTimeWhenDelete = true)
	private Date deleteTime;
		
	@Column(value = "name", maxStringLength = 32)
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

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
