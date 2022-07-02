package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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


}
