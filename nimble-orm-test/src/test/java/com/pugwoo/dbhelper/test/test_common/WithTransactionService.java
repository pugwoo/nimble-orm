package com.pugwoo.dbhelper.test.test_common;

public interface WithTransactionService {

    void insertOne(boolean isThrowException);

    void insertOneWithAfterCommit(boolean isThrowException);

    boolean getIsAfterCommitRun();

    void setIsAfterCommitRun(boolean b);

    void manualRollback(boolean isRollback);

}
