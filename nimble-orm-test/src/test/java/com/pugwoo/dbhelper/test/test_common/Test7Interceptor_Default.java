package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

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
		String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 24);

		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick" + uuid);
		studentDO.setAge(29);
		assert dbHelper.insert(studentDO) == 1;
		Long id = studentDO.getId();
		assert id != null;

		assert dbHelper.getByKey(StudentDO.class, id).getName().equals(studentDO.getName());

		studentDO = new StudentDO();
		studentDO.setId(id);
		studentDO.setName("karen" + uuid);
		assert dbHelper.update(studentDO) == 1;
		assert dbHelper.getByKey(StudentDO.class, id).getName().equals(studentDO.getName());
		
		studentDO.setName("karennick");
		assert dbHelper.update(studentDO, "where name=?", "karen" + uuid) == 1;
		
		assert dbHelper.updateCustom(studentDO, "age=age+1") == 1;
		dbHelper.updateAll(StudentDO.class, "name=?", "", "nick");
	}

	@Test
	public void testInsertBatch() {
		List<StudentDO> students = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			StudentDO s = new StudentDO();
			s.setName(UUID.randomUUID().toString().replace("-", ""));
			students.add(s);
		}

		assert dbHelper.insert(students) == 10;

		// 转换成set再插入一次
		ListUtils.forEach(students, studentDO -> studentDO.setId(null));
		Set<StudentDO> students2 = new HashSet<>(students);
		assert dbHelper.insert(students2) == 10;
	}
	
	@Test 
	public void testDelete() {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);

		Long id = studentDO.getId();
		assert dbHelper.getByKey(StudentDO.class, id).getName().equals(studentDO.getName());
		
		assert dbHelper.delete(StudentDO.class, "where id=?", id) == 1;

		CommonOps.insertBatch(dbHelper, CommonOps.getRandomInt(101, 100));
		assert dbHelper.delete(StudentDO.class, "where id > ?", 100) > 0;

		studentDO = CommonOps.insertOne(dbHelper);

		assert dbHelper.getByKey(StudentDO.class, studentDO.getId())
				.getName().equals(studentDO.getName());
		studentDO.setName(studentDO.getName() + "Del");
		assert dbHelper.delete(studentDO) == 1;

	}
	
	@Test 
	public void batchDelete() {
		List<StudentDO> insertBatch = CommonOps.insertBatch(dbHelper,10);
		int rows = dbHelper.delete(insertBatch);
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
