package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.test.model.StudentDO;

public class TestSQLUtils {

	public static void main(String[] args) {
		System.out.println(SQLUtils.getSelectSQL(StudentDO.class));
	}
	
}
