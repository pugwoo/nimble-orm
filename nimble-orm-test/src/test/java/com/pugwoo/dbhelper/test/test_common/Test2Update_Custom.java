package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class Test2Update_Custom {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testUpdateCustom() {
        StudentDO db = CommonOps.insertOne(dbHelper);

        StudentDO studentDO = new StudentDO();
        studentDO.setId(db.getId());

        int rows = dbHelper.updateCustom(studentDO, "name=?", "nick2");
        assert rows == 1;

        rows = dbHelper.updateCustom(studentDO, "set name=?", "nick3");
        assert rows == 1;

        rows = dbHelper.updateCustom(studentDO, "SET name=?", "nick4");
        assert rows == 1;

        db = dbHelper.getByKey(StudentDO.class, db.getId());
        assert "nick4".equals(db.getName());

        // 测试异常参数
        assert dbHelper.updateCustom(studentDO, null) == 0;
    }

    @Test
    public void testUpdateAll() {
        CommonOps.insertBatch(dbHelper,101);
        String newName = "nick" + UUID.randomUUID().toString().replace("-", "");

        int rows = dbHelper.updateAll(StudentDO.class,
                "set name=?", "where name like ?", newName, "nick%");
        assert rows > 0;

        List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where name=?", newName);
        assert list.size() == rows;

        // 测试异常参数
        assert dbHelper.updateAll(StudentDO.class, null, "where 1=1") == 0;
    }

}
