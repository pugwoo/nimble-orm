package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.*;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import lombok.Data;

@Data
@JoinTable(joinType = JoinTypeEnum.LEFT_JOIN, on = "t1.school_id=t2.id")
public class StudentSchoolJoinVO {

	@Data
	public static class StudentVO extends StudentDO {
		// 特别注意：计算列的computed中的列要加上表的别称，例如t1.
		@Column(value = "nameWithHi", computed = "CONCAT(t1.name,'hi')")
		private String nameWithHi;
	}


	@RelatedColumn(localColumn = "t1.id", remoteColumn = "t1.id")
	private InnerStudentSchoolJoinVO vo2;

	@RelatedColumn(localColumn = "t1.id,t2.id", remoteColumn = "t1.id,t2.id")
	private InnerStudentSchoolJoinVO vo3;

	@RelatedColumn(localColumn = "t1.school_id", remoteColumn = "id")
	private SchoolDO schoolDO2;

	@RelatedColumn(localColumn = "t2.id", remoteColumn = "id")
	private SchoolDO schoolDO3;


	@JoinLeftTable(forceIndex = "PRIMARY") // forceIndex仅测试，实际上这个例子中不需要
	private StudentVO studentDO;
	
	@JoinRightTable(forceIndex = "PRIMARY") // forceIndex仅测试，实际上这个例子中不需要
	private SchoolDO schoolDO;

	// 这个仅用来测试RelatedColumn的远程类是JoinTable的情况
	@Data
	@JoinTable(joinType = JoinTypeEnum.LEFT_JOIN, on = "t1.school_id=t2.id")
	public static class InnerStudentSchoolJoinVO {
		@JoinLeftTable
		private StudentVO studentDO;

		@JoinRightTable
		private SchoolDO schoolDO;
	}

}
