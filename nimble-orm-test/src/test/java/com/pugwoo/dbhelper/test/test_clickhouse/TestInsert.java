package com.pugwoo.dbhelper.test.test_clickhouse;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.test_clickhouse.entity.StudentDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.*;

@SpringBootTest
public class TestInsert {

    @Autowired
    private DBHelper dbHelper;

    @Test
    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'clickhouse'}", loadContext = true)
    public void testInsert() {
        StudentDO student = new StudentDO();
        student.setId(new Random().nextLong());
        student.setName("nick" + UUID.randomUUID());
        student.setAge(30);

        int rows = dbHelper.insert(student);
        assert rows == 1;
    }

    @Test
    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'clickhouse'}", loadContext = true)
    public void testInsertNullValue() {
        // case 1: 对于非null字段特意指定了null值
        StudentDO student = new StudentDO();
        student.setId(new Random().nextLong());
        student.setName(null);

        int rows = dbHelper.insertWithNull(student);
        assert rows == 1;

        // case 2: 对于非null字段，没有在insert语句中出现该字段
        student = new StudentDO();
        student.setId(new Random().nextLong());

        rows = dbHelper.insert(student);
        assert rows == 1;
    }

    @Test
    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'clickhouse'}", loadContext = true)
    public void testBatch() {
        int total = 99999;

        long randomId = new Random().nextLong();

        List<StudentDO> list = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            StudentDO student = new StudentDO();
            student.setId(randomId);
            student.setName("");
            student.setAge(null);
            student.setSchoolId(1000L);

            list.add(student);
        }

        // 再加一个name没有null，这样name字段就有非null和有null的了
        StudentDO student = new StudentDO();
        student.setId(randomId);
        student.setName("nick" + UUID.randomUUID());
        student.setAge(30);
        student.setSchoolId(1000L);
        list.add(student);

        long start = System.currentTimeMillis();
        int row = dbHelper.insertBatchWithoutReturnId(list);
        long end = System.currentTimeMillis();
        System.out.println("insert " + total + " rows, cost " + (end - start) + " ms");
        assert row == total + 1;

        assert end - start < 5000; // 10万条插入，在5秒内完成算合格，网络原因，有时快有时慢

        // 回查校验一下数据
        List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where id = ?", randomId);
        assert all.size() == total + 1;
        for (StudentDO s : all) {
            assert s.getSchoolId().equals(1000L);
        }
    }

    @Test
    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'clickhouse'}", loadContext = true)
    public void testInsertBatchWithoutReturnIdWithMapList() {
        int TOTAL = 1000;
        String uuidName = uuidName();

        Random random = new Random();
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < TOTAL - TOTAL / 2; i++) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("deleted", 0);
            studentMap.put("name", uuidName);
            studentMap.put("age", 0);
            studentMap.put("school_id", random.nextInt());
            list.add(studentMap);
        }
        for (int i = 0; i < TOTAL / 2; i++) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("deleted", 0);
            studentMap.put("name", uuidName);
            studentMap.put("age", 0);
            // 故意少掉1个属性
            list.add(studentMap);
        }

        dbHelper.setTimeoutWarningValve(1); // 改小超时阈值，让慢sql打印出来

        long start = System.currentTimeMillis();
        int rows = dbHelper.insertBatchWithoutReturnId("t_student", list);
        long end = System.currentTimeMillis();
        System.out.println("batch insert cost:" + (end - start) + "ms");
        assert rows == TOTAL;

        assert dbHelper.getAll(StudentDO.class, "where name=?", uuidName).size() == TOTAL;
    }

    @Test
    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'clickhouse'}", loadContext = true)
    public void testInsertBatchWithoutReturnIdWithColsAndData() {
        int TOTAL = 1000;
        String uuidName = uuidName();

        Random random = new Random();
        List<String> cols = new ArrayList<>();
        cols.add("deleted");
        cols.add("name");
        cols.add("age");
        cols.add("school_id");
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            Object[] args = new Object[]{0, uuidName, "0", random.nextInt()}; // age故意用字符串，测试转换
            data.add(args);
        }

        dbHelper.setTimeoutWarningValve(1); // 改小超时阈值，让慢sql打印出来

        long start = System.currentTimeMillis();
        int rows = dbHelper.insertBatchWithoutReturnId("t_student", cols, data);
        long end = System.currentTimeMillis();
        System.out.println("batch insert cost:" + (end - start) + "ms");
        assert rows == TOTAL;

        assert dbHelper.getAll(StudentDO.class, "where name=?", uuidName).size() == TOTAL;
    }

    private String uuidName() {
        return UUID.randomUUID().toString().replace("-", "");
    }


}
