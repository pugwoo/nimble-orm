package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootTest
@Transactional
public class TestTransaction {

    @Autowired
    private DBHelper dbHelper;

    /**事务相关测试*/
    @Test
    @Rollback(false)
    public void testTransaction() throws InterruptedException {
        final StudentDO studentDO1 = CommonOps.insertOne(dbHelper);
        final StudentDO studentDO2 = CommonOps.insertOne(dbHelper);

        System.out.println("insert ok, id1:" + studentDO1.getId() +
                ",id2:" + studentDO2.getId());

        AtomicBoolean isAfterCommitRun = new AtomicBoolean(false);

        dbHelper.executeAfterCommit(new Runnable() {
            @Override
            public void run() {
                System.out.println("transaction commit, student1:" + studentDO1.getId()
                        + ",student2:" + studentDO2.getId());
                isAfterCommitRun.set(true);
            }
        });

        System.out.println("myTrans end");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    assert isAfterCommitRun.get();
                    System.out.println("checked isAfterCommitRun");
                } catch (InterruptedException e) {
                }
            }
        }).start();
//		dbHelper.rollback(); // org.springframework.transaction.NoTransactionException
        // throw new RuntimeException(); // 抛出异常也无法让事务回滚
        // 原因：https://stackoverflow.com/questions/13525106/transactions-doesnt-work-in-junit
        // 这意味着rollback()这个方法在单元测试中没法测
    }

}
