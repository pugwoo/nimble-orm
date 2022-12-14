package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;

import java.util.List;

/**
 * 用于测试relateColumn的extraWhere有limit子句的情况
 */
public class StudentLimitVO extends StudentDO {

    @RelatedColumn(localColumn = "id", remoteColumn = "student_id",
            extraWhere = "where is_main=1 limit 2")
    private List<CourseDO> mainCourses;

    public List<CourseDO> getMainCourses() {
        return mainCourses;
    }

    public void setMainCourses(List<CourseDO> mainCourses) {
        this.mainCourses = mainCourses;
    }

}
