package com.pugwoo.dbhelper.test;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.vo.StudentDeadLoopVO;

/**
 * 该测试用例演示死循环的例子
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDeadLoop {
	
	private String getRandomName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}
	
	private StudentDO insertOne() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName(getRandomName("nick"));
		studentDO.setIntro(studentDO.getName().getBytes());
		dbHelper.insert(studentDO);
		return studentDO;
	}

	@Test
	public void testDeadLoop() {
		StudentDO studentDO = insertOne();
		StudentDeadLoopVO db = dbHelper.getByKey(StudentDeadLoopVO.class, studentDO.getId());
		System.out.println(db);
	}
		
	@Autowired
	private DBHelper dbHelper;
}
