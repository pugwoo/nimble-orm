package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test3Update_Custom extends com.pugwoo.dbhelper.test.test_common.Test3Update_Custom {

    @Autowired
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

}
