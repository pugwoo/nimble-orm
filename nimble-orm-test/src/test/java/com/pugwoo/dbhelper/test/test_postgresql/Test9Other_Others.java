package com.pugwoo.dbhelper.test.test_postgresql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test9Other_Others extends com.pugwoo.dbhelper.test.test_common.Test9Other_Others {

    @Autowired @Qualifier("postgresqlDbHelper")
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

}
