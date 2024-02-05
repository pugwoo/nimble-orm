package com.pugwoo.dbhelper.test.config;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.test.interceptor.DefaultInterceptor;
import com.pugwoo.dbhelper.test.interceptor.MyLogChangeInterceptor;
import com.pugwoo.dbhelper.test.interceptor.NotAllowInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ClickhouseDBHelperConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.clickhouse")
    public DataSource clickhouseDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "clickhouseJdbcTemplate")
    public JdbcTemplate clickhouseJdbcTemplate(@Qualifier("clickhouseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DBHelper clickhouseDbHelper(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setSlowSqlWarningValve(1000); // 超过1秒的话就告警
        dbHelper.turnOnFeature(FeatureEnum.LOG_SQL_AT_INFO_LEVEL);
        return dbHelper;
    }

    @Bean
    public DBHelper clickhouseDbHelperWithDefaultInterceptor(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setSlowSqlWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new DefaultInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }

    @Bean
    public DBHelper clickhouseDbHelperWithInterceptor(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setSlowSqlWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new MyLogChangeInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }

    @Bean
    public DBHelper clickhouseDbHelperWithNotAllowInterceptor(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setSlowSqlWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new NotAllowInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }
}
