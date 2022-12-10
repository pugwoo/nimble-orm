package com.pugwoo.dbhelper.test.test_clickhouse;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.test_clickhouse.entity.StudentDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CkCommonOps {

    public static void insertOne(DBHelper dbHelper) {
        StudentDO student = new StudentDO();
        student.setId(new Random().nextLong());
        student.setName("nick" + UUID.randomUUID());
        student.setAge(30);
        student.setSchoolId(1000L);

        int rows = dbHelper.insert(student);
       assert rows == 1;
    }

    public static void insertSome(DBHelper dbHelper, int num) {
        List<StudentDO> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            StudentDO student = new StudentDO();
            student.setId(new Random().nextLong());
            student.setName("nick" + UUID.randomUUID());
            student.setAge(30);
            student.setSchoolId(1000L);

            list.add(student);
        }

        assert dbHelper.insert(list) == num;
    }

}
