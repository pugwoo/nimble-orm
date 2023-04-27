package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.test.entity.JsonDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentNoTableNameDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void testWithTableName() {
        // 测试自定义表名
        Map<Class<?>, String> map = new HashMap<>();
        map.put(StudentDO.class, "t_student123");
        map.put(SchoolDO.class, "t_school123");

        DBHelper.withTableNames(map, () -> {
            String tableName = DBHelperContext.getTableName(StudentDO.class);
            assert tableName.equals("t_student123");

            tableName = DBHelperContext.getTableName(SchoolDO.class);
            assert tableName.equals("t_school123");

            tableName = DBHelperContext.getTableName(JsonDO.class);
            assert tableName == null;
        });

        // 测试表名是否被还原
        String tableName = DBHelperContext.getTableName(StudentDO.class);
        assert tableName == null;

        String tableName1 = DBHelperContext.getTableName(SchoolDO.class);
        assert tableName1 == null;

        // 测试已经有自定义表名了，是否可以自动还原
        DBHelperContext.setTableName(StudentDO.class, "t_student456");
        DBHelperContext.setTableName(SchoolDO.class, "t_school456");

        assert DBHelperContext.getTableName(StudentDO.class).equals("t_student456");
        assert DBHelperContext.getTableName(SchoolDO.class).equals("t_school456");

        DBHelper.withTableNames(map, () -> {
            String tableName2 = DBHelperContext.getTableName(StudentDO.class);
            assert tableName2.equals("t_student123");

            String tableName3 = DBHelperContext.getTableName(SchoolDO.class);
            assert tableName3.equals("t_school123");

            String tableName4 = DBHelperContext.getTableName(JsonDO.class);
            assert tableName4 == null;
        });

        assert DBHelperContext.getTableName(StudentDO.class).equals("t_student456");
        assert DBHelperContext.getTableName(SchoolDO.class).equals("t_school456");

    }

    @Test
    public void testSetTableNameInsertAndQuery() {

        DBHelperContext.setTableName(StudentNoTableNameDO.class, "t_student");

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

        DBHelperContext.resetTableName(); // 清空

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
