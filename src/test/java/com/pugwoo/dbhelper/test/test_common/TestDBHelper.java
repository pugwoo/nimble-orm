package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.exception.CasVersionNotMatchException;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.model.SubQuery;
import com.pugwoo.dbhelper.test.entity.CasVersionDO;
import com.pugwoo.dbhelper.test.entity.CasVersionLongDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentRandomNameDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

/**
 * 2015年1月13日 11:11:23
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestDBHelper {
	
	@Autowired
	private DBHelper dbHelper;

	@Test
	public void testNewDBHelper() {
		JdbcTemplate jdbcTemplate = ((SpringJdbcDBHelper) dbHelper).getJdbcTemplate();
		DBHelper dbHelper2 = new SpringJdbcDBHelper(jdbcTemplate);
		assert dbHelper.getAll(StudentDO.class).size() == dbHelper2.getAll(StudentDO.class).size();
	}

	// ============ UPDATE TEST START ======================
	@Test 
	public void testUpdateNull() {
		StudentDO db = CommonOps.insertOne(dbHelper);
		db.setAge(null);
		dbHelper.updateWithNull(db);
		
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue(db.getAge() == null);
	
		db.setAge(3);
		dbHelper.update(db);
		db.setAge(null);
		dbHelper.updateWithNull(db, "where age=?", 3);
		
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue(db.getAge() == null);
		
		List<StudentDO> list = new ArrayList<StudentDO>();
		list.add(db);
		db.setName(null);
		dbHelper.updateWithNull(list);
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue(db.getName() == null);
	}
	
	@Test
	
	public void testUpdate() {
		StudentDO db = CommonOps.insertOne(dbHelper);
		db.setName("nick2");
		dbHelper.update(db);
		
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue(db.getName().equals("nick2"));
		
		db.setAge(3);
		dbHelper.update(db);
		db.setAge(null);
		dbHelper.update(db);
		
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue(db.getAge().equals(3));
		
		db.setName("nick3");
		dbHelper.update(db, "where age=?", 3);
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue(db.getName().equals("nick3"));
		
		List<StudentDO> list = new ArrayList<StudentDO>();
		list.add(db);
		db.setName("nick4");
		dbHelper.update(list);
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue("nick4".equals(db.getName()));
	}
	
	@Test
	
	public void testUpdateCustom() {
		StudentDO db = CommonOps.insertOne(dbHelper);
		
		StudentDO studentDO = new StudentDO();
		studentDO.setId(db.getId());
		
		int rows = dbHelper.updateCustom(studentDO, "name=?", "nick2");
		Assert.assertTrue(rows == 1);

		rows = dbHelper.updateCustom(studentDO, "set name=?", "nick3");
		Assert.assertTrue(rows == 1);

		rows = dbHelper.updateCustom(studentDO, "SET name=?", "nick4");
		Assert.assertTrue(rows == 1);
		
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue("nick4".equals(db.getName()));

		// 测试异常参数
		assert dbHelper.updateCustom(studentDO, null) == 0;
	}
	
	@Test
	
	public void testUpdateAll() {
		CommonOps.insertBatch(dbHelper,101);
		String newName = "nick" + UUID.randomUUID().toString().replace("-", "");
		
		int rows = dbHelper.updateAll(StudentDO.class,
				"set name=?", "where name like ?", newName, "nick%");
		Assert.assertTrue(rows > 0);
		
		List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where name=?", newName);
		Assert.assertTrue(list.size() == rows);

		// 测试异常参数
		assert dbHelper.updateAll(StudentDO.class, null, "where 1=1") == 0;
	}
	
	// ============ UPDATE TEST END ======================
	
	// ============ INSERT_UPDATE TEST START =============

    @Test 
	public void testInsertOrUpdate() {
		assert dbHelper.insertOrUpdate(null) == 0;

		StudentDO studentDO = new StudentDO();
		studentDO.setName(CommonOps.getRandomName("tom"));
		assert dbHelper.insertOrUpdate(null) == 0;
		assert dbHelper.insertOrUpdate(studentDO) == 1;
		assert studentDO.getId() != null;

		StudentDO student2 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		assert student2.getName().equals(studentDO.getName());

		student2.setName(CommonOps.getRandomName("jim"));
		assert dbHelper.insertOrUpdate(student2) == 1;
		assert student2.getId().equals(studentDO.getId());

		StudentDO student3 = dbHelper.getByKey(StudentDO.class, student2.getId());
		assert student2.getName().equals(student3.getName());
	}

	@Test 
	public void testInsertOrUpdateWithNull() {
		assert dbHelper.insertOrUpdateWithNull(null) == 0;

		StudentDO studentDO = new StudentDO();
		studentDO.setName(CommonOps.getRandomName("tom"));
		assert dbHelper.insertOrUpdateWithNull(null) == 0;
		assert dbHelper.insertOrUpdateWithNull(studentDO) == 1;
		assert studentDO.getId() != null;

		StudentDO student2 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		assert student2.getName().equals(studentDO.getName());

		student2.setName(CommonOps.getRandomName("jim"));
		assert dbHelper.insertOrUpdateWithNull(student2) == 1;
		assert student2.getId().equals(studentDO.getId());

		StudentDO student3 = dbHelper.getByKey(StudentDO.class, student2.getId());
		assert student2.getName().equals(student3.getName());
	}

	
	@Test
	
	public void testInsertOrUpdateFull() {
		List<StudentDO> old = CommonOps.insertBatch(dbHelper,20);
		Assert.assertTrue(old.size() == 20);
		
		List<StudentDO> newlist = 
				dbHelper.getAll(StudentDO.class, "where id >= ? and id <= ?",
					old.get(10).getId(), old.get(19).getId());
		
		StudentDO studentDO = new StudentDO();
		studentDO.setName("hahahaha");
		newlist.add(studentDO);
		
		dbHelper.insertOrUpdateFull(old, newlist);
		
		Assert.assertTrue(studentDO.getId() != null);
		
		for(int i = 0; i < 10; i++) {
			StudentDO s = dbHelper.getByKey(StudentDO.class, old.get(i).getId());
			Assert.assertTrue(s == null);
		}
		
		for(int i = 10; i < 20; i++) {
			StudentDO s = dbHelper.getByKey(StudentDO.class, old.get(i).getId());
			Assert.assertTrue(s != null);
		}
		
		dbHelper.insertOrUpdateFullWithNull(old, newlist);


		// 异常参数的情况
		assert dbHelper.insertOrUpdateFull(null, null) == 0;
		assert dbHelper.insertOrUpdateFull(null, new ArrayList<StudentDO>()) == 0;

		StudentDO new1 = new StudentDO();
		new1.setName(CommonOps.getRandomName("tom"));
		assert dbHelper.insertOrUpdateFull(null, ListUtils.newArrayList(new1)) == 1;
		assert new1.getId() != null;
		assert dbHelper.getByKey(StudentDO.class, new1.getId()).getName().equals(new1.getName());
	}
	
	// ============ INSERT_UPDATE TEST END ===============
	
	// ============ DELETE TEST START ====================
	
	@Test 
	public void testDelete() throws InterruptedException {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);

		dbHelper.deleteByKey(studentDO);
		
		Assert.assertTrue(dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null);
		
		studentDO = CommonOps.insertOne(dbHelper);

		dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
		
		Assert.assertTrue(dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null);
	}

	@Test 
	public void testDeleteList() throws InterruptedException {

		List<StudentDO> studentDOList = new ArrayList<StudentDO>();
		studentDOList.add(CommonOps.insertOne(dbHelper));
		studentDOList.add(CommonOps.insertOne(dbHelper));

		dbHelper.deleteByKey(studentDOList);
		for (StudentDO studentDO : studentDOList) {
			Assert.assertTrue(dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null);
		}
	}
	
	// 测试写where条件的自定义删除
	@Test 
	public void testDeleteWhere() throws InterruptedException {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		dbHelper.delete(StudentDO.class, "where name=?", studentDO.getName());
	}
	
	// ============ DELETE TEST END ======================

	@Test 
	public void testSubQuery() {
		StudentDO stu1 = CommonOps.insertOne(dbHelper);
		StudentDO stu2 = CommonOps.insertOne(dbHelper);
		StudentDO stu3 = CommonOps.insertOne(dbHelper);
		
		List<Long> ids = new ArrayList<Long>();
		ids.add(stu1.getId());
		ids.add(stu2.getId());
		ids.add(stu3.getId());
		
		SubQuery subQuery = new SubQuery("id", StudentDO.class, "where id in (?)", ids);
		
		List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where id in (?)", subQuery);
		Assert.assertTrue(all.size() == 3);
		for(StudentDO stu : all) {
			Assert.assertTrue(ids.contains(stu.getId()));
		}

		all = dbHelper.getAll(StudentDO.class, "where id in (?) and id > ?" +
				" and name != '\\''", subQuery, 0); // 测试subQuery和参数混合
		Assert.assertTrue(all.size() == 3);
		for(StudentDO stu : all) {
			Assert.assertTrue(ids.contains(stu.getId()));
		}
		
		// 测试3层subQuery
		SubQuery subQuery1 = new SubQuery("id", StudentDO.class, "where id in (?)", ids);
		SubQuery subQuery2 = new SubQuery("id", StudentDO.class, "where id in (?)", subQuery1);
		SubQuery subQuery3 = new SubQuery("id", StudentDO.class, "where id in (?)", subQuery2);
		
		all = dbHelper.getAll(StudentDO.class, "where id in (?)", subQuery3);
		Assert.assertTrue(all.size() == 3);
		for(StudentDO stu : all) {
			Assert.assertTrue(ids.contains(stu.getId()));
		}
	}
	
	@Test
	
	public void testInsert2() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick888");
		// studentDO.setAge(28); 
		
		int row = dbHelper.insert(studentDO); // 如果值为null，则用数据库默认值
		// int row = dbHelper.insertWithNull(studentDO); // 强制设置数据库
		System.out.println("affected rows:" + row);
		System.out.println(studentDO);
		
		// 测试批量写入
		List<StudentDO> students = new ArrayList<StudentDO>();
		for(int i = 0; i < 10; i++) {
			StudentDO stu = new StudentDO();
			stu.setName("test" + i);
			stu.setAge(i);
			students.add(stu);
		}
		row = dbHelper.insert(students);
		assert row == 10;

		// 测试批量写入，Set参数
		Set<StudentDO> studentSet = new HashSet<>();
		for(int i = 0; i < 10; i++) {
			StudentDO stu = new StudentDO();
			stu.setName("test" + i);
			stu.setAge(i);
			studentSet.add(stu);
		}
		row = dbHelper.insert(studentSet);
		assert row == 10;

		// 测试random值
        StudentRandomNameDO studentRandomNameDO = new StudentRandomNameDO();
        dbHelper.insert(studentRandomNameDO);
        assert studentRandomNameDO.getId() != null;
        assert !studentRandomNameDO.getName().isEmpty();
	}

	@Test
	
	public void testMaxStringLength() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");

		dbHelper.insert(studentDO);
		assert studentDO.getName().length()==32; // 注解配置了32位长度

		StudentDO student2 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		assert student2.getName().length()==32;

		student2.setName("nick222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222");
		dbHelper.update(student2);
		assert student2.getName().length()==32;

		StudentDO student3 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		assert student3.getName().length()==32;
	}

	@Test
	public void testCasVersion() {
        CasVersionDO casVersionDO = new CasVersionDO();
        casVersionDO.setName("nick");

        assert dbHelper.insert(casVersionDO) > 0; // 插入时会自动写入casVersion字段的值

        assert casVersionDO.getId() > 0;
        assert casVersionDO.getVersion() > 0;

        casVersionDO.setName("nick2");
        assert dbHelper.update(casVersionDO) > 0; // 更新时会自动改casVersion字段的值

		casVersionDO.setName("nick3");
		assert dbHelper.update(casVersionDO) > 0;

        // version设置为null会异常
        casVersionDO.setVersion(null);
        boolean exOccur = false;
        try {
            casVersionDO.setName("nick3");
            dbHelper.update(casVersionDO);
        } catch (Exception e) {
            if(e instanceof CasVersionNotMatchException) {
                exOccur = true;
            }
        }
        assert exOccur;

        // version设置为一个错的值，会异常
		casVersionDO.setVersion(99);
		exOccur = false;
		try {
			casVersionDO.setName("nick3");
			dbHelper.update(casVersionDO);
		} catch (Exception e) {
			if(e instanceof CasVersionNotMatchException) {
				exOccur = true;
			}
		}
		assert exOccur;

        // 再把version设置为3，就正常了
        casVersionDO.setVersion(3);
        assert dbHelper.update(casVersionDO) > 0;

        // 反查之后，版本应该就是4了
        CasVersionDO tmp = dbHelper.getByKey(CasVersionDO.class, casVersionDO.getId());
        assert tmp.getVersion().equals(4);

        assert dbHelper.updateCustom(tmp, "name=?", "nick5") > 0;
		assert dbHelper.updateCustom(tmp, "name=?", "nick6") > 0;
		assert dbHelper.updateCustom(tmp, "name=?", "nick7") > 0;

		// 此时版本应该是7
		tmp = dbHelper.getByKey(CasVersionDO.class, casVersionDO.getId());
		assert tmp.getVersion().equals(7);


		// 测试CAS版本字段是Long的情况
		CasVersionLongDO casVersionLongDO = new CasVersionLongDO();
		casVersionLongDO.setName("nick");

		assert dbHelper.insert(casVersionLongDO) > 0; // 插入时会自动写入casVersion字段的值

		assert casVersionLongDO.getId() > 0;
		assert casVersionLongDO.getVersion() == 1;

		casVersionLongDO.setName("nick2");
		assert dbHelper.update(casVersionLongDO) > 0; // 更新时会自动改casVersion字段的值
		assert casVersionLongDO.getVersion() == 2;

    }


}
