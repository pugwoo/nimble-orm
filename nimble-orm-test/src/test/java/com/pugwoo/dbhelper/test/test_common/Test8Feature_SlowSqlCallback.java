package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class Test8Feature_SlowSqlCallback {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testSlowLog() {
        final StringBuilder sb = new StringBuilder();

        dbHelper.setTimeoutWarningValve(1);
        dbHelper.setTimeoutWarningCallback((executeMsTime, sql, args) -> {
            System.out.println("==in slow callback== execMs:" + executeMsTime + "ms,"
                    + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args));
            sb.append(sql);
        });

        StudentDO stu1 = new StudentDO();
        stu1.setName(UUID.randomUUID().toString().replace("-", ""));

        assert dbHelper.insert(stu1) == 1;

        assert !sb.toString().isEmpty();

        dbHelper.setTimeoutWarningValve(1000);
        dbHelper.setTimeoutWarningCallback(null);
    }

    @Test
    public void testSlowLogEx() {
        final StringBuilder sb = new StringBuilder();

        dbHelper.setTimeoutWarningValve(1);
        dbHelper.setTimeoutWarningCallback((executeMsTime, sql, args) -> {
            if (true) {
                throw new RuntimeException("just test");
            }
            sb.append(sql);
        });

        StudentDO stu1 = new StudentDO();
        stu1.setName(UUID.randomUUID().toString().replace("-", ""));

        assert dbHelper.insert(stu1) == 1; // 不会受callback抛出异常的影响

        assert sb.toString().isEmpty();

        dbHelper.setTimeoutWarningValve(1000);
        dbHelper.setTimeoutWarningCallback(null);
    }

    @Test
    public void testSlowLogForBatch() {
        final StringBuilder sb = new StringBuilder();

        dbHelper.setTimeoutWarningValve(1);
        dbHelper.setTimeoutWarningCallback((executeMsTime, sql, args) -> {
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

        assert dbHelper.insertBatchWithoutReturnId(students) == 2;

        assert !sb.toString().isEmpty();

        dbHelper.setTimeoutWarningValve(1000);
        dbHelper.setTimeoutWarningCallback(null);
    }

    @Test
    public void testSlowLogForBatchEx() {
        final StringBuilder sb = new StringBuilder();

        dbHelper.setTimeoutWarningValve(1);
        dbHelper.setTimeoutWarningCallback((executeMsTime, sql, args) -> {
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

        assert dbHelper.insertBatchWithoutReturnId(students) == 2; // 不会受callback抛出异常的影响

        assert sb.toString().isEmpty();

        dbHelper.setTimeoutWarningValve(1000);
        dbHelper.setTimeoutWarningCallback(null);
    }

}
