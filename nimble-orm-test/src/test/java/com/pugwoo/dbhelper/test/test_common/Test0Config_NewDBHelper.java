package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.test.entity.IdableSoftDeleteBaseDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

/**
 * 2015年1月13日 11:11:23
 */
public abstract class Test0Config_NewDBHelper {

	public abstract DBHelper getDBHelper();

	public abstract JdbcTemplate getJdbcTemplate();

	/**
	 * 测试new dbHelper
	 */
	@Test
	public void testNewDBHelper() {
		DBHelper dbHelper2 = new SpringJdbcDBHelper(getJdbcTemplate());
		assert getDBHelper().getAll(StudentDO.class, "limit 987").size() == dbHelper2.getAll(StudentDO.class, "limit 987").size();

		// named jdbc template并不是必要的，但是保留这个这个口子，两个目的：向下兼容、提供给需要定制化nameJdbc的扩展
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
		SpringJdbcDBHelper dbHelper3 = new SpringJdbcDBHelper();
		dbHelper3.setJdbcTemplate(getJdbcTemplate());
		dbHelper3.setNamedParameterJdbcTemplate(namedParameterJdbcTemplate);
		List<StudentDO> all = getDBHelper().getAll(StudentDO.class, "limit 987");
		assert all.size() == dbHelper3.getRaw(StudentDO.class,
				"select * from t_student where id in (?) ", ListUtils.transform(all, IdableSoftDeleteBaseDO::getId)).size();

		// 测试获取jdbc
		assert dbHelper3.getJdbcTemplate().equals(getJdbcTemplate());
		assert dbHelper3.getNamedParameterJdbcTemplate().equals(namedParameterJdbcTemplate);
	}


}
