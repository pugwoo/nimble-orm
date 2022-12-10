package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;

import java.util.List;

public class CourseVO extends CourseDO {

    // 这里使用conditional，对只有是主课程的学生才进行查询
    @RelatedColumn(localColumn = "student_id", remoteColumn = "id",
            conditional = "t.getIsMain() != null && t.getIsMain()")
    private List<StudentDO> mainCourseStudents;

    // 这里使用conditional，对只有是主课程的学生才进行查询
    @RelatedColumn(localColumn = "student_id", remoteColumn = "id",
            conditional = "t.getIsMain() != null && t.getIsMain()")
    private StudentDO mainCourseStudent;

    public List<StudentDO> getMainCourseStudents() {
        return mainCourseStudents;
    }

    public void setMainCourseStudents(List<StudentDO> mainCourseStudents) {
        this.mainCourseStudents = mainCourseStudents;
    }

    public StudentDO getMainCourseStudent() {
        return mainCourseStudent;
    }

    public void setMainCourseStudent(StudentDO mainCourseStudent) {
        this.mainCourseStudent = mainCourseStudent;
    }
}
