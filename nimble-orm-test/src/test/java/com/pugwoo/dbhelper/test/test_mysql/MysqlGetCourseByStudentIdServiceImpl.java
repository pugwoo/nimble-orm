package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.test_common.GetCourseByStudentIdServiceImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MysqlGetCourseByStudentIdServiceImpl extends GetCourseByStudentIdServiceImpl {

    @Autowired
    private DBHelper dbHelper;

    @Override
    public DBHelper getDBHelper() {
        return dbHelper;
    }

    @Data
    public static class StudentVO extends StudentDO {

        /**特别说明：dbHelperBean可以不用指定，这里只是测试指定DBHelper；这里remoteColumn故意用大写，也是可以自动匹配上的*/
        @RelatedColumn(localColumn = "school_id", remoteColumn = "ID", dbHelperBean = "dbHelper")
        private SchoolDO schoolDO;

        @RelatedColumn(localColumn = "id", remoteColumn = "student_id", dataService = MysqlGetCourseByStudentIdServiceImpl.class)
        private List<CourseDO> courses;

    }



}
