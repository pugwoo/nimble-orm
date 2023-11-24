package com.pugwoo.dbhelper.test.test_postgresql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test8Feature_Comment extends com.pugwoo.dbhelper.test.test_common.Test8Feature_Comment {

    @Autowired @Qualifier("postgresqlDbHelper")
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

}
