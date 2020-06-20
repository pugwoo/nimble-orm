package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.model.SubQuery;
import com.pugwoo.dbhelper.exception.CasVersionNotMatchException;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.test.entity.*;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.*;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 2015年1月13日 11:11:23
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelper {
	
	@Autowired
	private DBHelper dbHelper;

	// ============ JSON test ============================
	
	@Test @Rollback(false)
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

	// ============ INSERT TEST ============================
	
	@Test @Rollback(false)
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
	@Test @Rollback(false)
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
	@Rollback(false)
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
	@Rollback(false)
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
	}
	
	@Test
	@Rollback(false)
	public void testUpdateAll() {
		CommonOps.insertBatch(dbHelper,101);
		String newName = "nick" + UUID.randomUUID().toString().replace("-", "");
		
		int rows = dbHelper.updateAll(StudentDO.class,
				"set name=?", "where name like ?", newName, "nick%");
		Assert.assertTrue(rows > 0);
		
		List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where name=?", newName);
		Assert.assertTrue(list.size() == rows);
	}
	
	// ============ UPDATE TEST END ======================
	
	// ============ INSERT_UPDATE TEST START =============

    @Test @Rollback(false)
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

	@Test @Rollback(false)
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
	@Rollback(false)
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
	
	@Test @Rollback(false)
	public void testDelete() throws InterruptedException {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);

		dbHelper.deleteByKey(studentDO);
		
		Assert.assertTrue(dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null);
		
		studentDO = CommonOps.insertOne(dbHelper);

		dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
		
		Assert.assertTrue(dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null);
	}

	@Test @Rollback(false)
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
	@Test @Rollback(false)
	public void testDeleteWhere() throws InterruptedException {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		dbHelper.delete(StudentDO.class, "where name=?", studentDO.getName());
	}
	
	// ============ DELETE TEST END ======================
		
	@Test @Rollback(false)
	public void testGetByArray() {
		// 但是这种写法不稳定的，推荐传入List参数值
		List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where id in (?)", new long[]{50,51,52});
		System.out.println(list.size());
		list = dbHelper.getAll(StudentDO.class, "where id in (?)", new int[]{50,51,52});
		System.out.println(list.size());
		list = dbHelper.getAll(StudentDO.class, "where id in (?)", new short[]{50,51,52});
		System.out.println(list.size());
		list = dbHelper.getAll(StudentDO.class, "where id in (?)", new char[]{50,51,52});
		System.out.println(list.size());
		list = dbHelper.getAll(StudentDO.class, "where id in (?)", new float[]{50,51,52});
		System.out.println(list.size());
		list = dbHelper.getAll(StudentDO.class, "where id in (?)", new double[]{50,51,52});
		System.out.println(list.size());

		// 测试空list或空set
		list = dbHelper.getAll(StudentDO.class, "where id in (?)", new ArrayList<Long>());
		assert list.isEmpty();
		list = dbHelper.getAll(StudentDO.class, "where id in (?)", new HashSet<Long>());
		assert list.isEmpty();
	}
	
	@Test @Rollback(false)
	public void testGetJoin() {
		SchoolDO schoolDO = new SchoolDO();
		schoolDO.setName("sysu");
		dbHelper.insert(schoolDO);
		
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		studentDO.setSchoolId(schoolDO.getId());
		dbHelper.update(studentDO);
		
		StudentDO studentDO2 = CommonOps.insertOne(dbHelper);
		studentDO2.setSchoolId(schoolDO.getId());
		dbHelper.update(studentDO2);
		
		PageData<StudentSchoolJoinVO> pageData = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 10);
		Assert.assertTrue(pageData.getData().size() > 0);
		for(StudentSchoolJoinVO vo : pageData.getData()) {
			Assert.assertTrue(vo.getStudentDO() != null);
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

		// right join test
        PageData<StudentSchoolJoinVO2> pageData2 = dbHelper.getPage(StudentSchoolJoinVO2.class, 1, 10);
        Assert.assertTrue(pageData2.getData().size() > 0);
        for(StudentSchoolJoinVO2 vo : pageData2.getData()) {
            Assert.assertTrue(vo.getStudentDO() != null);
        }

	}
		
	@Test @Rollback(false)
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
	
	@Test @Rollback(false)
	public void testGetPage() {
		CommonOps.insertBatch(dbHelper,100);
		
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
	
	@Test @Rollback(false)
	public void testExcludeInheritedColumn() {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		StudentCalVO db = dbHelper.getByKey(StudentCalVO.class, studentDO.getId());
		Assert.assertTrue(db != null);
		Assert.assertTrue(db.getId() == null);
		Assert.assertTrue(db.getNameWithHi() != null && db.getNameWithHi().endsWith("hi"));
	}
	
	@Test @Rollback(false)
	public void testRelatedColumn() {
		
		SchoolDO schoolDO = new SchoolDO();
		schoolDO.setName("sysu");
		dbHelper.insert(schoolDO);
		
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
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
		
		StudentDO studentDO2  = CommonOps.insertOne(dbHelper);
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
	
	@Test @Rollback(false)
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
	
	@Test @Rollback(false)
	public void testGetByKeyList() {
		List<Long> ids = new ArrayList<Long>();
		ids.add(CommonOps.insertOne(dbHelper).getId());
		ids.add(CommonOps.insertOne(dbHelper).getId());
		ids.add(CommonOps.insertOne(dbHelper).getId());
		Map<Long, StudentDO> map = dbHelper.getByKeyList(StudentDO.class, ids);
		
		Assert.assertTrue(map.size() == 3);
		for(int i = 0; i < 3; i++) {
			Assert.assertTrue(map.get(ids.get(i)).getId().equals(ids.get(i)));
		}
		
		List<StudentDO> allKey = dbHelper.getAllKey(StudentDO.class, "where 1=1");
		Assert.assertTrue(allKey.size() >= 3);
	}
	
	@Test @Rollback(false)
	public void testExists() {
		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		Assert.assertTrue(dbHelper.isExist(StudentDO.class, null));
		Assert.assertTrue(dbHelper.isExist(StudentDO.class, "where id=?", studentDO.getId()));
		Assert.assertTrue(dbHelper.isExistAtLeast(1, StudentDO.class,
				"where id=?", studentDO.getId()));
		
		Assert.assertFalse(dbHelper.isExistAtLeast(2, StudentDO.class,
				"where id=?", studentDO.getId()));
	}

	@Test @Rollback(false)
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
		row = dbHelper.insert(students);
		System.out.println("affected rows:" + row);

		// 测试random值
        StudentRandomNameDO studentRandomNameDO = new StudentRandomNameDO();
        dbHelper.insert(studentRandomNameDO);
        assert studentRandomNameDO.getId() != null;
        assert !studentRandomNameDO.getName().isEmpty();
	}

	@Test
	@Rollback(false)
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
		row = dbHelper.insertWithNullWhereNotExist(studentDO, "name=?", studentDO.getName());
		Assert.assertTrue(row == 1);
		
		row = dbHelper.insertWithNullWhereNotExist(studentDO, "name=?", studentDO.getName());
		Assert.assertTrue(row == 0);

        row = dbHelper.insertWithNullWhereNotExist(studentDO, "name=?", studentDO.getName());
        Assert.assertTrue(row == 0);
	}

	@Test
    @Rollback(false)
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
    }


}
