package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.service.WithTransactionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test8Feature_Transaction {

    @Autowired
    private DBHelper dbHelper;
    @Autowired
    private WithTransactionService withTransactionService;

    /**测试事务是否生效*/
    @Test
    public void testTransactionEnabled() {
        long count = dbHelper.getCount(StudentDO.class);
        withTransactionService.insertOne(false);
        long count2 = dbHelper.getCount(StudentDO.class);

        assert count + 1 == count2;

        count = dbHelper.getCount(StudentDO.class);
        try {
            withTransactionService.insertOne(true);
        } catch (Exception ignored) {
        }
        count2 = dbHelper.getCount(StudentDO.class);

        assert count == count2;
    }

    @Test
    public void testRunAfterTransaction() {
        withTransactionService.setIsAfterCommitRun(false);
        withTransactionService.insertOneWithAfterCommit(false);
        assert withTransactionService.getIsAfterCommitRun();

        withTransactionService.setIsAfterCommitRun(false);
        try {
            withTransactionService.insertOneWithAfterCommit(true);
        } catch (Exception ignored) {
        }
        assert !withTransactionService.getIsAfterCommitRun();
    }

    @Test
    public void testManualRollback() {
        long count = dbHelper.getCount(StudentDO.class);
        withTransactionService.manualRollback(true);
        long count2 = dbHelper.getCount(StudentDO.class);

        assert count == count2;

        count = dbHelper.getCount(StudentDO.class);
        withTransactionService.manualRollback(false);
        count2 = dbHelper.getCount(StudentDO.class);

        assert count + 1== count2;
    }

}
