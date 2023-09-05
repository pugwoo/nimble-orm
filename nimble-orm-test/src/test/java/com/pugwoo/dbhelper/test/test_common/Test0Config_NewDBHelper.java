package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

/**
 * 2015年1月13日 11:11:23
 */
@SpringBootTest
public class Test0Config_NewDBHelper {
	
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

		// 测试获取jdbc
		assert dbHelper3.getJdbcTemplate().equals(jdbcTemplate);
		assert dbHelper3.getNamedParameterJdbcTemplate().equals(namedParameterJdbcTemplate);
	}


}
