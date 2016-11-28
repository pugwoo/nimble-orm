package com.pugwoo.dbhelper.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	// ============ UPDATE TEST START ======================
	
	@Test
	@Rollback(false)
	public void testUpdateCustom() {
		StudentDO studentDO = new StudentDO();
		studentDO.setId(1L);
		int rows = dbHelper.updateCustom(studentDO, "name=?", "nick");
		System.out.println(rows);
	}
	
	// ============ UPDATE TEST END ======================
	
	@Test
	@Rollback(false)
	public void testInsertOrUpdateFull() {
		List<StudentDO> old = dbHelper.getAll(StudentDO.class, "where id < 10");
		List<StudentDO> newlist = dbHelper.getAll(StudentDO.class, "where id > 7 and id < 10");
		StudentDO studentDO = new StudentDO();
		studentDO.setName("hahahaha");
		newlist.add(studentDO);
		dbHelper.insertOrUpdateFull(old, newlist);
	}
	
	@Test
	@Rollback(false)
	public void testUpdate() {
		StudentDO studentDO = new StudentDO();
		studentDO.setId(1L);
		studentDO.setName("new name");
		dbHelper.update(studentDO);
	}
	
	@Test
	@Rollback(false)
	public void testInsert() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("mytestname");
		studentDO.setAge(12);
		dbHelper.insert(studentDO);
		
		StudentDO st = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		System.out.println(st.getName());
		
		// 测试=?时传的参数是null的情况,会爆异常
		// 为什么id=?当传入null值时，不自动转换为is null呢？
		// 【因为】null本身就是一个有歧义的用法，在大多数查询中，都是明确查询有值的。
		//        自动转换null的话，可能将异常的参数当作正常的情况被处理，将会是一个大坑。
		// st = dbHelper.getOne(StudentDO.class, "where id=?", null);
		// System.out.println(st);
	}
	
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
		
		// 测试分页获取
		PageData<StudentDO> page1 = dbHelper.getPage(StudentDO.class, 1, 10);
		System.out.println("total:" + page1.getTotal());
		System.out.println("cur page size:" + page1.getData().size());
		for(StudentDO studentDO : page1.getData()) {
			System.out.println(studentDO);
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
		ids.add(50L);
		ids.add(52L);
		ids.add(54L);
		Map<Long, StudentDO> map = dbHelper.getByKeyList(StudentDO.class, ids);
		System.out.println(map);
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
		studentDO.setName("nick99");
		
		int row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
		System.out.println("row=" + row);
		
		// 这个不会插入
		row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
		System.out.println("row=" + row);
		
		// 这个不会插入
		row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
		System.out.println("row=" + row);
	}
	
	@Test
	@Rollback(false)
	public void deleteByKey() {
//		int rows = dbHelper.deleteByKey(StudentDO.class, 50);
//		System.out.println(rows);
		
	    // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况
		
		StudentDO studentDO = new StudentDO();
		studentDO.setId(60L);
		System.out.println(dbHelper.deleteByKey(studentDO)); 
	}
	
}
