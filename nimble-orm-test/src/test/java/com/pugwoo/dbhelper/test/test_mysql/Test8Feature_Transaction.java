package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.test_common.WithTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test8Feature_Transaction extends com.pugwoo.dbhelper.test.test_common.Test8Feature_Transaction {

    @Autowired
    private MysqlWithTransactionService withTransactionService;

    @Autowired
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
