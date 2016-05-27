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
 *
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelper {
	
	@Autowired
	private DBHelper dbHelper;
		
	@Test
	@Rollback(false)
	public void test() {
		StudentDO studentDO = new StudentDO();
		studentDO.setNum("hello world".getBytes());
		dbHelper.insert(studentDO);
		
		StudentDO st = dbHelper.getByKey(StudentDO.class, studentDO.getId());
		System.out.println(new String(st.getNum()));
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
	@Rollback(false)
	public void testInsert() {
		StudentDO studentDO = new StudentDO();
		studentDO.setName("nick888");
		studentDO.setAge(28);
		
		int row = dbHelper.insert(studentDO);
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
		row = dbHelper.insertInOneSQL(students);
		System.out.println("affected rows:" + row);
	}
	
}
