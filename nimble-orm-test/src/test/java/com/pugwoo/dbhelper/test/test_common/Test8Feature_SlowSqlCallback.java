package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Test8Feature_SlowSqlCallback {

    public abstract DBHelper getDBHelper();

    @Test
    public void testSlowLog() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setTimeoutWarningValve(1);
        getDBHelper().setTimeoutWarningCallback((executeMsTime, sql, args) -> {
            System.out.println("==in slow callback== execMs:" + executeMsTime + "ms,"
                    + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args));
            sb.append(sql);
        });

        StudentDO stu1 = CommonOps.insertOne(getDBHelper(), UUID.randomUUID().toString().replace("-", ""));

        getDBHelper().getAll(StudentDO.class); // 会触发慢sql回调
        getDBHelper().getAll(StudentDO.class, "where id=?", stu1.getId()); // 会触发慢sql回调

        assert !sb.toString().isEmpty();

        getDBHelper().setTimeoutWarningValve(1000);
        getDBHelper().setTimeoutWarningCallback(null);
    }

    @Test
    public void testSlowLogEx() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setTimeoutWarningValve(1);
        getDBHelper().setTimeoutWarningCallback((executeMsTime, sql, args) -> {
            if (true) {
                throw new RuntimeException("just test");
            }
            sb.append(sql);
        });

        // 不会受callback抛出异常的影响
        CommonOps.insertOne(getDBHelper(), UUID.randomUUID().toString().replace("-", ""));

        assert sb.toString().isEmpty();

        getDBHelper().setTimeoutWarningValve(1000);
        getDBHelper().setTimeoutWarningCallback(null);
    }

    @Test
    public void testSlowLogForBatch() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setTimeoutWarningValve(1);
        getDBHelper().setTimeoutWarningCallback((executeMsTime, sql, args) -> {
            System.out.println("==in slow callback== execMs:" + executeMsTime + "ms,"
                    + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args));
            sb.append(sql);
        });

        List<StudentDO> students = new ArrayList<>();
        StudentDO stu1 = new StudentDO();
        stu1.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu1);

        StudentDO stu2 = new StudentDO();
        stu2.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu2);

        assert getDBHelper().insertBatchWithoutReturnId(students) == 2;

        assert !sb.toString().isEmpty();

        getDBHelper().setTimeoutWarningValve(1000);
        getDBHelper().setTimeoutWarningCallback(null);
    }

    @Test
    public void testSlowLogForBatchEx() {
        final StringBuilder sb = new StringBuilder();

        getDBHelper().setTimeoutWarningValve(1);
        getDBHelper().setTimeoutWarningCallback((executeMsTime, sql, args) -> {
            if (true) {
                throw new RuntimeException("just test");
            }
            sb.append(sql);
        });

        List<StudentDO> students = new ArrayList<>();
        StudentDO stu1 = new StudentDO();
        stu1.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu1);

        StudentDO stu2 = new StudentDO();
        stu2.setName(UUID.randomUUID().toString().replace("-", ""));
        students.add(stu2);

        assert getDBHelper().insertBatchWithoutReturnId(students) == 2; // 不会受callback抛出异常的影响

        assert sb.toString().isEmpty();

        getDBHelper().setTimeoutWarningValve(1000);
        getDBHelper().setTimeoutWarningCallback(null);
    }

}
