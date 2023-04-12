package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentNoTableNameDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class Test8Feature_DynamicTable {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testSetTableName() {
        String tableName = DBHelperContext.getTableName(StudentDO.class);
        assert tableName == null;

        // 测试错误参数
        DBHelperContext.setTableName(StudentDO.class, null);
        DBHelperContext.setTableName(StudentDO.class, "");
        DBHelperContext.setTableName(null, "my_table");

        DBHelperContext.setTableName(StudentDO.class, "my_table");
        tableName = DBHelperContext.getTableName(StudentDO.class);
        assert tableName.equals("my_table");

        DBHelperContext.resetTableName();
        tableName = DBHelperContext.getTableName(StudentDO.class);
        assert tableName == null;
    }

    @Test
    public void testSetTableNameInsertAndQuery() {

        DBHelper.setTableName(StudentNoTableNameDO.class, "t_student");

        // 插入
        StudentNoTableNameDO studentNoTableNameDO = new StudentNoTableNameDO();
        studentNoTableNameDO.setName("nick");

        dbHelper.insert(studentNoTableNameDO);

        assert studentNoTableNameDO.getId() != null;

        // 查询
        StudentNoTableNameDO student2 = dbHelper.getByKey(StudentNoTableNameDO.class, studentNoTableNameDO.getId());
        assert student2.getId().equals(studentNoTableNameDO.getId());
        assert student2.getName().equals(studentNoTableNameDO.getName());

        List<StudentNoTableNameDO> list = dbHelper.getAll(
                StudentNoTableNameDO.class, "where id=?", studentNoTableNameDO.getId());
        assert list.get(0).getId().equals(studentNoTableNameDO.getId());
        assert list.get(0).getName().equals(studentNoTableNameDO.getName());

        // 更新
        studentNoTableNameDO.setName(UUID.randomUUID().toString().replace("-", ""));
        int rows = dbHelper.update(studentNoTableNameDO);
        assert rows == 1;

        // 再查询
        student2 = dbHelper.getByKey(StudentNoTableNameDO.class, studentNoTableNameDO.getId());
        assert student2.getId().equals(studentNoTableNameDO.getId());
        assert student2.getName().equals(studentNoTableNameDO.getName());

        list = dbHelper.getAll(StudentNoTableNameDO.class, "where id=?", studentNoTableNameDO.getId());
        assert list.get(0).getId().equals(studentNoTableNameDO.getId());
        assert list.get(0).getName().equals(studentNoTableNameDO.getName());

        // 删除
        rows = dbHelper.delete(studentNoTableNameDO);
        assert rows == 1;

        assert dbHelper.getAll(StudentNoTableNameDO.class, "where id=?", studentNoTableNameDO.getId())
                        .isEmpty();

        DBHelper.resetTableNames(); // 清空

        // 应该抛出异常
        boolean ex = false;
        try {
            dbHelper.getByKey(StudentNoTableNameDO.class, studentNoTableNameDO.getId());
        } catch (Exception e) {
            ex = true;
        }
        assert ex;
    }

}
