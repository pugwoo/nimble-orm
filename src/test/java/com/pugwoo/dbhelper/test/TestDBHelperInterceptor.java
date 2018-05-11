package com.pugwoo.dbhelper.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
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
	
	private String getRandomName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}
	
	private List<StudentDO> insertBatch(int num) {
		List<StudentDO> list = new ArrayList<StudentDO>();
		for(int i = 0; i < num; i++) {
			StudentDO studentDO = new StudentDO();
			studentDO.setName(getRandomName("nick"));
			list.add(studentDO);
		}
		
		int rows = dbHelper.insert(list);
		Assert.assertTrue(rows == num);
		
		return list;
	}
	
	@Test @Rollback(false)
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
		List<Long> keys = new ArrayList<Long>();
		keys.add(id);
		dbHelper.getByKeyList(StudentDO.class, keys);
		
		dbHelper.getAll(StudentDO.class);
		dbHelper.getPage(StudentDO.class, 1, 10);
		dbHelper.getOne(StudentDO.class);
	}
	
	@Test @Rollback(false)
	public void testInsertUpdate() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		Long id = studentDO.getId();
		
		studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insertWhereNotExist(studentDO, "where id=?", id);
		
		studentDO = new StudentDO();
		studentDO.setId(id);
		studentDO.setName("karen");
		dbHelper.update(studentDO);
		
		studentDO.setName("karennick");
		dbHelper.update(studentDO, "where name=?", "karen");
		
		dbHelper.updateCustom(studentDO, "age=age+1");
		dbHelper.updateAll(StudentDO.class, "name=?", "", "nick");
	}
	
	@Test @Rollback(false)
	public void testDelete() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		Long id = studentDO.getId();
		
		dbHelper.deleteByKey(StudentDO.class, id);
		dbHelper.delete(StudentDO.class, "where id > ?", 100);
	}
	
	@Test @Rollback(false)
	public void batchDelete() {
		List<StudentDO> insertBatch = insertBatch(10);
		int rows = dbHelper.deleteByKey(insertBatch);
		Assert.assertTrue(rows == insertBatch.size());
		
		insertBatch = insertBatch(20);
		rows = dbHelper.delete(StudentDO.class, "where 1=?", 1);
		Assert.assertTrue(rows >= 20);
	}
	
	@Test @Rollback(false)
	public void testCustomsUpdateDelete() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		
		dbHelper.updateCustom(studentDO, "age=age+1");
		dbHelper.delete(StudentDO.class, "where 1=1");
		
		studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		dbHelper.updateAll(StudentDO.class, "age=age+1", "where 1=1");
	}
}
