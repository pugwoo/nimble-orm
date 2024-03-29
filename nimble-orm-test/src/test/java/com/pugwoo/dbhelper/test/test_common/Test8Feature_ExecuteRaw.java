package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Test8Feature_ExecuteRaw {

    public abstract DBHelper getDBHelper();

    @Test
    public void testInsert() {
        String name = UUID.randomUUID().toString();
        int rows = 0;
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            rows = getDBHelper().executeRaw("insert into t_student(id,deleted,name) values(?,false,?)", CommonOps.getRandomLong(), name);
        } else {
            rows = getDBHelper().executeRaw("insert into t_student(deleted,name) values(false,?)", name);
        }
        assert rows == 1;
        StudentDO student = getDBHelper().getOne(StudentDO.class, "where name=?", name);
        assert student.getName().equals(name);

        // 插入2个
        String name1 = UUID.randomUUID().toString();
        String name2 = UUID.randomUUID().toString();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            rows = getDBHelper().executeRaw("insert into t_student(id,deleted,name) values(?,false,?),(?,false,?)",
                    CommonOps.getRandomLong(), name1, CommonOps.getRandomLong(), name2);
        } else {
            rows = getDBHelper().executeRaw("insert into t_student(deleted,name) values(false,?),(false,?)", name1, name2);
        }
        assert rows == 2;
        StudentDO student1 = getDBHelper().getOne(StudentDO.class, "where name=?", name1);
        StudentDO student2 = getDBHelper().getOne(StudentDO.class, "where name=?", name2);
        assert student1.getName().equals(name1);
        assert student2.getName().equals(name2);

        // 使用paramMap参数方式插入2个
        Map<String, Object> params = new HashMap<>();
        params.put("name1", UUID.randomUUID().toString());
        params.put("name2", UUID.randomUUID().toString());
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            params.put("id1", CommonOps.getRandomLong());
            params.put("id2", CommonOps.getRandomLong());
            rows = getDBHelper().executeRaw("insert into t_student(id,deleted,name) values(:id1,false,:name1),(:id2,false,:name2)", params);
        } else {
            rows = getDBHelper().executeRaw("insert into t_student(deleted,name) values(false,:name1),(false,:name2)", params);
        }
        assert rows == 2;
        student1 = getDBHelper().getOne(StudentDO.class, "where name=?", params.get("name1"));
        student2 = getDBHelper().getOne(StudentDO.class, "where name=?", params.get("name2"));
        assert student1.getName().equals(params.get("name1"));
        assert student2.getName().equals(params.get("name2"));

        // 测试没有paramMap的插入，插入1个
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            assert getDBHelper().executeRaw("insert into t_student(id,deleted,name) values(rand(),false,'nick')",
                    (Map<String, Object>) null) == 1;
        } else {
            assert getDBHelper().executeRaw("insert into t_student(deleted,name) values(false,'nick')",
                    (Map<String, Object>) null) == 1;
        }

        // 复制的方式插入2个
        List<String> names = ListUtils.newArrayList(name1, name2);
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            rows = getDBHelper().executeRaw("insert into t_student(id,deleted,name) select rand(),false,name from t_student"
                    + " where name in (?)", names);
        } else {
            rows = getDBHelper().executeRaw("insert into t_student(deleted,name) select false,name from t_student"
                    + " where name in (?)", names);
        }
        assert rows == 2;
        assert getDBHelper().getAll(StudentDO.class, "where name=?", name1).size() == 2;
        assert getDBHelper().getAll(StudentDO.class, "where name=?", name2).size() == 2;
    }

    @Test
    public void testUpdate() {
        String name = UUID.randomUUID().toString();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            getDBHelper().executeRaw("insert into t_student(id,deleted,name) values(?,false,?)", CommonOps.getRandomLong(), name);
        } else {
            getDBHelper().executeRaw("insert into t_student(deleted,name) values(false,?)", name);
        }

        String name1 = UUID.randomUUID().toString();
        int rows = getDBHelper().executeRaw("update t_student set name=? where name=?", name1, name);

        assert rows == 1;
        StudentDO student = getDBHelper().getOne(StudentDO.class, "where name=?", name1);
        assert student.getName().equals(name1);
    }

    @Test
    public void testDelete() {
        String name = UUID.randomUUID().toString();

        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            getDBHelper().executeRaw("insert into t_student(id,deleted,name) values(?,false,?)",
                    CommonOps.getRandomLong(), name);
        } else {
            getDBHelper().executeRaw("insert into t_student(deleted,name) values(false,?)", name);
        }

        int rows = getDBHelper().executeRaw("delete from t_student where name=?", name);
        assert rows == 1;

        StudentDO student = getDBHelper().getOne(StudentDO.class, "where name=?", name);
        assert student == null;
    }

    @Test
    public void testCreateTruncateDropTable() {
        String name = UUID.randomUUID().toString().replace("-", "");

        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            getDBHelper().executeRaw("create table t_raw_" + name + "(name String) ENGINE=MergeTree ORDER BY(name)");
        } else {
            getDBHelper().executeRaw("create table t_raw_" + name + "(name varchar(1024))");
        }

        assert getDBHelper().executeRaw("insert into t_raw_" + name + "(name)values(?)", name) == 1;

        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_raw_" + name) == 1;

        getDBHelper().executeRaw("truncate table t_raw_" + name);

        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_raw_" + name) == 0;

        getDBHelper().executeRaw("drop table t_raw_" + name);
    }

}
