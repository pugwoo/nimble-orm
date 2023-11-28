package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.*;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.exception.BadSQLSyntaxException;
import com.pugwoo.dbhelper.exception.RelatedColumnFieldNotFoundException;
import com.pugwoo.dbhelper.exception.SpringBeanNotMatchException;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.service.WrongDataService;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.*;
import com.pugwoo.wooutils.collect.ListUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Test1Query_RelatedColumn {

    public abstract DBHelper getDBHelper();

    @Test
    public void testRelatedColumnWithLimit() {
        StudentDO studentDO = CommonOps.insertOne(getDBHelper());

        CommonOps.insertOneCourseDO(getDBHelper(), "math", studentDO.getId(), true);
        CommonOps.insertOneCourseDO(getDBHelper(), "eng", studentDO.getId(), true);
        CommonOps.insertOneCourseDO(getDBHelper(), "chinese", studentDO.getId(), true);

        StudentDO studentDO2 = CommonOps.insertOne(getDBHelper());

        List<StudentLimitVO> all = getDBHelper().getAll(StudentLimitVO.class, "where id=? or id=?",
                studentDO.getId(), studentDO2.getId());
        for (StudentLimitVO a : all) {
            if (a.getId().equals(studentDO.getId())) {
                assert a.getMainCourses().size() == 2;
                assert a.getMainCourses().get(0).getIsMain();
                assert a.getMainCourses().get(1).getIsMain();
            }
            if (a.getId().equals(studentDO2.getId())) {
                System.out.println(a.getMainCourses().size());
                assert a.getMainCourses().isEmpty();
            }
        }
    }

    @Test
    public void testRelatedColumnConditional() {
        // 构造数据：
        // 课程1 学生1 主课程
        // 课程1 学生2 非主课程
        // 课程2 学生1 非主课程
        // 课程2 学生2 主课程
        StudentDO student1 = CommonOps.insertOne(getDBHelper());
        StudentDO student2 = CommonOps.insertOne(getDBHelper());

        String course1 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String course2 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        CourseDO courseDO = CommonOps.insertOneCourseDO(getDBHelper(), course1, student1.getId(), true);
        Long id1 = courseDO.getId();

        courseDO = CommonOps.insertOneCourseDO(getDBHelper(), course1, student2.getId(), false);
        Long id2 = courseDO.getId();

        courseDO = CommonOps.insertOneCourseDO(getDBHelper(), course2, student1.getId(), false);
        Long id3 = courseDO.getId();

        courseDO = CommonOps.insertOneCourseDO(getDBHelper(), course2, student2.getId(), true);
        Long id4 = courseDO.getId();

        CourseVO courseVO = getDBHelper().getOne(CourseVO.class, "where id=?", id1);
        assert courseVO.getMainCourseStudents().size() == 1;
        assert courseVO.getMainCourseStudents().get(0).getId().equals(student1.getId());
        assert courseVO.getMainCourseStudent().getId().equals(student1.getId());
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

        courseVO = getDBHelper().getOne(CourseVO.class, "where id=?", id2);
        assert courseVO.getMainCourseStudents().isEmpty();
        assert courseVO.getMainCourseStudent() == null;
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

        courseVO = getDBHelper().getOne(CourseVO.class, "where id=?", id3);
        assert courseVO.getMainCourseStudents().isEmpty();
        assert courseVO.getMainCourseStudent() == null;
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

        courseVO = getDBHelper().getOne(CourseVO.class, "where id=?", id4);
        assert courseVO.getMainCourseStudents().size() == 1;
        assert courseVO.getMainCourseStudents().get(0).getId().equals(student2.getId());
        assert courseVO.getMainCourseStudent().getId().equals(student2.getId());
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

    }

    @Test
    public void testRelatedColumn() {

        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), "sysu");

        StudentDO studentDO = CommonOps.insertOne(getDBHelper());
        studentDO.setSchoolId(schoolDO.getId());
        getDBHelper().update(studentDO);

        CourseDO courseDO1 = CommonOps.insertOneCourseDO(getDBHelper(), "math", studentDO.getId(), true);
        CourseDO courseDO2 = CommonOps.insertOneCourseDO(getDBHelper(), "eng", studentDO.getId(), false);

        StudentDO studentDO2  = CommonOps.insertOne(getDBHelper());
        studentDO2.setSchoolId(schoolDO.getId());
        getDBHelper().update(studentDO2);

        CourseDO courseDO3 = CommonOps.insertOneCourseDO(getDBHelper(), "math", studentDO2.getId(), true);
        CourseDO courseDO4 = CommonOps.insertOneCourseDO(getDBHelper(), "chinese", studentDO2.getId(), false);

        /////////////////// 下面是查询 ///////////////////

        StudentVO studentVO1 = getDBHelper().getByKey(StudentVO.class, studentDO.getId());
        assert studentVO1 != null;
        assert studentVO1.getSchoolDO() != null;
        assert studentVO1.getSchoolDO().getId().equals(studentVO1.getSchoolId());
        assert studentVO1.getCourses() != null;
        assert studentVO1.getCourses().size() == 2;
        assert studentVO1.getCourses().get(0).getId().equals(courseDO1.getId())
                || studentVO1.getCourses().get(0).getId().equals(courseDO2.getId());
        assert studentVO1.getMainCourses().size() == 1 &&
                studentVO1.getMainCourses().get(0).getName().equals("math"); // math是主课程
        assert studentVO1.getNameWithHi().equals(studentVO1.getName() + "hi"); // 测试计算列

        // == handleRelatedColumn test
        StudentVOForHandleRelatedColumnOnly studentVO2 = new StudentVOForHandleRelatedColumnOnly();
        studentVO2.setId(studentDO.getId());
        studentVO2.setSchoolId(studentDO.getSchoolId());
        getDBHelper().handleRelatedColumn(studentVO2);
        assert studentVO2 != null;
        assert studentVO2.getSchoolDO() != null;
        assert studentVO2.getSchoolDO().getId().equals(studentVO2.getSchoolId());
        assert studentVO2.getCourses() != null;
        assert studentVO2.getCourses().size() == 2;
        assert studentVO2.getCourses().get(0).getId().equals(courseDO1.getId())
                || studentVO2.getCourses().get(0).getId().equals(courseDO2.getId());
        assert studentVO2.getMainCourses().size() == 1 &&
                studentVO2.getMainCourses().get(0).getName().equals("math"); // math是主课程

        studentVO2 = new StudentVOForHandleRelatedColumnOnly();
        studentVO2.setId(studentDO.getId());
        studentVO2.setSchoolId(studentDO.getSchoolId());
        getDBHelper().handleRelatedColumn(studentVO2, "courses", "schoolDO"); // 指定要的RelatedColumn
        assert studentVO2.getSchoolDO() != null;
        assert studentVO2.getSchoolDO().getId().equals(studentVO2.getSchoolId());
        assert studentVO2.getCourses() != null;
        assert studentVO2.getCourses().size() == 2;
        assert studentVO2.getCourses().get(0).getId().equals(courseDO1.getId())
                || studentVO2.getCourses().get(0).getId().equals(courseDO2.getId());
        assert studentVO2.getMainCourses() == null;

        // 转换成list处理，这个其实和上面这一段是一样的逻辑
        studentVO2 = new StudentVOForHandleRelatedColumnOnly();
        studentVO2.setId(studentDO.getId());
        studentVO2.setSchoolId(studentDO.getSchoolId());
        getDBHelper().handleRelatedColumn(ListUtils.newList(studentVO2), "courses", "schoolDO");
        assert studentVO2.getSchoolDO() != null;
        assert studentVO2.getSchoolDO().getId().equals(studentVO2.getSchoolId());
        assert studentVO2.getCourses() != null;
        assert studentVO2.getCourses().size() == 2;
        assert studentVO2.getCourses().get(0).getId().equals(courseDO1.getId())
                || studentVO2.getCourses().get(0).getId().equals(courseDO2.getId());
        assert studentVO2.getMainCourses() == null;

        // END

        List<Long> ids = new ArrayList<Long>();
        ids.add(studentDO.getId());
        ids.add(studentDO2.getId());
        List<StudentVO> studentVOs = getDBHelper().getAll(StudentVO.class,
                "where id in (?)", ids);
        assert studentVOs.size() == 2;
        for(StudentVO sVO : studentVOs) {
            assert sVO != null;
            assert sVO.getSchoolDO() != null;
            assert sVO.getSchoolDO().getId().equals(sVO.getSchoolId());
            assert sVO.getCourses() != null;
            assert sVO.getCourses().size() == 2;
            assert sVO.getMainCourses().size() == 1 &&
                    studentVO1.getMainCourses().get(0).getName().equals("math"); // math是主课程

            if (sVO.getId().equals(studentDO2.getId())) {
                assert sVO.getCourses().get(0).getId().equals(courseDO3.getId()) && sVO.getCourses().get(1).getId().equals(courseDO4.getId()) ||
                        sVO.getCourses().get(0).getId().equals(courseDO4.getId()) && sVO.getCourses().get(1).getId().equals(courseDO3.getId());
            }

            assert sVO.getNameWithHi().equals(sVO.getName() + "hi"); // 测试计算列
        }

        // 测试innerClass
        SchoolWithInnerClassVO schoolVO = getDBHelper().getByKey(SchoolWithInnerClassVO.class, schoolDO.getId());
        assert schoolVO != null && schoolVO.getId().equals(schoolDO.getId());
        assert schoolVO.getStudents().size() == 2;
        for(SchoolWithInnerClassVO.StudentVO s : schoolVO.getStudents()) {
            assert s != null && s.getId() != null && s.getCourses().size() == 2;
        }
    }

    // ================== 计算列的related column关联查询
    @Data
    @Table("t_school")
    public static class School1DO {
        @Column(value = "name2", computed = "concat('S',name)")
        private String name2;
    }

    @Data
    @Table("t_student")
    public static class Student1VO {
        @RelatedColumn(localColumn = "name1", remoteColumn = "name2") // 靠计算列进行关联
        private School1DO school1DO;

        @Column(value = "name1", computed = "concat('S',name)")
        private String name1;
    }

    @Data
    @Table("t_student")
    public static class StudentLeftJoinVO {
        @Column(value = "name1", computed = "concat('S',t1.name)") // 注意，这里要加t1，因为计算列框架不会自动处理
        private String name1;
    }

    @Data
    @Table("t_student")
    public static class StudentRightJoinVO {
        @Column(value = "name1", computed = "concat('S',t2.name)") // 注意，这里要加t2，因为计算列框架不会自动处理
        private String name1;
    }

    @Data
    @JoinTable(joinType = JoinTypeEnum.JOIN, on = "t1.id=t2.id")
    public static class Student2VO {

        @JoinLeftTable
        private StudentLeftJoinVO student1DO;

        @JoinRightTable
        private StudentRightJoinVO student2DO;

        @RelatedColumn(localColumn = "t1.name1", remoteColumn = "name2") // 靠计算列进行关联
        private School1DO school1DO;
    }

    @Test
    public void testComputedRelatedColumn() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 24);

        StudentDO studentDO = CommonOps.insertOne(getDBHelper(), uuid);

        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), uuid);

        Student1VO one = getDBHelper().getOne(Student1VO.class, "where name=?", uuid);
        assert one.getName1().equals("S" + uuid);
        assert one.getSchool1DO().getName2().equals("S" + uuid);

        Student2VO two = getDBHelper().getOne(Student2VO.class, "where t1.name=?", uuid);
        assert two.getSchool1DO().getName2().equals("S" + uuid);

    }

    // ================== 测试related column匹配的字段不是同一类型的情况

    @Test
    public void testDiffClassTypeRelatedColumn() {
        // postgresql不支持用string类型去查数字类型的字段，因此不测试
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.POSTGRESQL) {
            return;
        }

        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), "sysu");

        StudentDO studentDO = CommonOps.insertOne(getDBHelper(), schoolDO.getId());

        StudentStringSchoolIdDO one = getDBHelper().getOne(StudentStringSchoolIdDO.class, "where id=?", studentDO.getId());
        assert one.getId().equals(studentDO.getId());
        assert one.getSchoolDO().getId().equals(schoolDO.getId()); // 不同类型的比对会转换成string进行

        assert one.getSchools().size() == 1;
        assert one.getSchools().get(0).getId().equals(schoolDO.getId()); // 不同类型的比对会转换成string进行
    }

    /**这个DO的school id类型是字符串，故意映射的，这是允许的（对于pgsql，这种用string去查数字类型的方式会报错）*/
    @Data
    @Table("t_student")
    public static class StudentStringSchoolIdDO {
        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;

        @Column("school_id")
        private String schoolId;

        @RelatedColumn(localColumn = "school_id", remoteColumn = "id")
        private SchoolDO schoolDO;

        @RelatedColumn(localColumn = "school_id", remoteColumn = "id")
        private List<SchoolDO> schools;
    }

    // ================== 以下单元测试是测试异常情况的测试

    @Test
    public void testRelatedColumnWrongArgs() {
        boolean isThrow = false;
        try {
            getDBHelper().getOne(CourseBlankLocalColumnVO.class);
        } catch (RelatedColumnFieldNotFoundException e) {
            isThrow = true;
        }
        assert isThrow;

        isThrow = false;
        try {
            getDBHelper().getOne(CourseBlankRemoteColumnVO.class);
        } catch (RelatedColumnFieldNotFoundException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Test
    public void testRelatedColumnEmptyList() {
        // student id不设置
        CourseDO courseDO = CommonOps.insertOneCourseDO(getDBHelper(), null, null);

        CourseEmptyListVO one = getDBHelper().getOne(CourseEmptyListVO.class, "where id=?", courseDO.getId());
        assert one.getStudents() != null; // 不会是null，框架会自动设置
        assert one.getStudents().isEmpty();
    }

    @Data
    public static class CourseBlankLocalColumnVO extends CourseDO {
        @RelatedColumn(localColumn = "", remoteColumn = "id")
        private StudentDO conditionNotReturnBoolean;
    }

    @Data
    public static class CourseBlankRemoteColumnVO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "")
        private StudentDO conditionNotReturnBoolean;
    }

    @Data
    public static class CourseEmptyListVO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id")
        private List<StudentDO> students;
    }

    @Data
    public static class CourseDBHelperBeanNotExistVO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id", dbHelperBean = "xxxxx")
        private List<StudentDO> students;
    }

    @Data
    public static class CourseDBHelperBeanNotMatchVO extends CourseDO {
        // wrongBeanService 这个bean是存在的，但是类型不对
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id", dbHelperBean = "wrongBeanService")
        private List<StudentDO> students;
    }

    @Test
    public void testRelatedColumnWrongDBHelperBean() {
        boolean isThrow = false;
        try {
            getDBHelper().getAll(CourseDBHelperBeanNotExistVO.class);
        } catch (NoSuchBeanDefinitionException e) {
            isThrow = true;
        }
        assert isThrow;

        isThrow = false;
        try {
            getDBHelper().getAll(CourseDBHelperBeanNotMatchVO.class);
        } catch (SpringBeanNotMatchException e) {
            isThrow = true;
        }
        assert isThrow;

    }

    @Data
    public static class CourseEmptyWrongExtraWhere1VO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id", extraWhere = "where a(((") // SQL语法错误
        private List<StudentDO> students;
    }

    @Data
    public static class CourseEmptyWrongExtraWhere2VO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id", extraWhere = "where a&&& limit 10") // SQL语法错误，且带limit
        private List<StudentDO> students;
    }

    @Test
    public void testWrongExtraWhere() {
        // 先插入一条数据
        CourseDO courseDO = CommonOps.insertOneCourseDO(getDBHelper(), "some course", 1L);

        boolean isThrow = false;
        try {
            getDBHelper().getAll(CourseEmptyWrongExtraWhere1VO.class);
        } catch (BadSQLSyntaxException e) {
            isThrow = true;
        }
        assert isThrow;

        isThrow = false;
        try {
            getDBHelper().getAll(CourseEmptyWrongExtraWhere2VO.class);
        } catch (BadSQLSyntaxException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Data
    public static class CourseNullDataServiceVO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id", dataService = WrongDataService.class)
        private List<StudentDO> students;
    }

    @Test
    public void testNullDataService() {
        StudentDO studentDO = CommonOps.insertOne(getDBHelper());

        CourseDO courseDO = CommonOps.insertOneCourseDO(getDBHelper(), null, studentDO.getId());

        CourseNullDataServiceVO one = getDBHelper().getOne(CourseNullDataServiceVO.class, "where id=?", courseDO.getId());
        assert one.getStudents() != null; // 不会是null，框架会自动设置
        assert one.getStudents().isEmpty();
    }

    @Test
    public void handleNullList() {
        List<CourseVO> courses = new ArrayList<>();
        courses.add(null);
        courses.add(null);

        getDBHelper().handleRelatedColumn(courses);
    }
}
