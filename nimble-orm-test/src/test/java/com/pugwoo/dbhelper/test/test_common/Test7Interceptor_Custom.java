package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.TypesDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class Test7Interceptor_Custom {

	@Autowired
	@Qualifier("dbHelperWithInterceptor")
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
	public void testMultiKeyUpdateAll() {
		Long long1 = new Random().nextLong();
		Long long2 = new Random().nextLong();
		Long long3 = new Random().nextLong();
		Long long4 = new Random().nextLong();

		TypesDO typesDO1 = new TypesDO();
		typesDO1.setId1(long1);
		typesDO1.setId2(long2);

		assert dbHelper.insert(typesDO1) == 1;

		TypesDO typesDO2 = new TypesDO();
		typesDO2.setId1(long1);
		typesDO2.setId2(long3);
		TypesDO typesDO3 = new TypesDO();
		typesDO3.setId1(long1);
		typesDO3.setId2(long4);

		assert dbHelper.insert(ListUtils.newArrayList(typesDO2, typesDO3)) == 2;

		assert dbHelper.updateAll(TypesDO.class, "set my_short=3",
				"where id1=?", long1) == 3;
		List<TypesDO> all = dbHelper.getAll(TypesDO.class, "where id1=?", long1);
		assert all.size() == 3;
		for(TypesDO typesDO : all) {
			assert typesDO.getS() == 3;
		}

	}
	
	@Test 
	public void testDelete() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick");
		studentDO.setAge(29);
		dbHelper.insert(studentDO);
		Long id = studentDO.getId();
		
		dbHelper.deleteByKey(StudentDO.class, id);
		dbHelper.delete(StudentDO.class, "where id > ?", 100);
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
