package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.exception.NotAllowModifyException;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;

import java.util.List;

public abstract class Test7Interceptor_NotAllow {

    public abstract DBHelper getDBHelper();

    public abstract DBHelper getNativeDBHelper();

    @Test
    public void testQuery() {
        boolean isThrow = false;
        try {
            List<StudentDO> all = getDBHelper().getAll(StudentDO.class);
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
            getDBHelper().insert(studentDO);
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Test
    public void testUpdate() {
        boolean isThrow = false;
        try {
            StudentDO student = CommonOps.insertOne(getNativeDBHelper());
            student.setName("some name");
            getDBHelper().update(student);
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;

        // 更新

        isThrow = false;
        try {
            StudentDO student = CommonOps.insertOne(getNativeDBHelper());
            getDBHelper().updateCustom(student, "set name=?", "xxxx");
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;

        // 更新all
        isThrow = false;
        try {
            StudentDO student = CommonOps.insertOne(getNativeDBHelper());
            getDBHelper().updateAll(StudentDO.class, "set name=?", "where id=?",
                    "some_name", student.getId());
        } catch (NotAllowModifyException e) {
            isThrow = true;
        }
        assert isThrow;
    }

    @Test
    public void testDelete() {
        StudentDO studentDO = CommonOps.insertOne(getNativeDBHelper());

        boolean isThrow = false;
        try {
            getDBHelper().delete(StudentDO.class, "where id=?", studentDO.getId());
        } catch (NotAllowModifyException | NotAllowQueryException e) {
            isThrow = true;
        }
        assert isThrow;
    }

}
