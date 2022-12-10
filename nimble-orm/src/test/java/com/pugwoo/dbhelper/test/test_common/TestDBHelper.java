package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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

	@Test
	public void testGlobalComment() throws Exception {
		JdbcTemplate jdbcTemplate = ((SpringJdbcDBHelper) dbHelper).getJdbcTemplate();
		final DBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);

		String globalComment = UUID.randomUUID().toString();
		dbHelper.setGlobalComment(globalComment);

		AtomicBoolean isWithComment = new AtomicBoolean(false);
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			List<Map> processlist = dbHelper.getRaw(Map.class, "SHOW PROCESSLIST");
			for (Map<String, Object> p : processlist) {
				String info = (String) p.get("Info");
				System.out.println("===" + info);
				if (info != null && info.contains(globalComment) && info.contains("select sleep(1)")) {
					isWithComment.set(true);
				}
			}
		});
		thread.start();

		dbHelper.getRaw(String.class, "select sleep(1)");

		thread.join();

		assert isWithComment.get();

		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		assert dbHelper.getByKey(StudentDO.class, studentDO.getId()).getName().equals(studentDO.getName());
	}

	@Test
	public void testLocalComment() throws Exception {
		JdbcTemplate jdbcTemplate = ((SpringJdbcDBHelper) dbHelper).getJdbcTemplate();
		final DBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);

		String localComment = UUID.randomUUID().toString();
		dbHelper.setLocalComment(localComment);

		AtomicBoolean isWithComment = new AtomicBoolean(false);
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			List<Map> processlist = dbHelper.getRaw(Map.class, "SHOW PROCESSLIST");
			for (Map<String, Object> p : processlist) {
				String info = (String) p.get("Info");
				System.out.println("===" + info);
				if (info != null && info.contains(localComment) && info.contains("select sleep(1)")) {
					isWithComment.set(true);
				}
			}
		});
		thread.start();

		dbHelper.getRaw(String.class, "select sleep(1)");

		thread.join();

		assert isWithComment.get();

		StudentDO studentDO = CommonOps.insertOne(dbHelper);
		assert dbHelper.getByKey(StudentDO.class, studentDO.getId()).getName().equals(studentDO.getName());
	}

}
