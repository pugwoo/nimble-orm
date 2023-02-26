package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class Test1Query_RelatedColumn {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testRelatedColumnWithLimit() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        CourseDO courseDO1 = new CourseDO();
        courseDO1.setName("math");
        courseDO1.setStudentId(studentDO.getId());
        courseDO1.setIsMain(true); // math是主课程
        dbHelper.insert(courseDO1);

        CourseDO courseDO2 = new CourseDO();
        courseDO2.setName("eng");
        courseDO2.setStudentId(studentDO.getId());
        courseDO2.setIsMain(true); // eng是主课程
        dbHelper.insert(courseDO2);

        CourseDO courseDO3 = new CourseDO();
        courseDO3.setName("chinese");
        courseDO3.setStudentId(studentDO.getId());
        courseDO3.setIsMain(true); // chinese是主课程
        dbHelper.insert(courseDO3);

        StudentDO studentDO2 = CommonOps.insertOne(dbHelper);

        List<StudentLimitVO> all = dbHelper.getAll(StudentLimitVO.class, "where id=? or id=?",
                studentDO.getId(), studentDO2.getId());
        for (StudentLimitVO a : all) {
            if (a.getId().equals(studentDO.getId())) {
                assert a.getMainCourses().size() == 2;
                assert a.getMainCourses().get(0).getIsMain();
                assert a.getMainCourses().get(1).getIsMain();
            }
            if (a.getId().equals(studentDO2.getId())) {
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
        StudentDO student1 = CommonOps.insertOne(dbHelper);
        StudentDO student2 = CommonOps.insertOne(dbHelper);

        String course1 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String course2 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        CourseDO courseDO = new CourseDO();
        courseDO.setName(course1);
        courseDO.setStudentId(student1.getId());
        courseDO.setIsMain(true);
        dbHelper.insert(courseDO);
        Long id1 = courseDO.getId();

        courseDO = new CourseDO();
        courseDO.setName(course1);
        courseDO.setStudentId(student2.getId());
        courseDO.setIsMain(false);
        dbHelper.insert(courseDO);
        Long id2 = courseDO.getId();

        courseDO = new CourseDO();
        courseDO.setName(course2);
        courseDO.setStudentId(student1.getId());
        courseDO.setIsMain(false);
        dbHelper.insert(courseDO);
        Long id3 = courseDO.getId();

        courseDO = new CourseDO();
        courseDO.setName(course2);
        courseDO.setStudentId(student2.getId());
        courseDO.setIsMain(true);
        dbHelper.insert(courseDO);
        Long id4 = courseDO.getId();

        CourseVO courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id1);
        assert courseVO.getMainCourseStudents().size() == 1;
        assert courseVO.getMainCourseStudents().get(0).getId().equals(student1.getId());
        assert courseVO.getMainCourseStudent().getId().equals(student1.getId());
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

        courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id2);
        assert courseVO.getMainCourseStudents().isEmpty();
        assert courseVO.getMainCourseStudent() == null;
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

        courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id3);
        assert courseVO.getMainCourseStudents().isEmpty();
        assert courseVO.getMainCourseStudent() == null;
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

        courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id4);
        assert courseVO.getMainCourseStudents().size() == 1;
        assert courseVO.getMainCourseStudents().get(0).getId().equals(student2.getId());
        assert courseVO.getMainCourseStudent().getId().equals(student2.getId());
        assert courseVO.getConditionNull() == null;
        assert courseVO.getConditionNotReturnBoolean() == null;
        assert courseVO.getConditionThrowException() == null;

    }

    @Test
    public void testRelatedColumn() {

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("sysu");
        dbHelper.insert(schoolDO);

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        studentDO.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO);

        CourseDO courseDO1 = new CourseDO();
        courseDO1.setName("math");
        courseDO1.setStudentId(studentDO.getId());
        courseDO1.setIsMain(true); // math是主课程
        dbHelper.insert(courseDO1);

        CourseDO courseDO2 = new CourseDO();
        courseDO2.setName("eng");
        courseDO2.setStudentId(studentDO.getId());
        dbHelper.insert(courseDO2);

        StudentDO studentDO2  = CommonOps.insertOne(dbHelper);
        studentDO2.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO2);

        CourseDO courseDO3 = new CourseDO();
        courseDO3.setName("math");
        courseDO3.setStudentId(studentDO2.getId());
        courseDO3.setIsMain(true); // math是主课程
        dbHelper.insert(courseDO3);

        CourseDO courseDO4 = new CourseDO();
        courseDO4.setName("chinese");
        courseDO4.setStudentId(studentDO2.getId());
        dbHelper.insert(courseDO4);

        /////////////////// 下面是查询 ///////////////////

        StudentVO studentVO1 = dbHelper.getByKey(StudentVO.class, studentDO.getId());
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
        dbHelper.handleRelatedColumn(studentVO2);
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
        dbHelper.handleRelatedColumn(studentVO2, "courses", "schoolDO"); // 指定要的RelatedColumn
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
        dbHelper.handleRelatedColumn(ListUtils.newList(studentVO2), "courses", "schoolDO");
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
        List<StudentVO> studentVOs = dbHelper.getAll(StudentVO.class,
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

            if(sVO.getId().equals(studentDO2.getId())) {
                assert
                        sVO.getCourses().get(0).getId().equals(courseDO3.getId())
                                || sVO.getCourses().get(1).getId().equals(courseDO4.getId());
            }

            assert sVO.getNameWithHi().equals(sVO.getName() + "hi"); // 测试计算列
        }

        // 测试innerClass
        SchoolWithInnerClassVO schoolVO = dbHelper.getByKey(SchoolWithInnerClassVO.class, schoolDO.getId());
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

        StudentDO studentDO = new StudentDO();
        studentDO.setName(uuid);
        dbHelper.insert(studentDO);

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName(uuid);
        dbHelper.insert(schoolDO);

        Student1VO one = dbHelper.getOne(Student1VO.class, "where name=?", uuid);
        assert one.getName1().equals("S" + uuid);
        assert one.getSchool1DO().getName2().equals("S" + uuid);

        Student2VO two = dbHelper.getOne(Student2VO.class, "where t1.name=?", uuid);
        assert two.getSchool1DO().getName2().equals("S" + uuid);

    }

    // ================== 测试related column匹配的字段不是同一类型的情况

    @Test
    public void testDiffClassTypeRelatedColumn() {
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("sysu");
        dbHelper.insert(schoolDO);

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        studentDO.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO);

        StudentStringSchoolIdDO one = dbHelper.getOne(StudentStringSchoolIdDO.class, "where id=?", studentDO.getId());
        assert one.getId().equals(studentDO.getId());
        assert one.getSchoolDO().getId().equals(schoolDO.getId()); // 不同类型的比对会转换成string进行

        assert one.getSchools().size() == 1;
        assert one.getSchools().get(0).getId().equals(schoolDO.getId()); // 不同类型的比对会转换成string进行
    }

    /**这个DO的school id类型是字符串，故意映射的，这是允许的*/
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
            dbHelper.getOne(CourseBlankLocalColumnVO.class);
        } catch (RelatedColumnFieldNotFoundException e) {
            isThrow = true;
        }
        assert isThrow;

        isThrow = false;
        try {
            dbHelper.getOne(CourseBlankRemoteColumnVO.class);
        } catch (RelatedColumnFieldNotFoundException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Test
    public void testRelatedColumnEmptyList() {
        CourseDO courseDO = new CourseDO();
        // courseDO.setStudentId(-1L); // student id不设置
        dbHelper.insert(courseDO);

        CourseEmptyListVO one = dbHelper.getOne(CourseEmptyListVO.class, "where id=?", courseDO.getId());
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
        // withTransactionService 这个bean是存在的，但是类型不对
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id",
                dbHelperBean = "withTransactionService")
        private List<StudentDO> students;
    }

    @Test
    public void testRelatedColumnWrongDBHelperBean() {
        boolean isThrow = false;
        try {
            dbHelper.getAll(CourseDBHelperBeanNotExistVO.class);
        } catch (NoSuchBeanDefinitionException e) {
            isThrow = true;
        }
        assert isThrow;

        isThrow = false;
        try {
            dbHelper.getAll(CourseDBHelperBeanNotMatchVO.class);
        } catch (SpringBeanNotMatchException e) {
            isThrow = true;
        }
        assert isThrow;

    }

    @Data
    public static class CourseEmptyWrongExtraWhere1VO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id", extraWhere = "where a&&&") // SQL语法错误
        private List<StudentDO> students;
    }

    @Data
    public static class CourseEmptyWrongExtraWhere2VO extends CourseDO {
        @RelatedColumn(localColumn = "student_id", remoteColumn = "id", extraWhere = "where a&&& limit 10") // SQL语法错误，且带limit
        private List<StudentDO> students;
    }

    @Test
    public void testWrongExtraWhere() {
        boolean isThrow = false;
        try {
            dbHelper.getAll(CourseEmptyWrongExtraWhere1VO.class);
        } catch (BadSQLSyntaxException e) {
            isThrow = true;
        }
        assert isThrow;

        isThrow = false;
        try {
            dbHelper.getAll(CourseEmptyWrongExtraWhere2VO.class);
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
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        CourseDO courseDO = new CourseDO();
        courseDO.setStudentId(studentDO.getId());
        dbHelper.insert(courseDO);

        CourseNullDataServiceVO one = dbHelper.getOne(CourseNullDataServiceVO.class, "where id=?", courseDO.getId());
        assert one.getStudents() != null; // 不会是null，框架会自动设置
        assert one.getStudents().isEmpty();
    }
}
