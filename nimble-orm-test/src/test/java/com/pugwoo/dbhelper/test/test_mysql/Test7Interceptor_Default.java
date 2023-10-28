package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test7Interceptor_Default extends com.pugwoo.dbhelper.test.test_common.Test7Interceptor_Default {

    @Autowired
    @Qualifier("dbHelperWithDefaultInterceptor")
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

}
