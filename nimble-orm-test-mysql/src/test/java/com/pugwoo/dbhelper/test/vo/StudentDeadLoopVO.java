package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.StudentDO;

public class StudentDeadLoopVO extends StudentDO {

	@RelatedColumn(localColumn = "id", remoteColumn = "id")
	private StudentDeadLoopVO deadLoopVO;
	
	
	
}
