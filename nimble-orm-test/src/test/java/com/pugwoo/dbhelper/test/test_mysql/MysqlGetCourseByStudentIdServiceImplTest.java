package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class MysqlGetCourseByStudentIdServiceImplTest {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void test() {
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName(UUID.randomUUID().toString().substring(0, 16));
        assert dbHelper.insert(schoolDO) == 1;

        MysqlGetCourseByStudentIdServiceImpl.StudentVO student = new MysqlGetCourseByStudentIdServiceImpl.StudentVO();
        student.setName(UUID.randomUUID().toString());
        student.setSchoolId(schoolDO.getId());
        assert dbHelper.insert(student) == 1;

        CourseDO course = new CourseDO();
        course.setName(UUID.randomUUID().toString().substring(0, 16));
        course.setStudentId(student.getId());
        assert dbHelper.insert(course) == 1;

        MysqlGetCourseByStudentIdServiceImpl.StudentVO student2 = dbHelper.getOne(
                MysqlGetCourseByStudentIdServiceImpl.StudentVO.class, "where name=?", student.getName());
        assert student2.getSchoolDO().getName().equals(schoolDO.getName());
        assert student2.getCourses().size() == 1;
        assert student2.getCourses().get(0).getName().equals(course.getName());
    }

}
