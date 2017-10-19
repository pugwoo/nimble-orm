package com.pugwoo.dbhelper.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc-interceptor.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelperInterceptor {

	@Autowired
	private DBHelper dbHelper;
	
	@Test
	public void test() {
		StudentDO studentDO = new StudentDO();
		studentDO.setId(123L);
		dbHelper.getByKey(studentDO);
		System.out.println(studentDO);
		
		dbHelper.getByKey(StudentDO.class, 123);
		dbHelper.getAll(StudentDO.class);
	}
}
