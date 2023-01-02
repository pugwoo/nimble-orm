package com.pugwoo.dbhelper.test.test_clickhouse;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.test_clickhouse.entity.StudentDO;

import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
        System.out.println(rows);

    }

    @Test
    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'clickhouse'}", loadContext = true)
    public void testBatch() {
        int total = 1000;

        List<StudentDO> list = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            StudentDO student = new StudentDO();
            student.setId(new Random().nextLong());
            student.setName("nick" + UUID.randomUUID());
            student.setAge(30);
            student.setSchoolId(1000L);

            list.add(student);
        }

        int row = dbHelper.insertBatchWithoutReturnId(list);
        assert row == total;

        List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where name in (?)",
                ListUtils.transform(list, o -> o.getName()));
        assert all.size() == total;
    }

}
