package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DBHelperConfiguration {

    @Primary
    @Bean
    public DBHelper dbHelper(JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        return dbHelper;
    }

}
