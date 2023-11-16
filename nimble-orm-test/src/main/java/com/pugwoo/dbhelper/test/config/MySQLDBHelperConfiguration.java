package com.pugwoo.dbhelper.test.config;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.test.interceptor.DefaultInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MySQLDBHelperConfiguration {

    // mysql

    @Primary
    @Bean("mysqlDataSourceProperties")
    @ConfigurationProperties("spring.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean("mysqlDataSource")
    // 特别注意这一行，这样才能使hikari配置生效，配置加大了连接池的大小
    @ConfigurationProperties(prefix = "spring.datasource.mysql.hikari")
    public HikariDataSource mysqlDataSource() {
        return mysqlDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

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
        interceptors.add(new DefaultInterceptor());
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
