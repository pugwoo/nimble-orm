package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.exception.NotAllowModifyException;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Test7Interceptor_NotAllow {

    @Autowired
    @Qualifier("dbHelperWithNotAllowInterceptor")
    private DBHelper dbHelper;

    @Autowired
    @Qualifier("dbHelper")
    private DBHelper normalDBHelper;

    @Test
    public void testQuery() {
        boolean isThrow = false;
        try {
            List<StudentDO> all = dbHelper.getAll(StudentDO.class);
        } catch (NotAllowQueryException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Test
    public void testInsert() {
        boolean isThrow = false;
        try {
            StudentDO studentDO = new StudentDO();
            studentDO.setName("some name");
            dbHelper.insert(studentDO);
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Test
    public void testUpdate() {
        boolean isThrow = false;
        try {
            StudentDO student = CommonOps.insertOne(normalDBHelper);
            student.setName("some name");
            dbHelper.update(student);
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;

        // 更新

        isThrow = false;
        try {
            StudentDO student = CommonOps.insertOne(normalDBHelper);
            dbHelper.updateCustom(student, "set name=?", "xxxx");
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;

        // 更新all
        isThrow = false;
        try {
            StudentDO student = CommonOps.insertOne(normalDBHelper);
            dbHelper.updateAll(StudentDO.class, "set name=?", "where id=?",
                    "some_name", student.getId());
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Test
    public void testDelete() {
        StudentDO studentDO = CommonOps.insertOne(normalDBHelper);

        boolean isThrow = false;
        try {
            dbHelper.delete(StudentDO.class, "where id=?", studentDO.getId());
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;
    }

}
