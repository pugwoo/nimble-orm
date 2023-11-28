package com.pugwoo.dbhelper.test.test_postgresql;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.test_common.WithTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test8Feature_Transaction extends com.pugwoo.dbhelper.test.test_common.Test8Feature_Transaction {

    @Autowired
    private PostgresqlWithTransactionService withTransactionService;

    @Autowired @Qualifier("postgresqlDbHelper")
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

    @Override
    public WithTransactionService getWithTransactionService() {
        return withTransactionService;
    }

}
