package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.TypesDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

public abstract class Test7Interceptor_Custom {

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
		Long id = studentDO.getId();
		assert id != null;

		assert getDBHelper().getByKey(StudentDO.class, id).getName().equals(studentDO.getName());

		studentDO = new StudentDO();
		studentDO.setId(id);
		studentDO.setAge(29);
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
		ListUtils.forEach(students, studentDO -> studentDO.setId(null));

		if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
			ListUtils.forEach(students, studentDO -> studentDO.setId(CommonOps.getRandomLong()));
		}

		Set<StudentDO> students2 = new HashSet<>(students);
		assert getDBHelper().insert(students2) == 10;
	}

	@Test 
	public void testMultiKeyUpdateAll() {
		Long long1 = CommonOps.getRandomLong();
		Long long2 = CommonOps.getRandomLong();
		Long long3 = CommonOps.getRandomLong();
		Long long4 = CommonOps.getRandomLong();

		TypesDO typesDO1 = new TypesDO();
		typesDO1.setId1(long1);
		typesDO1.setId2(long2);

		assert getDBHelper().insert(typesDO1) == 1;

		TypesDO typesDO2 = new TypesDO();
		typesDO2.setId1(long1);
		typesDO2.setId2(long3);
		TypesDO typesDO3 = new TypesDO();
		typesDO3.setId1(long1);
		typesDO3.setId2(long4);

		assert getDBHelper().insert(ListUtils.newArrayList(typesDO2, typesDO3)) == 2;

		int rows = getDBHelper().updateAll(TypesDO.class, "set my_short=3",
				"where id1=?", long1);
		if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
			assert rows == 1;
		} else {
			assert rows == 3;
		}

		List<TypesDO> all = getDBHelper().getAll(TypesDO.class, "where id1=?", long1);
		assert all.size() == 3;
		for(TypesDO typesDO : all) {
			assert typesDO.getS() == 3;
		}

	}
	
	@Test 
	public void testDelete() {
		StudentDO studentDO = CommonOps.insertOne(getDBHelper(), "nick");
		Long id = studentDO.getId();
		
		assert getDBHelper().delete(StudentDO.class, "where id=?", id) == 1;
	}
	
	@Test 
	public void batchDelete() {
		List<StudentDO> insertBatch = CommonOps.insertBatch(getDBHelper(),10);
		int rows = getDBHelper().delete(insertBatch);
		if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
			assert rows == insertBatch.size();
		} else {
			assert rows == 1;
		}

		CommonOps.insertBatch(getDBHelper(),20);
		rows = getDBHelper().delete(StudentDO.class, "where 1=? limit 20", 1);

		if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
			assert rows >= 20;
		} else {
			assert rows == 1;
		}
	}
	
	@Test 
	public void testCustomsUpdateDelete() {
		StudentDO studentDO = CommonOps.insertOne(getDBHelper(), "nick");
		studentDO.setAge(29);
		assert getDBHelper().update(studentDO) == 1;

		assert getDBHelper().updateCustom(studentDO, "age=age+1") == 1;
		assert getDBHelper().delete(StudentDO.class, "where 1=1 limit 15") >= 1;
		
		studentDO = CommonOps.insertOne(getDBHelper(), "nick");
		studentDO.setAge(29);
		assert getDBHelper().update(studentDO) == 1;
		if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.MYSQL) {
			assert getDBHelper().updateAll(StudentDO.class, "age=age+1", "where 1=1 limit 12") >= 1; // 只有mysql支持limit (不过pg支持子查询limit，而mysql不支持)
		} else {
			assert getDBHelper().updateAll(StudentDO.class, "age=age+1", "where 1=1") >= 1;
		}
	}
}
