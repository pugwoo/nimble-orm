package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc-default-interceptor.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelperDefaultInterceptor {

	@Autowired
	private DBHelper dbHelper;

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
		StudentDO studentDO = CommonOps.insertOne(dbHelper);

		Long id = studentDO.getId();
		assert dbHelper.getByKey(StudentDO.class, id).getName().equals(studentDO.getName());
		
		assert dbHelper.deleteByKey(StudentDO.class, id) == 1;

		CommonOps.insertBatch(dbHelper, CommonOps.getRandomInt(101, 100));
		assert dbHelper.delete(StudentDO.class, "where id > ?", 100) > 0;

		studentDO = CommonOps.insertOne(dbHelper);

		assert dbHelper.getByKey(StudentDO.class, studentDO.getId())
				.getName().equals(studentDO.getName());
		studentDO.setName(studentDO.getName() + "Del");
		assert dbHelper.deleteByKey(studentDO) == 1;

	}
	
	@Test @Rollback(false)
	public void batchDelete() {
		List<StudentDO> insertBatch = CommonOps.insertBatch(dbHelper,10);
		int rows = dbHelper.deleteByKey(insertBatch);
		Assert.assertTrue(rows == insertBatch.size());
		
		insertBatch = CommonOps.insertBatch(dbHelper,20);
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
