package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DBHelperConfiguration {

    @Primary
    @Bean
    public DBHelper dbHelper(JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        dbHelper.turnOnFeature(FeatureEnum.LOG_SQL_AT_INFO_LEVEL);
        return dbHelper;
    }

    @Bean
    public DBHelper dbHelperWithDefaultInterceptor(JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new com.pugwoo.dbhelper.DBHelperInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }

    @Bean
    public DBHelper dbHelperWithInterceptor(JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new com.pugwoo.dbhelper.test.interceptor.MyLogChangeInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }

    @Bean
    public DBHelper dbHelperWithNotAllowInterceptor(JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new com.pugwoo.dbhelper.test.interceptor.NotAllowInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }

}
