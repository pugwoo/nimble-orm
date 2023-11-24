package com.pugwoo.dbhelper.test.test_postgresql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test7Interceptor_Custom extends com.pugwoo.dbhelper.test.test_common.Test7Interceptor_Custom {

    @Autowired
    @Qualifier("postgresqlDbHelperWithInterceptor")
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

}
