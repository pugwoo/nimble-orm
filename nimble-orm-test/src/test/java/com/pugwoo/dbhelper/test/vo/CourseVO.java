package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import lombok.Data;

import java.util.List;

@Data
public class CourseVO extends CourseDO {

    // 这里使用conditional，对只有是主课程的学生才进行查询
    @RelatedColumn(localColumn = "student_id", remoteColumn = "id",
            conditional = "t.getIsMain() != null && t.getIsMain()")
    private List<StudentDO> mainCourseStudents;

    // 这里使用conditional，对只有是主课程的学生才进行查询
    @RelatedColumn(localColumn = "student_id", remoteColumn = "id",
            conditional = "t.getIsMain() != null && t.getIsMain()")
    private StudentDO mainCourseStudent;

    @RelatedColumn(localColumn = "student_id", remoteColumn = "id", conditional = "null")
    private StudentDO conditionNull;

    @RelatedColumn(localColumn = "student_id", remoteColumn = "id", conditional = "'something'")
    private StudentDO conditionNotReturnBoolean;

    @RelatedColumn(localColumn = "student_id", remoteColumn = "id", conditional = "a+b")
    private StudentDO conditionThrowException;

}
