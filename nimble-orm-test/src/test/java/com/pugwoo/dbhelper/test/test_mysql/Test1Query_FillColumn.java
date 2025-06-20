package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 测试 @FillColumn 注解功能 - MySQL版本
 */
@SpringBootTest
public class Test1Query_FillColumn extends com.pugwoo.dbhelper.test.test_common.Test1Query_FillColumn {

    @Autowired
    private DBHelper dbHelper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
