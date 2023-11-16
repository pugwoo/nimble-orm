package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.enums.ValueConditionEnum;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.test.entity.IdableSoftDeleteBaseDO;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

public abstract class Test2Insert_DefaultValue {

    public abstract DBHelper getDBHelper();

    @Data
    @Table(value = "t_student", insertDefaultValueMap = "mysql")
    public static class StudentDefaultValueDO extends IdableSoftDeleteBaseDO {
        @Column("school_id")
        private Long schoolId;

        @Column(value = "school_snapshot") // 这个数据不是必填，也没有默认值
        private String schoolSnapshot;

        @Column(value = "course_snapshot", insertValueScript = "'{}'") // 这个数据不是必填，也没有默认值
        private String courseSnapshot;
    }

    @Data
    @Table(value = "t_student", insertDefaultValueMap = "mysql2", insertValueCondition = ValueConditionEnum.WHEN_EMPTY)
    public static class StudentDefaultValueDO2 extends IdableSoftDeleteBaseDO {
        @Column("school_id")
        private Long schoolId;

        @Column(value = "school_snapshot") // 这个数据不是必填，也没有默认值
        private String schoolSnapshot;

        @Column(value = "course_snapshot", insertValueScript = "'{}'", insertValueCondition = ValueConditionEnum.WHEN_EMPTY)
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

        @Column(value = "course_snapshot", insertValueScript = "'{}'", insertValueCondition = ValueConditionEnum.WHEN_BLANK)
        // 这个数据不是必填，也没有默认值
        private String courseSnapshot;
    }

    @Test
    public void testTableDefaultValueWhenNull() {
        StudentDefaultValueDO student = new StudentDefaultValueDO();
        // clickhouse需要设置id
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            student.setId(new Random().nextLong());

            // clickhouse jdbc driver有bug，0.5.0仍未解决 https://github.com/ClickHouse/clickhouse-java/issues/999
            // 所以这里改成 1970-01-02 00:00:00
            student.setDeleteTime(LocalDateTime.of(1970, 1, 2, 0, 0, 0));
        }
        getDBHelper().insert(student);

        StudentDefaultValueDO one = getDBHelper().getOne(StudentDefaultValueDO.class, "where id=?", student.getId());
        assert one.getSchoolSnapshot().equals("");
        assert one.getSchoolId().equals(0L);
        assert one.getCourseSnapshot().equals("{}");
    }

    @Test
    public void testTableDefaultValueWhenEmpty() {
        DBHelperContext.setInsertDefaultValue("mysql2", String.class, "{}");
        DBHelperContext.setInsertDefaultValue("mysql2", Long.class, -1000L);

        // null 值
        {
            StudentDefaultValueDO2 student = new StudentDefaultValueDO2();
            // clickhouse需要设置id
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                student.setId(new Random().nextLong());
            }
            getDBHelper().insert(student);

            StudentDefaultValueDO2 one = getDBHelper().getOne(StudentDefaultValueDO2.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("{}");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("{}");
        }

        // empty 值
        {
            StudentDefaultValueDO2 student = new StudentDefaultValueDO2();
            // clickhouse需要设置id
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                student.setId(new Random().nextLong());
            }
            student.setSchoolSnapshot(""); // 是空字符串也会被替换
            student.setCourseSnapshot(""); // 是空字符串也会被替换
            getDBHelper().insert(student);

            StudentDefaultValueDO2 one = getDBHelper().getOne(StudentDefaultValueDO2.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("{}");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("{}");
        }

    }

    @Test
    public void testTableDefaultValueWhenBlank() {
        DBHelperContext.setInsertDefaultValue("mysql2", String.class, "{}");
        DBHelperContext.setInsertDefaultValue("mysql2", Long.class, -1000L);

        // null 值
        {
            StudentDefaultValueDO3 student = new StudentDefaultValueDO3();
            // clickhouse需要设置id
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                student.setId(new Random().nextLong());
            }
            getDBHelper().insert(student);

            StudentDefaultValueDO3 one = getDBHelper().getOne(StudentDefaultValueDO3.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("{}");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("{}");
        }

        // empty 值
        {
            StudentDefaultValueDO3 student = new StudentDefaultValueDO3();
            // clickhouse需要设置id
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                student.setId(new Random().nextLong());
            }
            student.setSchoolSnapshot(""); // 是空字符串也会被替换
            student.setCourseSnapshot(""); // 是空字符串也会被替换
            getDBHelper().insert(student);

            StudentDefaultValueDO3 one = getDBHelper().getOne(StudentDefaultValueDO3.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("{}");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("{}");
        }

        // blank 值
        {
            StudentDefaultValueDO3 student = new StudentDefaultValueDO3();
            // clickhouse需要设置id
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                student.setId(new Random().nextLong());
            }
            student.setSchoolSnapshot("    "); // 是空白字符串也会被替换
            student.setCourseSnapshot("    "); // 是空白字符串也会被替换
            getDBHelper().insert(student);

            StudentDefaultValueDO3 one = getDBHelper().getOne(StudentDefaultValueDO3.class, "where id=?", student.getId());
            assert one.getSchoolSnapshot().equals("{}");
            assert one.getSchoolId().equals(-1000L);
            assert one.getCourseSnapshot().equals("{}");
        }

    }



}
