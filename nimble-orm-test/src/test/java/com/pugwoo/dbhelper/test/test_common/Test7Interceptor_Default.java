package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

public abstract class Test7Interceptor_Default {

	public abstract DBHelper getDBHelper();

	@Test
	public void testQuery() {
		StudentDO studentDO = CommonOps.insertOne(getDBHelper(), "nick");
		Long id = studentDO.getId();

		getDBHelper().getByKey(StudentDO.class, id);
		
		getDBHelper().getAll(StudentDO.class);
		getDBHelper().getPage(StudentDO.class, 1, 10);
		getDBHelper().getOne(StudentDO.class);
	}
	
	@Test 
	public void testInsertUpdate() {
		String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 24);

		StudentDO studentDO = CommonOps.insertOne(getDBHelper(), "nick" + uuid);
		studentDO.setAge(29);
		getDBHelper().update(studentDO);

		Long id = studentDO.getId();
		assert id != null;

		assert getDBHelper().getByKey(StudentDO.class, id).getName().equals(studentDO.getName());

		studentDO = new StudentDO();
		studentDO.setId(id);
		studentDO.setName("karen" + uuid);
		assert getDBHelper().update(studentDO) == 1;
		assert getDBHelper().getByKey(StudentDO.class, id).getName().equals(studentDO.getName());
		
		studentDO.setName("karennick");
		assert getDBHelper().update(studentDO, "where name=?", "karen" + uuid) == 1;
		
		assert getDBHelper().updateCustom(studentDO, "age=age+1") == 1;
		getDBHelper().updateAll(StudentDO.class, "name=?", "", "nick");
	}

	@Test
	public void testInsertBatch() {
		List<StudentDO> students = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			StudentDO s = new StudentDO();
			if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
				s.setId(CommonOps.getRandomLong());
			}
			s.setName(UUID.randomUUID().toString().replace("-", ""));
			students.add(s);
		}

		assert getDBHelper().insert(students) == 10;

		// 转换成set再插入一次
		if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
			ListUtils.forEach(students, studentDO -> studentDO.setId(CommonOps.getRandomLong()));
		} else {
			ListUtils.forEach(students, studentDO -> studentDO.setId(null));
		}
		Set<StudentDO> students2 = new HashSet<>(students);
		assert getDBHelper().insert(students2) == 10;
	}
	
	@Test 
	public void testDelete() {
		StudentDO studentDO = CommonOps.insertOne(getDBHelper());

		Long id = studentDO.getId();
		assert getDBHelper().getByKey(StudentDO.class, id).getName().equals(studentDO.getName());
		
		assert getDBHelper().delete(StudentDO.class, "where id=?", id) == 1;

		CommonOps.insertBatch(getDBHelper(), CommonOps.getRandomInt(101, 100));
		assert getDBHelper().delete(StudentDO.class, "where id > ?", 100) > 0;

		studentDO = CommonOps.insertOne(getDBHelper());

		assert getDBHelper().getByKey(StudentDO.class, studentDO.getId())
				.getName().equals(studentDO.getName());
		studentDO.setName(studentDO.getName() + "Del");
		assert getDBHelper().delete(studentDO) == 1;

	}
	
	@Test 
	public void batchDelete() {
		List<StudentDO> insertBatch = CommonOps.insertBatch(getDBHelper(),10);
		int rows = getDBHelper().delete(insertBatch);

		assert rows == insertBatch.size();

		insertBatch = CommonOps.insertBatch(getDBHelper(),20);
		rows = getDBHelper().delete(StudentDO.class, "where 1=?", 1);
		if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
			assert rows == 1;
		} else {
			assert rows >= 20;
		}

	}
	
	@Test 
	public void testCustomsUpdateDelete() {
		StudentDO studentDO = CommonOps.insertOne(getDBHelper(), "nick");
		studentDO.setAge(29);
		getDBHelper().update(studentDO);
		
		getDBHelper().updateCustom(studentDO, "age=age+1");
		getDBHelper().delete(StudentDO.class, "where 1=1");

		studentDO = CommonOps.insertOne(getDBHelper(), "nick");
		studentDO.setAge(29);
		getDBHelper().update(studentDO);
		getDBHelper().updateAll(StudentDO.class, "age=age+1", "where 1=1");
	}
}
