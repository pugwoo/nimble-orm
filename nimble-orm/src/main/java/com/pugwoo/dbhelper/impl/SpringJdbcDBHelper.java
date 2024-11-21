package com.pugwoo.dbhelper.impl;

import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.impl.part.P6_ExecuteOp;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 2015年1月12日 16:41:03 数据库操作封装：增删改查
 * @author pugwoo
 */
public class SpringJdbcDBHelper extends P6_ExecuteOp {
	
	// 实现分别安排在impl.part包下几个文件中

    public SpringJdbcDBHelper() {
    }

    public SpringJdbcDBHelper(JdbcTemplate jdbcTemplate) {
        this.setJdbcTemplate(jdbcTemplate);
    }

    /**
     * 支持指定数据库类型，当数据库类型有指定时，不再通过jdbcTemplate判断数据库类型
     * @param jdbcTemplate jdbcTemplate
     * @param databaseType 数据库类型枚举
     */
    public SpringJdbcDBHelper(JdbcTemplate jdbcTemplate, DatabaseTypeEnum databaseType) {
        this.setJdbcTemplate(jdbcTemplate, databaseType);
    }

}
