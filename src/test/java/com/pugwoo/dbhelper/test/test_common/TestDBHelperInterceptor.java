package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.TypesDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc-interceptor.xml")
@RunWith(SpringJUnit4ClassRunner.class)
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
		List<Long> keys = new ArrayList<Long>();
		keys.add(id);
		dbHelper.getByKeyList(StudentDO.class, keys);
		
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
		Assert.assertTrue(rows == insertBatch.size());
		
		insertBatch = CommonOps.insertBatch(dbHelper,20);
		rows = dbHelper.delete(StudentDO.class, "where 1=?", 1);
		Assert.assertTrue(rows >= 20);
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
