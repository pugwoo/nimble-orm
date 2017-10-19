package com.pugwoo.dbhelper.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.wooutils.collect.ListUtils;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc-interceptor.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelperInterceptor {

	@Autowired
	private DBHelper dbHelper;
	
	@Test
	public void testQuery() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		Long id = studentDO.getId();
		
		studentDO = new StudentDO();
		studentDO.setId(id);
		dbHelper.getByKey(studentDO);
		dbHelper.getByKey(StudentDO.class, id);
		dbHelper.getByKeyList(StudentDO.class, ListUtils.newArrayList(id));
		
		dbHelper.getAll(StudentDO.class);
		dbHelper.getPage(StudentDO.class, 1, 10);
		dbHelper.getOne(StudentDO.class);
	}
}
