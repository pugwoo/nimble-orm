package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.test_common.WithTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MysqlWithTransactionService implements WithTransactionService {

    @Autowired
    private DBHelper dbHelper;

    public DBHelper getDBHelper() {
        return dbHelper;
    }

    private AtomicBoolean isAfterCommitRun = new AtomicBoolean();

    @Transactional
    public void insertOne(boolean isThrowException) {
        StudentDO studentDO = new StudentDO();
        studentDO.setName(UUID.randomUUID().toString().replace("-", ""));

        getDBHelper().insert(studentDO);

        if (isThrowException) {
            throw new RuntimeException();
        }
    }

    @Transactional
    public void insertOneWithAfterCommit(boolean isThrowException) {
        StudentDO studentDO = new StudentDO();
        studentDO.setName(UUID.randomUUID().toString().replace("-", ""));

        getDBHelper().insert(studentDO);

        // 注册事务提交之后的操作
        assert getDBHelper().executeAfterCommit(() -> isAfterCommitRun.set(true));

        assert !getDBHelper().executeAfterCommit(null); // 测试参数异常情况

        // 在调用之前，都会把isAfterCommitRun设置为false，所以此时事务还没提交，就还是false
        assert !isAfterCommitRun.get();

        if (isThrowException) {
            throw new RuntimeException();
        }
    }

    public boolean getIsAfterCommitRun() {
        return isAfterCommitRun.get();
    }

    public void setIsAfterCommitRun(boolean b) {
        isAfterCommitRun.set(b);
    }

    @Transactional
    public void manualRollback(boolean isRollback) {
        StudentDO studentDO = new StudentDO();
        studentDO.setName(UUID.randomUUID().toString().replace("-", ""));

        getDBHelper().insert(studentDO);

        if (isRollback) {
            getDBHelper().rollback();
        }
    }
}
