package com.pugwoo.dbhelper.test.ex_tests;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentDeadLoopVO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 该测试用例演示死循环的例子
 */
@SpringBootTest
public class TestDeadLoop {

	@Autowired
	private DBHelper dbHelper;

	@Test
	@EnabledIfSystemProperty(named = "runExceptionTest", matches = "true")
	public void testDeadLoop() {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		dbHelper.getByKey(StudentDeadLoopVO.class, studentDO.getId());
	}

}
