package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.test.model.SchoolDO;
import com.pugwoo.dbhelper.test.model.StudentDO;

@JoinTable(joinType = "join", on = "t1.school_id=t2.id")
public class StudentSchoolJoinVO {

	@JoinLeftTable
	private StudentDO studentDO;
	
	@JoinRightTable
	private SchoolDO schoolDO;

	public StudentDO getStudentDO() {
		return studentDO;
	}

	public void setStudentDO(StudentDO studentDO) {
		this.studentDO = studentDO;
	}

	public SchoolDO getSchoolDO() {
		return schoolDO;
	}

	public void setSchoolDO(SchoolDO schoolDO) {
		this.schoolDO = schoolDO;
	}
	
}
