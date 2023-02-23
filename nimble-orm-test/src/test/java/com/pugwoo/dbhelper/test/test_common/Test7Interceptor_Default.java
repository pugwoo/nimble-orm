package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Test7Interceptor_Default {

	@Autowired
	@Qualifier("dbHelperWithDefaultInterceptor")
	private DBHelper dbHelper;

	@Test
	public void testQuery() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		Long id = studentDO.getId();

		dbHelper.getByKey(StudentDO.class, id);
		
		dbHelper.getAll(StudentDO.class);
		dbHelper.getPage(StudentDO.class, 1, 10);
		dbHelper.getOne(StudentDO.class);
	}
	
	@Test 
	public void testInsertUpdate() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		Long id = studentDO.getId();

		studentDO = new StudentDO();
		studentDO.setId(id);
		studentDO.setName("karen");
		dbHelper.update(studentDO);
		
		studentDO.setName("karennick");
		dbHelper.update(studentDO, "where name=?", "karen");
		
		dbHelper.updateCustom(studentDO, "age=age+1");
		dbHelper.updateAll(StudentDO.class, "name=?", "", "nick");
	}
	
	@Test 
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
	
	@Test 
	public void batchDelete() {
		List<StudentDO> insertBatch = CommonOps.insertBatch(dbHelper,10);
		int rows = dbHelper.deleteByKey(insertBatch);
		assert rows == insertBatch.size();
		
		insertBatch = CommonOps.insertBatch(dbHelper,20);
		rows = dbHelper.delete(StudentDO.class, "where 1=?", 1);
		assert rows >= 20;
	}
	
	@Test 
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
