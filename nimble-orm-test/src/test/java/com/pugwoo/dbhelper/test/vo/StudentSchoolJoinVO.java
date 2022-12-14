package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;

@JoinTable(joinType = JoinTypeEnum.LEFT_JOIN, on = "t1.school_id=t2.id")
public class StudentSchoolJoinVO {

	public static class StudentVO extends StudentDO {

		// 特别注意：计算列的value和computed中的列都要加上表的别称，例如t1.
		@Column(value = "t1.nameWithHi", computed = "CONCAT(t1.name,'hi')")
		private String nameWithHi;

		public String getNameWithHi() {
			return nameWithHi;
		}

		public void setNameWithHi(String nameWithHi) {
			this.nameWithHi = nameWithHi;
		}
	}

	@JoinLeftTable
	private StudentVO studentDO;
	
	@JoinRightTable
	private SchoolDO schoolDO;

	public StudentVO getStudentDO() {
		return studentDO;
	}

	public void setStudentDO(StudentVO studentDO) {
		this.studentDO = studentDO;
	}

	public SchoolDO getSchoolDO() {
		return schoolDO;
	}

	public void setSchoolDO(SchoolDO schoolDO) {
		this.schoolDO = schoolDO;
	}
	
}
