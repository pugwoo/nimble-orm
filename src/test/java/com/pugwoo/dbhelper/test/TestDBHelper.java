package com.pugwoo.dbhelper.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.test.model.StudentDO;

/**
 * 2015年1月13日 11:11:23
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelper {
	
	@Autowired
	private DBHelper dbHelper;
	
	private String getRandomName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}
	
	private StudentDO insertOne() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName(getRandomName("nick"));
		studentDO.setIntro(studentDO.getName().getBytes());
		dbHelper.insert(studentDO);
		return studentDO;
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
	
	// ============ INSERT TEST ============================
	
	@Test
	public void testInsert() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("mytestname");
		studentDO.setAge(12);
		dbHelper.insert(studentDO);
		
		StudentDO st = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		Assert.assertTrue(st.getName().equals("mytestname"));
		
		studentDO.setId(null);
		studentDO.setName(null);
		dbHelper.insertWithNull(studentDO);
		st = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		Assert.assertTrue(st.getName() == null);
	}
	
	
	// ============ UPDATE TEST START ======================
	@Test
	public void testUpdateNull() {
		StudentDO db = insertOne();
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
	@Rollback(false)
	public void testUpdate() {
		StudentDO db = insertOne();
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
	@Rollback(false)
	public void testUpdateCustom() {
		StudentDO db = insertOne();
		
		StudentDO studentDO = new StudentDO();
		studentDO.setId(db.getId());
		
		int rows = dbHelper.updateCustom(studentDO, "name=?", "nick2");
		Assert.assertTrue(rows == 1);
		
		db = dbHelper.getByKey(StudentDO.class, db.getId());
		Assert.assertTrue("nick2".equals(db.getName()));
	}
	
	// ============ UPDATE TEST END ======================
	
	// ============ INSERT_UPDATE TEST START =============
	
	@Test
	@Rollback(false)
	public void testInsertOrUpdateFull() {
		List<StudentDO> old = insertBatch(20);
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
	}
	
	// ============ INSERT_UPDATE TEST END ===============
	
	// ============ DELETE TEST START ====================
	
	@Test
	@Rollback(false)
	public void testDelete() {
		StudentDO studentDO = insertOne();
		dbHelper.deleteByKey(studentDO);
		
		Assert.assertTrue(dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null);
		
		studentDO = insertOne();
		dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
		
		Assert.assertTrue(dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null);
	}
	
	// 测试写where条件的自定义删除
	@Test
	@Rollback(false)
	public void testDeleteWhere() {
		dbHelper.delete(StudentDO.class, "where name=?", "nick2");
	}
	
	// ============ DELETE TEST END ======================
	
	/**
	 * 测试jdbcTemplate
	 */
	@Test
	public void testJdbcTemplate() {
		Integer count = dbHelper.queryForObject(
				Integer.class, "select count(*) from t_student where id<?", 50);
		System.out.println("count:" + count);
		
		Map<String, Object> map = dbHelper.queryForMap("select * from t_student limit 1");
		System.out.println(map);
	}
	
	@Test
	public void testGetByArray() {
		// 但是这种写法不稳定的，推荐传入List参数值
		List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where id in (?)", new long[]{50,51,52});
		System.out.println(list.size());
	}
		
	@Test
	public void testGetList() {
		// 测试获取全部
		List<StudentDO> list = dbHelper.getAll(StudentDO.class);
		System.out.println("total:" + list.size());
		for(StudentDO studentDO : list) {
			System.out.println(studentDO);
		}
		
		System.out.println("===============================");
		
		// 测试获取有条件的查询
		Long[] ids = new Long[3];
		ids[0] = 2L;
		ids[1] = 4L;
		ids[2] = 6L;
		//List<StudentDO> list2 = dbHelper.getAll(StudentDO.class, "where id in (?)",
		//		ids); // 这样是错误的范例，getAll只会取ids的第一个参数传入in (?)中
		List<StudentDO> list2 = dbHelper.getAll(StudentDO.class, "where id in (?)",
				ids, 1); // 这是一种hack的写法，后面带上的参数1，可以让Java把ids当作单个参数处理
		System.out.println("total:" + list2.size());
		for(StudentDO studentDO : list2) {
			System.out.println(studentDO);
		}
		
		System.out.println("===============================");
		

	}
	
	@Test
	public void testGetPage() {
		insertBatch(100);
		
		// 测试分页获取
		PageData<StudentDO> page1 = dbHelper.getPage(StudentDO.class, 1, 10);
		Assert.assertTrue(page1.getTotal() >= 100);
		Assert.assertTrue(page1.getData().size() == 10);
		
		page1 = dbHelper.getPage(StudentDO.class, 2, 10);
		Assert.assertTrue(page1.getTotal() >= 100);
		Assert.assertTrue(page1.getData().size() == 10);
		
		page1 = dbHelper.getPageWithoutCount(StudentDO.class, 1, 10);
		Assert.assertTrue(page1.getData().size() == 10);
		
		page1 = dbHelper.getPageWithoutCount(StudentDO.class, 2, 10);
		Assert.assertTrue(page1.getData().size() == 10);
		
		int total = dbHelper.getCount(StudentDO.class);
		Assert.assertTrue(total >= 100);
		
		total = dbHelper.getCount(StudentDO.class, "where name like ?", "nick%");
		Assert.assertTrue(total >= 100);
	}
	
	@Test
	public void testGetByKey() {
		StudentDO studentDO = new StudentDO();
		studentDO.setId(2L);
		if(dbHelper.getByKey(studentDO)) {
			System.out.println(studentDO);
		} else {
			System.out.println("not found");
		}
		
		StudentDO student2 = dbHelper.getByKey(StudentDO.class, 2);
		System.out.println("student2:" + student2);
		
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put("id", 2);
		StudentDO student3 = dbHelper.getByKey(StudentDO.class, keyMap);
		System.out.println("student3:" + student3);
	}
	
	@Test
	public void testGetByKeyList() {
		List<Long> ids = new ArrayList<Long>();
		ids.add(insertOne().getId());
		ids.add(insertOne().getId());
		ids.add(insertOne().getId());
		Map<Long, StudentDO> map = dbHelper.getByKeyList(StudentDO.class, ids);
		
		Assert.assertTrue(map.size() == 3);
		for(int i = 0; i < 3; i++) {
			Assert.assertTrue(map.get(ids.get(i)).getId().equals(ids.get(i)));
		}
	}
	
	@Test
	@Rollback(false)
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
		row = dbHelper.insertWithNullInOneSQL(students);
		System.out.println("affected rows:" + row);
	}
	
	@Test
	@Rollback(false)
	public void testInsertWhereNotExists() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
		
		int row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
		Assert.assertTrue(row == 1);
		
		// 这个不会插入，写不写`where`关键字都可以
		row = dbHelper.insertWhereNotExist(studentDO, "where name=?", studentDO.getName());
		Assert.assertTrue(row == 0);
		
		// 这个不会插入
		row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
		Assert.assertTrue(row == 0);
		
		row = dbHelper.deleteByKey(studentDO);
		Assert.assertTrue(row == 1);
		
		// 删除后再插入
		studentDO.setId(null);
		row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
		Assert.assertTrue(row == 1);
		
		row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
		Assert.assertTrue(row == 0);
	}
	
	/////////////////////////测试删除///////////////////////////
	
	@Test
	@Rollback(false)
	public void deleteByKey() {
		StudentDO studentDO = insertOne();
		
		int rows = dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
		Assert.assertTrue(rows == 1);
		
		rows = dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
		Assert.assertTrue(rows == 0);
		
	    // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况
		
		studentDO = insertOne();
		rows = dbHelper.deleteByKey(studentDO);
		Assert.assertTrue(rows == 1);
		
		rows = dbHelper.deleteByKey(studentDO);
		Assert.assertTrue(rows == 0);
	}
	
}
