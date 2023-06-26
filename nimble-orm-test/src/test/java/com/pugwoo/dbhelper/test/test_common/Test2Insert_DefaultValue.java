package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.enums.ValueConditionEnum;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.test.entity.IdableSoftDeleteBaseDO;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test2Insert_DefaultValue {

    @Autowired
    private DBHelper dbHelper;

    @Data
    @Table(value = "t_student", insertDefaultValueMap = "mysql")
    public static class StudentDefaultValueDO extends IdableSoftDeleteBaseDO {
        @Column("school_id")
        private Long schoolId;

        @Column(value = "school_snapshot") // 这个数据不是必填，也没有默认值
        private String schoolSnapshot;

        @Column(value = "course_snapshot", insertValueScript = "'aa'") // 这个数据不是必填，也没有默认值
        private String courseSnapshot;
    }

    @Data
    @Table(value = "t_student", insertDefaultValueMap = "mysql2", insertValueCondition = ValueConditionEnum.WHEN_EMPTY)
    public static class StudentDefaultValueDO2 extends IdableSoftDeleteBaseDO {
        @Column("school_id")
        private Long schoolId;

        @Column(value = "school_snapshot") // 这个数据不是必填，也没有默认值
        private String schoolSnapshot;

        @Column(value = "course_snapshot", insertValueScript = "'aa'", insertValueCondition = ValueConditionEnum.WHEN_EMPTY)
        // 这个数据不是必填，也没有默认值
        private String courseSnapshot;
    }

    @Data
    @Table(value = "t_student", insertDefaultValueMap = "mysql2", insertValueCondition = ValueConditionEnum.WHEN_BLANK)
    public static class StudentDefaultValueDO3 extends IdableSoftDeleteBaseDO {
        @Column("school_id")
        private Long schoolId;

        @Column(value = "school_snapshot") // 这个数据不是必填，也没有默认值
        private String schoolSnapshot;

        @Column(value = "course_snapshot", insertValueScript = "'aa'", insertValueCondition = ValueConditionEnum.WHEN_BLANK)
        // 这个数据不是必填，也没有默认值
        private String courseSnapshot;
    }

    @Test
    public void testTableDefaultValueWhenNull() {
        StudentDefaultValueDO student = new StudentDefaultValueDO();
        dbHelper.insert(student);

        StudentDefaultValueDO one = dbHelper.getOne(StudentDefaultValueDO.class, "where id=?", student.getId());
        assert one.getSchoolSnapshot().equals("");
        assert one.getSchoolId().equals(0L);
        assert one.getCourseSnapshot().equals("aa");
    }

    @Test
    public void testTableDefaultValueWhenEmpty() {
        DBHelperContext.setInsertDefaultValue("mysql2", String.class, "(空值)");
        DBHelperContext.setInsertDefaultValue("mysql2", Long.class, -1000L);

        // null 值
        {
            StudentDefaultValueDO2 student = new StudentDefaultValueDO2();
            dbHelper.insert(student);

            StudentDefaultValueDO2 one = dbHelper.getOne(StudentDefaultValueDO2.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("(空值)");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("aa");
        }

        // empty 值
        {
            StudentDefaultValueDO2 student = new StudentDefaultValueDO2();
            student.setSchoolSnapshot(""); // 是空字符串也会被替换
            student.setCourseSnapshot(""); // 是空字符串也会被替换
            dbHelper.insert(student);

            StudentDefaultValueDO2 one = dbHelper.getOne(StudentDefaultValueDO2.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("(空值)");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("aa");
        }

    }

    @Test
    public void testTableDefaultValueWhenBlank() {
        DBHelperContext.setInsertDefaultValue("mysql2", String.class, "(空值)");
        DBHelperContext.setInsertDefaultValue("mysql2", Long.class, -1000L);

        // null 值
        {
            StudentDefaultValueDO3 student = new StudentDefaultValueDO3();
            dbHelper.insert(student);

            StudentDefaultValueDO3 one = dbHelper.getOne(StudentDefaultValueDO3.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("(空值)");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("aa");
        }

        // empty 值
        {
            StudentDefaultValueDO3 student = new StudentDefaultValueDO3();
            student.setSchoolSnapshot(""); // 是空字符串也会被替换
            student.setCourseSnapshot(""); // 是空字符串也会被替换
            dbHelper.insert(student);

            StudentDefaultValueDO3 one = dbHelper.getOne(StudentDefaultValueDO3.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("(空值)");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("aa");
        }

        // blank 值
        {
            StudentDefaultValueDO3 student = new StudentDefaultValueDO3();
            student.setSchoolSnapshot("    "); // 是空白字符串也会被替换
            student.setCourseSnapshot("    "); // 是空白字符串也会被替换
            dbHelper.insert(student);

            StudentDefaultValueDO3 one = dbHelper.getOne(StudentDefaultValueDO3.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("(空值)");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("aa");
        }

    }



}
