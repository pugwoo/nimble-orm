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
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentTrueDeleteDO;
import com.pugwoo.dbhelper.test.vo.SchoolWithInnerClassVO;
import com.pugwoo.dbhelper.test.vo.StudentCalVO;
import com.pugwoo.dbhelper.test.vo.StudentSchoolJoinVO;
import com.pugwoo.dbhelper.test.vo.StudentVO;
import com.pugwoo.dbhelper.test.vo.StudentVOForHandleRelatedColumnOnly;

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
	
	// ============ JSON test ============================
	
	@Test
	public void testJSON() {
		StudentDO studentDO = new StudentDO();
		SchoolDO schoolDO = new SchoolDO();
		schoolDO.setName("SYSU");
		
		studentDO.setSchoolSnapshot(schoolDO);
		
		List<CourseDO> courses = new ArrayList<CourseDO>();
		studentDO.setCourseSnapshot(courses);
		
		CourseDO course1 = new CourseDO();
		course1.setName("math");
		courses.add(course1);
		
		CourseDO course2 = new CourseDO();
		course2.setName("eng");
		courses.add(course2);
		
		dbHelper.insert(studentDO);
		
		StudentDO studentDB = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		Assert.assertTrue(studentDB.getSchoolSnapshot() != null);
		Assert.assertTrue(studentDB.getSchoolSnapshot().getName().equals("SYSU"));
		
		Assert.assertTrue(studentDB.getCourseSnapshot() != null);
		Assert.assertTrue(studentDB.getCourseSnapshot().size() == 2);
		Assert.assertTrue(studentDB.getCourseSnapshot().get(0).getName().equals("math"));
		Assert.assertTrue(studentDB.getCourseSnapshot().get(1).getName().equals("eng"));
		
		studentDO.getCourseSnapshot().get(1).setName("english");
		dbHelper.update(studentDO);
		
		studentDB = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		Assert.assertTrue(studentDB.getCourseSnapshot().get(1).getName().equals("english"));
		
	}
	
	// ============ Transaction TEST =======================
	
	@Transactional
	@Test @Rollback(false) 
	public void testTransaction() throws InterruptedException {
		final StudentDO studentDO1 = insertOne();
		final StudentDO studentDO2 = insertOne();
		
		System.out.println("insert ok, id1:" + studentDO1.getId() +
				",id2:" + studentDO2.getId());
		
		dbHelper.executeAfterCommit(new Runnable() {
			@Override
			public void run() {
				System.out.println("transaction commit, student1:" + studentDO1.getId()
				 + ",student2:" + studentDO2.getId());
			}
		});
		
		System.out.println("myTrans end");
//		dbHelper.rollback(); // org.springframework.transaction.NoTransactionException
		// throw new RuntimeException(); // 抛出异常也无法让事务回滚
		// 原因：https://stackoverflow.com/questions/13525106/transactions-doesnt-work-in-junit
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
	
	@Test
	@Rollback(false)
	public void testUpdateAll() {
		insertBatch(101);
		String newName = "nick" + UUID.randomUUID().toString().replace("-", "");
		
		int rows = dbHelper.updateAll(StudentDO.class,
				"set name=?", "where name like ?", newName, "nick%");
		Assert.assertTrue(rows > 0);
		
		List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where name=?", newName);
		Assert.assertTrue(list.size() == rows);
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
	public void testDeleteWhere() {
		dbHelper.delete(StudentDO.class, "where name=?", "nick2");
	}
	
	// ============ DELETE TEST END ======================
		
	@Test
	public void testGetByArray() {
		// 但是这种写法不稳定的，推荐传入List参数值
		List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where id in (?)", new long[]{50,51,52});
		System.out.println(list.size());
	}
	
	@Test
	public void testGetJoin() {
		SchoolDO schoolDO = new SchoolDO();
		schoolDO.setName("sysu");
		dbHelper.insert(schoolDO);
		
		StudentDO studentDO = insertOne();
		studentDO.setSchoolId(schoolDO.getId());
		dbHelper.update(studentDO);
		
		StudentDO studentDO2 = insertOne();
		studentDO2.setSchoolId(schoolDO.getId());
		dbHelper.update(studentDO2);
		
		PageData<StudentSchoolJoinVO> pageData = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 10);
		Assert.assertTrue(pageData.getData().size() > 0);
		for(StudentSchoolJoinVO vo : pageData.getData()) {
			Assert.assertTrue(vo.getStudentDO() != null);
//			System.out.println(vo.getSchoolDO());
		}
		
		pageData = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 10,
				"where t1.name like ?", "nick%");
		Assert.assertTrue(pageData.getData().size() > 0);
		for(StudentSchoolJoinVO vo : pageData.getData()) {
			Assert.assertTrue(vo.getStudentDO() != null);
		}
		
		int total = dbHelper.getCount(StudentSchoolJoinVO.class);
		Assert.assertTrue(total > 0);
		total = dbHelper.getCount(StudentSchoolJoinVO.class, "where t1.name like ?", "nick%");
		Assert.assertTrue(total > 0);
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
	public void testExcludeInheritedColumn() {
		StudentDO studentDO = insertOne();
		StudentCalVO db = dbHelper.getByKey(StudentCalVO.class, studentDO.getId());
		Assert.assertTrue(db != null);
		Assert.assertTrue(db.getId() == null);
		Assert.assertTrue(db.getNameWithHi() != null && db.getNameWithHi().endsWith("hi"));
	}
	
	@Test
	public void testRelatedColumn() {
		
		SchoolDO schoolDO = new SchoolDO();
		schoolDO.setName("sysu");
		dbHelper.insert(schoolDO);
		
		StudentDO studentDO = insertOne();
		studentDO.setSchoolId(schoolDO.getId());
		dbHelper.update(studentDO);
		
		CourseDO courseDO1 = new CourseDO();
		courseDO1.setName("math");
		courseDO1.setStudentId(studentDO.getId());
		courseDO1.setIsMain(true); // math是主课程
		dbHelper.insert(courseDO1);
		
		CourseDO courseDO2 = new CourseDO();
		courseDO2.setName("eng");
		courseDO2.setStudentId(studentDO.getId());
		dbHelper.insert(courseDO2);
		
		StudentDO studentDO2  = insertOne();
		studentDO2.setSchoolId(schoolDO.getId());
		dbHelper.update(studentDO2);
		
		CourseDO courseDO3 = new CourseDO();
		courseDO3.setName("math");
		courseDO3.setStudentId(studentDO2.getId());
		courseDO3.setIsMain(true); // math是主课程
		dbHelper.insert(courseDO3);
		
		CourseDO courseDO4 = new CourseDO();
		courseDO4.setName("chinese");
		courseDO4.setStudentId(studentDO2.getId());
		dbHelper.insert(courseDO4);
		
		/////////////////// 下面是查询 ///////////////////
		
		StudentVO studentVO1 = dbHelper.getByKey(StudentVO.class, studentDO.getId());
		Assert.assertTrue(studentVO1 != null);
		Assert.assertTrue(studentVO1.getSchoolDO() != null);
		Assert.assertTrue(studentVO1.getSchoolDO().getId().equals(studentVO1.getSchoolId()));
		Assert.assertTrue(studentVO1.getCourses() != null);
		Assert.assertTrue(studentVO1.getCourses().size() == 2);
		Assert.assertTrue(studentVO1.getCourses().get(0).getId().equals(courseDO1.getId())
				|| studentVO1.getCourses().get(0).getId().equals(courseDO2.getId()));
		Assert.assertTrue(studentVO1.getMainCourses().size() == 1 &&
				studentVO1.getMainCourses().get(0).getName().equals("math")); // math是主课程
		Assert.assertTrue(studentVO1.getNameWithHi().equals(studentVO1.getName() + "hi")); // 测试计算列
		
		// == handleRelatedColumn test
		StudentVOForHandleRelatedColumnOnly studentVO2 = new StudentVOForHandleRelatedColumnOnly();
		studentVO2.setId(studentDO.getId());
		studentVO2.setSchoolId(studentDO.getSchoolId());
		dbHelper.handleRelatedColumn(studentVO2);
		Assert.assertTrue(studentVO2 != null);
		Assert.assertTrue(studentVO2.getSchoolDO() != null);
		Assert.assertTrue(studentVO2.getSchoolDO().getId().equals(studentVO2.getSchoolId()));
		Assert.assertTrue(studentVO2.getCourses() != null);
		Assert.assertTrue(studentVO2.getCourses().size() == 2);
		Assert.assertTrue(studentVO2.getCourses().get(0).getId().equals(courseDO1.getId())
				|| studentVO2.getCourses().get(0).getId().equals(courseDO2.getId()));
		Assert.assertTrue(studentVO2.getMainCourses().size() == 1 &&
				studentVO2.getMainCourses().get(0).getName().equals("math")); // math是主课程
		
		studentVO2 = new StudentVOForHandleRelatedColumnOnly();
		studentVO2.setId(studentDO.getId());
		studentVO2.setSchoolId(studentDO.getSchoolId());
		dbHelper.handleRelatedColumn(studentVO2, "courses", "schoolDO"); // 指定要的RelatedColumn
		Assert.assertTrue(studentVO2 != null);
		Assert.assertTrue(studentVO2.getSchoolDO() != null);
		Assert.assertTrue(studentVO2.getSchoolDO().getId().equals(studentVO2.getSchoolId()));
		Assert.assertTrue(studentVO2.getCourses() != null);
		Assert.assertTrue(studentVO2.getCourses().size() == 2);
		Assert.assertTrue(studentVO2.getCourses().get(0).getId().equals(courseDO1.getId())
				|| studentVO2.getCourses().get(0).getId().equals(courseDO2.getId()));
		Assert.assertTrue(studentVO2.getMainCourses() == null);
		
		// END

		List<Long> ids = new ArrayList<Long>();
		ids.add(studentDO.getId());
		ids.add(studentDO2.getId());
		List<StudentVO> studentVOs = dbHelper.getAll(StudentVO.class,
				"where id in (?)", ids);
		Assert.assertTrue(studentVOs.size() == 2);
		for(StudentVO sVO : studentVOs) {
			Assert.assertTrue(sVO != null);
			Assert.assertTrue(sVO.getSchoolDO() != null);
			Assert.assertTrue(sVO.getSchoolDO().getId().equals(sVO.getSchoolId()));
			Assert.assertTrue(sVO.getCourses() != null);
			Assert.assertTrue(sVO.getCourses().size() == 2);
			Assert.assertTrue(sVO.getMainCourses().size() == 1 && 
					studentVO1.getMainCourses().get(0).getName().equals("math")); // math是主课程
			
			if(sVO.getId().equals(studentDO2.getId())) {
				Assert.assertTrue(
						sVO.getCourses().get(0).getId().equals(courseDO3.getId())
				  || sVO.getCourses().get(1).getId().equals(courseDO4.getId()));
			}
			
			Assert.assertTrue(sVO.getNameWithHi().equals(sVO.getName() + "hi")); // 测试计算列
		}
		
		// 测试innerClass
		SchoolWithInnerClassVO schoolVO = dbHelper.getByKey(SchoolWithInnerClassVO.class, schoolDO.getId());
		Assert.assertTrue(schoolVO != null && schoolVO.getId().equals(schoolDO.getId()));
		Assert.assertTrue(schoolVO.getStudents().size() == 2);
		for(com.pugwoo.dbhelper.test.vo.SchoolWithInnerClassVO.StudentVO s : schoolVO.getStudents()) {
			Assert.assertTrue(s != null && s.getId() != null && s.getCourses().size() == 2);
		}
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
		
		List<StudentDO> allKey = dbHelper.getAllKey(StudentDO.class, "where 1=1");
		Assert.assertTrue(allKey.size() >= 3);
	}
	
	@Test
	public void testExists() {
		StudentDO studentDO = insertOne();
		Assert.assertTrue(dbHelper.isExist(StudentDO.class, null));
		Assert.assertTrue(dbHelper.isExist(StudentDO.class, "where id=?", studentDO.getId()));
		Assert.assertTrue(dbHelper.isExistAtLeast(1, StudentDO.class,
				"where id=?", studentDO.getId()));
		
		Assert.assertFalse(dbHelper.isExistAtLeast(2, StudentDO.class,
				"where id=?", studentDO.getId()));
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
	
	@Test
	@Rollback(false)
	public void testTrueDelete() {
		StudentTrueDeleteDO studentTrueDeleteDO = new StudentTrueDeleteDO();
		studentTrueDeleteDO.setName("john");
		dbHelper.insert(studentTrueDeleteDO);
		
		int rows = dbHelper.deleteByKey(StudentTrueDeleteDO.class, studentTrueDeleteDO.getId());
		Assert.assertTrue(rows == 1);
		
		rows = dbHelper.deleteByKey(StudentTrueDeleteDO.class, studentTrueDeleteDO.getId());
		Assert.assertTrue(rows == 0);
		
	    // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况
		
		studentTrueDeleteDO = new StudentTrueDeleteDO();
		studentTrueDeleteDO.setName("john");
		dbHelper.insert(studentTrueDeleteDO);
		
		rows = dbHelper.deleteByKey(studentTrueDeleteDO);
		Assert.assertTrue(rows == 1);
		
		rows = dbHelper.deleteByKey(studentTrueDeleteDO);
		Assert.assertTrue(rows == 0);
		
		//
		studentTrueDeleteDO = new StudentTrueDeleteDO();
		studentTrueDeleteDO.setName("john");
		dbHelper.insert(studentTrueDeleteDO);
		
		rows = dbHelper.delete(StudentTrueDeleteDO.class, "where name=?", "john");
		Assert.assertTrue(rows > 0);
		
		rows = dbHelper.delete(StudentTrueDeleteDO.class, "where name=?", "john");
		Assert.assertTrue(rows == 0);
	}
	
}
