package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.model.SchoolDO;
import com.pugwoo.dbhelper.test.model.StudentDO;

/**
 * 关联上schoolDO
 * @author NICK
 */
public class StudentVO extends StudentDO {
	
	@RelatedColumn("school_id")
	private SchoolDO schoolDO;

	public SchoolDO getSchoolDO() {
		return schoolDO;
	}

	public void setSchoolDO(SchoolDO schoolDO) {
		this.schoolDO = schoolDO;
	}
	
}
