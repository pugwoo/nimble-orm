package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentNoTableNameDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDynamic {

    @Autowired
    private DBHelper dbHelper;

    /**测试分表*/
    @Test
    public void test() {
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
    public void test2() {

        DBHelper.setTableName(StudentNoTableNameDO.class, "t_student");

        StudentNoTableNameDO studentNoTableNameDO = new StudentNoTableNameDO();
        studentNoTableNameDO.setName("nick");

        dbHelper.insert(studentNoTableNameDO);

        assert studentNoTableNameDO.getId() != null;

        StudentNoTableNameDO student2 = dbHelper.getByKey(StudentNoTableNameDO.class, studentNoTableNameDO.getId());
        assert student2.getId().equals(studentNoTableNameDO.getId());
        assert student2.getName().equals(studentNoTableNameDO.getName());

        DBHelper.resetTableNames(); // 清空

        // 应该抛出异常
        boolean ex = false;
        try {
            student2 = dbHelper.getByKey(StudentNoTableNameDO.class, studentNoTableNameDO.getId());
        } catch (Exception e) {
            ex = true;
        }
        assert ex;
    }

}
