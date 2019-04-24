package com.pugwoo.dbhelper.test.utils;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommonOps {

    private static String getRandomName(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static StudentDO insertOne(DBHelper dbHelper) {
        StudentDO studentDO = new StudentDO();
        studentDO.setName(getRandomName("nick"));
        studentDO.setIntro(studentDO.getName().getBytes());
        dbHelper.insert(studentDO);
        return studentDO;
    }

    public static List<StudentDO> insertBatch(DBHelper dbHelper, int num) {
        List<StudentDO> list = new ArrayList<StudentDO>();
        for(int i = 0; i < num; i++) {
            StudentDO studentDO = new StudentDO();
            studentDO.setName(getRandomName("nick"));
            list.add(studentDO);
        }

        int rows = dbHelper.insert(list);
        Assert.assertTrue(rows == num);

        return list;
    }

}
