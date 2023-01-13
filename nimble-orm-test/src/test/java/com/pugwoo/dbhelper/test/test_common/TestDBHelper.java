package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 2015年1月13日 11:11:23
 */
@SpringBootTest
public class TestDBHelper {
	
	@Autowired
	private DBHelper dbHelper;
    @Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 测试new dbHelper
	 */
	@Test
	public void testNewDBHelper() {
		DBHelper dbHelper2 = new SpringJdbcDBHelper(jdbcTemplate);
		assert dbHelper.getAll(StudentDO.class).size() == dbHelper2.getAll(StudentDO.class).size();

		// named jdbc template并不是必要的，但是保留这个这个口子，两个目的：向下兼容、提供给需要定制化nameJdbc的扩展
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		SpringJdbcDBHelper dbHelper3 = new SpringJdbcDBHelper();
		dbHelper3.setJdbcTemplate(jdbcTemplate);
		dbHelper3.setNamedParameterJdbcTemplate(namedParameterJdbcTemplate);
		List<StudentDO> all = dbHelper.getAll(StudentDO.class);
		assert all.size() == dbHelper3.getRaw(StudentDO.class,
				"select * from t_student where id in (?)", ListUtils.transform(all, o -> o.getId())).size();
	}

	@Test
	public void testGlobalComment() throws Exception {
		String globalComment = UUID.randomUUID().toString();
		DBHelper.setGlobalComment(globalComment);

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
		String localComment = UUID.randomUUID().toString();
		DBHelper.setLocalComment(localComment);

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
