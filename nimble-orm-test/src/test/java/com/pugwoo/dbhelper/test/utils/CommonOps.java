package com.pugwoo.dbhelper.test.utils;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CommonOps {

    public static String getRandomName(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static int getRandomInt(int base, int bound) {
        return base + new Random().nextInt(bound);
    }

    public static StudentDO insertOne(DBHelper dbHelper, String name) {
        StudentDO studentDO = new StudentDO();
        studentDO.setName(name);

        if (dbHelper.getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            studentDO.setId(new Random().nextLong());
        }

        assert dbHelper.insert(studentDO) == 1;
        return  studentDO;
    }

    public static StudentDO insertOne(DBHelper dbHelper, Long schoolId) {
        StudentDO studentDO = new StudentDO();
        studentDO.setSchoolId(schoolId);

        if (dbHelper.getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            studentDO.setId(new Random().nextLong());
        }

        assert dbHelper.insert(studentDO) == 1;
        return studentDO;
    }

    public static StudentDO insertOne(DBHelper dbHelper) {
        StudentDO studentDO = new StudentDO();
        if (dbHelper.getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            studentDO.setId(new Random().nextLong());
        }

        studentDO.setName(getRandomName("nick"));
        studentDO.setIntro(studentDO.getName().getBytes());
        assert dbHelper.insert(studentDO) == 1;
        return studentDO;
    }

    public static List<StudentDO> insertBatch(DBHelper dbHelper, int num) {
        return insertBatch(dbHelper, num, "nick");
    }

    public static List<StudentDO> insertBatch(DBHelper dbHelper, int num, String prefix) {
        List<StudentDO> list = new ArrayList<StudentDO>();
        for(int i = 0; i < num; i++) {
            StudentDO studentDO = new StudentDO();

            // clickhouse不支持自增id，所以对于clickhouse自动设置一个随机id
            DatabaseTypeEnum databaseType = dbHelper.getDatabaseType();
            if (databaseType == DatabaseTypeEnum.CLICKHOUSE) {
                studentDO.setId(Math.abs(new Random().nextLong()));
            }

            studentDO.setName(getRandomName(prefix));
            list.add(studentDO);
        }

        int rows = dbHelper.insert(list);
        assert rows == num;

        return list;
    }

    public static List<StudentDO> insertBatchNoReturnId(DBHelper dbHelper, int num) {
        return insertBatchNoReturnId(dbHelper, num, "nick");
    }

    public static List<StudentDO> insertBatchNoReturnId(DBHelper dbHelper, int num, String prefix) {
        List<StudentDO> list = new ArrayList<StudentDO>();
        for(int i = 0; i < num; i++) {
            StudentDO studentDO = new StudentDO();
            studentDO.setName(getRandomName(prefix));
            list.add(studentDO);
        }

        int rows = dbHelper.insertBatchWithoutReturnId(list);
        assert rows == num;

        return list;
    }

    public static SchoolDO insertOneSchoolDO(DBHelper dbHelper, String name) {
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName(name);
        if (dbHelper.getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            schoolDO.setId(new Random().nextLong());
        }
        assert dbHelper.insert(schoolDO) == 1;
        return schoolDO;
    }

    public static CourseDO insertOneCourseDO(DBHelper dbHelper, String name, Long studentId) {
        CourseDO courseDO = new CourseDO();
        courseDO.setName(name);
        courseDO.setStudentId(studentId);
        if (dbHelper.getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            courseDO.setId(new Random().nextLong());
        }
        assert dbHelper.insert(courseDO) == 1;
        return courseDO;
    }

    public static CourseDO insertOneCourseDO(DBHelper dbHelper, String name, Long studentId, boolean isMain) {
        CourseDO courseDO = new CourseDO();
        courseDO.setName(name);
        courseDO.setStudentId(studentId);
        if (dbHelper.getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            courseDO.setId(new Random().nextLong());
        }
        courseDO.setIsMain(isMain);
        assert dbHelper.insert(courseDO) == 1;
        return courseDO;
    }

}
