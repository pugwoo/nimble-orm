package com.pugwoo.dbhelper.test.test_clickhouse;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.test_clickhouse.entity.StudentDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestInsert {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testInsert() {
        StudentDO student = new StudentDO();
        student.setId(new Random().nextLong());
        student.setName("nick" + UUID.randomUUID());
        student.setAge(30);

        int rows = dbHelper.insert(student);
        System.out.println(rows);

    }

    @Test
    public void testBatch() {
        List<StudentDO> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            StudentDO student = new StudentDO();
            student.setId(new Random().nextLong());
            student.setName("nick" + UUID.randomUUID());
            student.setAge(30);
            student.setSchoolId(1000L);

            list.add(student);
        }

        dbHelper.insert(list);
    }

}
