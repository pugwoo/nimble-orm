package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test7Interceptor_NotAllow extends com.pugwoo.dbhelper.test.test_common.Test7Interceptor_NotAllow {

    @Autowired
    @Qualifier("dbHelperWithNotAllowInterceptor")
    private DBHelper dbHelper;

    @Autowired
    @Qualifier("dbHelper")
    private DBHelper nativeDBHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

    @Override
    public DBHelper getNativeDBHelper() {
        return nativeDBHelper;
    }

}
