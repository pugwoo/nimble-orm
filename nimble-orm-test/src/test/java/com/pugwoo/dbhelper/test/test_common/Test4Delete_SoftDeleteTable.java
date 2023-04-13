package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class Test4Delete_SoftDeleteTable {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testDelete() {
        List<StudentDO> studentDOS = CommonOps.insertBatch(dbHelper, 1);

        // 设置完整的字段
        StudentDO studentDO = studentDOS.get(0);
        studentDO.setAge(new Random().nextInt(100));
        studentDO.setIntro("i like basketball".getBytes());
        long schoolId = new Random().nextLong();
        studentDO.setSchoolId(schoolId % 100000L);
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setId(studentDO.getSchoolId());
        schoolDO.setName(UUID.randomUUID().toString());
        studentDO.setSchoolSnapshot(schoolDO);

        List<CourseDO> courses = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CourseDO courseDO = new CourseDO();
            courseDO.setId(new Random().nextLong() % 100000L);
            courseDO.setName(UUID.randomUUID().toString());
            courses.add(courseDO);
        }
        studentDO.setCourseSnapshot(courses);

        assert dbHelper.update(studentDO) == 1;

        assert dbHelper.delete(studentDOS.get(0)) == 1;

        // 测试数据已经被删除了
        assert dbHelper.getOne(StudentDO.class, "where id=?", studentDO.getId()) == null;

        // 从另外一张表里查出数据，再比较
        Map<Class<?>, String> tableNames = new HashMap<>();
        tableNames.put(StudentDO.class, "t_student_del");

        DBHelper.withTableNames(tableNames, () -> {
            StudentDO s = dbHelper.getOne(StudentDO.class, "where id=?", studentDO.getId());
            assert s != null;
            assert s.getId().equals(studentDO.getId());
            assert s.getCreateTime().equals(studentDO.getCreateTime());
            assert s.getName().equals(studentDO.getName());
            assert s.getAge().equals(studentDO.getAge());
            assert new String(s.getIntro()).equals(new String(studentDO.getIntro()));
            assert s.getSchoolId().equals(studentDO.getSchoolId());
            assert s.getSchoolSnapshot().getId().equals(studentDO.getSchoolSnapshot().getId());
            assert s.getSchoolSnapshot().getName().equals(studentDO.getSchoolSnapshot().getName());

            assert NimbleOrmJSON.toJson(s.getCourseSnapshot()).equals(NimbleOrmJSON.toJson(studentDO.getCourseSnapshot()));
        });

    }

}
