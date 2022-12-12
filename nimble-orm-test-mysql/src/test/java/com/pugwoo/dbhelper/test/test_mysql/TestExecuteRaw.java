package com.pugwoo.dbhelper.test.test_mysql;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.wooutils.collect.ListUtils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@Transactional
public class TestExecuteRaw {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testInsert() {
        String name = UUID.randomUUID().toString();
        int rows = dbHelper.executeRaw("insert into t_student(deleted,name) values(0,?)", name);
        assert rows == 1;
        StudentDO student = dbHelper.getOne(StudentDO.class, "where name=?", name);
        assert student.getName().equals(name);

        // 插入2个
        String name1 = UUID.randomUUID().toString();
        String name2 = UUID.randomUUID().toString();
        rows = dbHelper.executeRaw("insert into t_student(deleted,name) values(0,?),(0,?)", name1, name2);
        assert rows == 2;
        StudentDO student1 = dbHelper.getOne(StudentDO.class, "where name=?", name1);
        StudentDO student2 = dbHelper.getOne(StudentDO.class, "where name=?", name2);
        assert student1.getName().equals(name1);
        assert student2.getName().equals(name2);

        // 使用paramMap参数方式插入2个
        Map<String, Object> params = new HashMap<>();
        params.put("name1", UUID.randomUUID().toString());
        params.put("name2", UUID.randomUUID().toString());
        rows = dbHelper.executeRaw("insert into t_student(deleted,name) values(0,:name1),(0,:name2)", params);
        assert rows == 2;
        student1 = dbHelper.getOne(StudentDO.class, "where name=?", params.get("name1"));
        student2 = dbHelper.getOne(StudentDO.class, "where name=?", params.get("name2"));
        assert student1.getName().equals(params.get("name1"));
        assert student2.getName().equals(params.get("name2"));

        // 复制的方式插入2个
        List<String> names = ListUtils.newArrayList(name1, name2);
        rows = dbHelper.executeRaw("insert into t_student(deleted,name) select 0,name from t_student"
                + " where name in (?)", names);
        assert rows == 2;
        assert dbHelper.getAll(StudentDO.class, "where name=?", name1).size() == 2;
        assert dbHelper.getAll(StudentDO.class, "where name=?", name2).size() == 2;
    }

    @Test
    public void testUpdate() {
        String name = UUID.randomUUID().toString();
        dbHelper.executeRaw("insert into t_student(deleted,name) values(0,?)", name);

        String name1 = UUID.randomUUID().toString();
        int rows = dbHelper.executeRaw("update t_student set name=? where name=?", name1, name);

        assert rows == 1;
        StudentDO student = dbHelper.getOne(StudentDO.class, "where name=?", name1);
        assert student.getName().equals(name1);
    }

    @Test
    public void testDelete() {
        String name = UUID.randomUUID().toString();
        dbHelper.executeRaw("insert into t_student(deleted,name) values(0,?)", name);

        int rows = dbHelper.executeRaw("delete from t_student where name=?", name);
        assert rows == 1;

        StudentDO student = dbHelper.getOne(StudentDO.class, "where name=?", name);
        assert student == null;
    }

    @Rollback(false)
    @Test
    public void testCreateTruncateDropTable() {
        String name = UUID.randomUUID().toString().replace("-", "");

        dbHelper.executeRaw("create table t_raw_" + name + "(NAME varchar(1024))");

        assert dbHelper.executeRaw("insert into t_raw_" + name + "(name)values(?)", name) == 1;

        assert dbHelper.getRawCount("select count(*) from t_raw_" + name) == 1;

        dbHelper.executeRaw("truncate table t_raw_" + name);

        assert dbHelper.getRawCount("select count(*) from t_raw_" + name) == 0;

        dbHelper.executeRaw("drop table t_raw_" + name);
    }

}
