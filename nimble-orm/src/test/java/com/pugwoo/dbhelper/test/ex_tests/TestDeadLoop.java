package com.pugwoo.dbhelper.test.ex_tests;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentDeadLoopVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 该测试用例演示死循环的例子
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestDeadLoop {

	@Autowired
	private DBHelper dbHelper;

	@Test
	public void testDeadLoop() {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		dbHelper.getByKey(StudentDeadLoopVO.class, studentDO.getId());
	}

}
