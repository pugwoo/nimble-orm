package com.pugwoo.dbhelper.test.test_postgresql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class Test0Config_NewDBHelper extends com.pugwoo.dbhelper.test.test_common.Test0Config_NewDBHelper {

    @Autowired @Qualifier("postgresqlDbHelper")
    private DBHelper dbHelper;
    @Autowired @Qualifier("postgresqlJdbcTemplate")
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
