package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperSqlCallback;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.model.RowData;
import com.pugwoo.dbhelper.model.RunningSqlData;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.thread.ThreadPoolUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Test8Feature_SlowSqlCallback {

    public abstract DBHelper getDBHelper();

    @Test
    public void testLogCallback() {
        getDBHelper().setSqlCallback(new DBHelperSqlCallback() {
            @Override
            public void beforeExecute(String sql, List<Object> args, int batchSize) {
                System.out.println("==in before callback== sql:" + sql + "args:" + NimbleOrmJSON.toJson(args) + ",batchSize:" + batchSize);
            }

            @Override
            public void afterExecute(long executeMsTime, String sql, List<Object> args, int batchSize) {
                System.out.println("==in after callback== execMs:" + executeMsTime + "ms,"
                        + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args) + ",batchSize:" + batchSize);
            }
        });

        StudentDO stu1 = CommonOps.insertOne(getDBHelper(), UUID.randomUUID().toString().replace("-", ""));
        StudentDO stu2 = getDBHelper().getOne(StudentDO.class, "where id=?", stu1.getId());
        assert stu2.getName().equals(stu1.getName());
        // 说明：这里手工检查一下输出
    }

    @Test
    public void testSlowLog() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setSlowSqlWarningValve(1);
        getDBHelper().setSlowSqlWarningCallback((executeMsTime, sql, args, batchSize) -> {
            System.out.println("==in slow callback== execMs:" + executeMsTime + "ms,"
                    + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args));
            sb.append(sql);
        });

        StudentDO stu1 = CommonOps.insertOne(getDBHelper(), UUID.randomUUID().toString().replace("-", ""));

        getDBHelper().getAll(StudentDO.class); // 会触发慢sql回调
        getDBHelper().getAll(StudentDO.class, "where id=?", stu1.getId()); // 会触发慢sql回调

        assert !sb.toString().isEmpty();

        getDBHelper().setSlowSqlWarningValve(1000);
        getDBHelper().setSlowSqlWarningCallback(null);
    }

    @Test
    public void testSlowLogEx() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setSlowSqlWarningValve(1);
        getDBHelper().setSlowSqlWarningCallback((executeMsTime, sql, args, batchSize) -> {
            if (true) {
                throw new RuntimeException("just test");
            }
            sb.append(sql);
        });

        // 不会受callback抛出异常的影响
        CommonOps.insertOne(getDBHelper(), UUID.randomUUID().toString().replace("-", ""));

        assert sb.toString().isEmpty();

        getDBHelper().setSlowSqlWarningValve(1000);
        getDBHelper().setSlowSqlWarningCallback(null);
    }

    @Test
    public void testSlowLogForBatch() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setSlowSqlWarningValve(1);
        getDBHelper().setSlowSqlWarningCallback((executeMsTime, sql, args, batchSize) -> {
            System.out.println("==in slow callback== execMs:" + executeMsTime + "ms,"
                    + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args));
            sb.append(sql);
        });

        List<StudentDO> students = new ArrayList<>();
        StudentDO stu1 = new StudentDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            stu1.setId(CommonOps.getRandomLong());
        }
        stu1.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu1);

        StudentDO stu2 = new StudentDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            stu2.setId(CommonOps.getRandomLong());
        }
        stu2.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu2);

        assert getDBHelper().insertBatchWithoutReturnId(students) == 2;

        assert !sb.toString().isEmpty();

        getDBHelper().setSlowSqlWarningValve(1000);
        getDBHelper().setSlowSqlWarningCallback(null);
    }

    @Test
    public void testSlowLogForBatchEx() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setSlowSqlWarningValve(1);
        getDBHelper().setSlowSqlWarningCallback((executeMsTime, sql, args, batchSize) -> {
            if (true) {
                throw new RuntimeException("just test");
            }
            sb.append(sql);
        });

        List<StudentDO> students = new ArrayList<>();
        StudentDO stu1 = new StudentDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            stu1.setId(CommonOps.getRandomLong());
        }
        stu1.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu1);

        StudentDO stu2 = new StudentDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            stu2.setId(CommonOps.getRandomLong());
        }
        stu2.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu2);

        assert getDBHelper().insertBatchWithoutReturnId(students) == 2; // 不会受callback抛出异常的影响

        assert sb.toString().isEmpty();

        getDBHelper().setSlowSqlWarningValve(1000);
        getDBHelper().setSlowSqlWarningCallback(null);
    }

    /**查询正在执行的sql*/
    @Test
    public void testGetRunningSql() throws Exception {

        ThreadPoolExecutor pool = ThreadPoolUtils.createThreadPool(2, 100, 2, "test");
        for (int i = 0; i < 2; i++) {
            final int finalI = i;
            pool.execute(() -> {
                if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.POSTGRESQL) {
                    getDBHelper().getRaw(RowData.class, "select pg_sleep(1) as ab89defded" + finalI);
                } else {
                    getDBHelper().getRaw(RowData.class, "select sleep(1) as ab89defded" + finalI);
                }
            });
        }

        Thread.sleep(500);

        Collection<RunningSqlData> runningSql = DBHelperContext.getRunningSql();
        assert runningSql.size() == 2;
        for (RunningSqlData runningSqlData : runningSql) {
            assert runningSqlData.getSql().contains("sleep(1) as ab89defded");
        }

        ThreadPoolUtils.shutdownAndWaitAllTermination(pool);
    }

}
