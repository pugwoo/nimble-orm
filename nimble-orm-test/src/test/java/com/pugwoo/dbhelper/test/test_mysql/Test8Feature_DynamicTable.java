package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test8Feature_DynamicTable extends com.pugwoo.dbhelper.test.test_common.Test8Feature_DynamicTable {

    @Autowired
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

}
