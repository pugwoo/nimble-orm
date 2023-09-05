package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public abstract class Test3Update_Custom {

    public abstract DBHelper getDBHelper();

    @Test
    public void testUpdateCustom() {
        StudentDO db = CommonOps.insertOne(getDBHelper());

        StudentDO studentDO = new StudentDO();
        studentDO.setId(db.getId());

        int rows = getDBHelper().updateCustom(studentDO, "name=?", "nick2");
        assert rows == 1;

        rows = getDBHelper().updateCustom(studentDO, "set name=?", "nick3");
        assert rows == 1;

        rows = getDBHelper().updateCustom(studentDO, "SET name=?", "nick4");
        assert rows == 1;

        db = getDBHelper().getByKey(StudentDO.class, db.getId());
        assert "nick4".equals(db.getName());

        // 测试异常参数
        assert getDBHelper().updateCustom(studentDO, null) == 0;
    }

    @Test
    public void testUpdateAll() {
        CommonOps.insertBatch(getDBHelper(),101);
        String newName = "nick" + UUID.randomUUID().toString().replace("-", "");

        int rows = getDBHelper().updateAll(StudentDO.class,
                "set name=?", "where name like ?", newName, "nick%");
        assert rows > 0;

        List<StudentDO> list = getDBHelper().getAll(StudentDO.class, "where name=?", newName);
        assert list.size() == rows;

        // 测试异常参数
        assert getDBHelper().updateAll(StudentDO.class, null, "where 1=1") == 0;
    }

}
