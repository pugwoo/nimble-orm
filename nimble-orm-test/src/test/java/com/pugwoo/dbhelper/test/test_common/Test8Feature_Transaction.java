package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.service.WithTransactionService;
import org.junit.jupiter.api.Test;

public abstract class Test8Feature_Transaction {

    public abstract DBHelper getDBHelper();

    public abstract WithTransactionService getWithTransactionService();

    /**测试事务是否生效*/
    @Test
    public void testTransactionEnabled() {
        long count = getDBHelper().getCount(StudentDO.class);
        getWithTransactionService().insertOne(false);
        long count2 = getDBHelper().getCount(StudentDO.class);

        assert count + 1 == count2;

        count = getDBHelper().getCount(StudentDO.class);
        try {
            getWithTransactionService().insertOne(true);
        } catch (Exception ignored) {
        }
        count2 = getDBHelper().getCount(StudentDO.class);

        assert count == count2;
    }

    @Test
    public void testRunAfterTransaction() {
        getWithTransactionService().setIsAfterCommitRun(false);
        getWithTransactionService().insertOneWithAfterCommit(false);
        assert getWithTransactionService().getIsAfterCommitRun();

        getWithTransactionService().setIsAfterCommitRun(false);
        try {
            getWithTransactionService().insertOneWithAfterCommit(true);
        } catch (Exception ignored) {
        }
        assert !getWithTransactionService().getIsAfterCommitRun();
    }

    @Test
    public void testManualRollback() {
        long count = getDBHelper().getCount(StudentDO.class);
        getWithTransactionService().manualRollback(true);
        long count2 = getDBHelper().getCount(StudentDO.class);

        assert count == count2;

        count = getDBHelper().getCount(StudentDO.class);
        getWithTransactionService().manualRollback(false);
        count2 = getDBHelper().getCount(StudentDO.class);

        assert count + 1== count2;
    }

}
