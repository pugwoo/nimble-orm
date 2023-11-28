package com.pugwoo.dbhelper.test.test_postgresql;

import com.pugwoo.dbhelper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test1Query_RelatedColumn extends com.pugwoo.dbhelper.test.test_common.Test1Query_RelatedColumn {

    @Autowired @Qualifier("postgresqlDbHelper")
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }
    
}
