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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class PostgresqlDBHelperConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.postgresql")
    public DataSource postgresqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "postgresqlJdbcTemplate")
    public JdbcTemplate postgresqlJdbcTemplate(@Qualifier("postgresqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager postgresqlTransactionManager(@Qualifier("postgresqlDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DBHelper postgresqlDbHelper(@Qualifier("postgresqlJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        dbHelper.turnOnFeature(FeatureEnum.LOG_SQL_AT_INFO_LEVEL);
        return dbHelper;
    }

    @Bean
    public DBHelper postgresqlDbHelperWithDefaultInterceptor(@Qualifier("postgresqlJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new DefaultInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }

    @Bean
    public DBHelper postgresqlDbHelperWithInterceptor(@Qualifier("postgresqlJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new MyLogChangeInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }

    @Bean
    public DBHelper postgresqlDbHelperWithNotAllowInterceptor(@Qualifier("postgresqlJdbcTemplate") JdbcTemplate jdbcTemplate) {
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper(jdbcTemplate);
        dbHelper.setTimeoutWarningValve(1000); // 超过1秒的话就告警
        List<DBHelperInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new NotAllowInterceptor());
        dbHelper.setInterceptors(interceptors);
        return dbHelper;
    }
}
