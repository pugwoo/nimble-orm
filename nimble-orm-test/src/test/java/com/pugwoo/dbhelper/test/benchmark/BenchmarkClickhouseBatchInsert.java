package com.pugwoo.dbhelper.test.benchmark;

import com.pugwoo.dbhelper.DBHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
public class BenchmarkClickhouseBatchInsert {

    @Autowired
    @Qualifier("clickhouseDbHelper")
    private DBHelper dbHelper;

    @Test
    @EnabledIfSystemProperty(named = "runBenchmarkTest", matches = "true")
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

}
