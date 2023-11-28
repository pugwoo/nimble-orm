package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.test.entity.*;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Test4Delete_Basic {

    public abstract DBHelper getDBHelper();

    /**通过key删除*/
    @Test
    public void deleteByKey() {
        StudentDO studentDO = CommonOps.insertOne(getDBHelper());

        int rows = getDBHelper().delete(StudentDO.class, "where id=?", studentDO.getId());
        assert rows == 1;
        rows = getDBHelper().delete(StudentDO.class, "where id=?", studentDO.getId());
        // clickhouse没有办法正确返回修改条数
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 0;
        }

        assert getDBHelper().getByKey(StudentDO.class, studentDO.getId()) == null;

        // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况

        studentDO = CommonOps.insertOne(getDBHelper());

        rows = getDBHelper().delete(studentDO);
        assert rows == 1;
        rows = getDBHelper().delete(studentDO);
        // clickhouse没有办法正确返回修改条数
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 0;
        }
        assert getDBHelper().getByKey(StudentDO.class, studentDO.getId()) == null;
    }

    /**通过写where条件的自定义删除*/
    @Test
    public void testDeleteWhere() {
        StudentDO studentDO = CommonOps.insertOne(getDBHelper());
        assert getDBHelper().delete(StudentDO.class, "where name=?", studentDO.getName()) == 1;
        int rows = getDBHelper().delete(StudentDO.class, "where name=?", studentDO.getName());
        // clickhouse没法正确返回修改条数
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 0;
        }
    }

    /**通过key批量删除*/
    @Test 
    public void batchDelete() {
        int random = 10 + new Random().nextInt(10);

        List<StudentDO> insertBatch = CommonOps.insertBatch(getDBHelper(), random);
        int rows = getDBHelper().delete(insertBatch);
        // clickhouse只会返回1
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == insertBatch.size();
        } else {
            assert rows == 1;
        }

        for (StudentDO studentDO : insertBatch) {
            assert getDBHelper().getByKey(StudentDO.class, studentDO.getId()) == null;
        }

        getDBHelper().deleteHard(StudentDO.class, "where 1=1");
        CommonOps.insertBatch(getDBHelper(),random);
        rows = getDBHelper().delete(StudentDO.class, "where 1=?", 1);

        // clickhouse只会返回1
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == random;
        } else {
            assert rows == 1;
        }

        insertBatch = CommonOps.insertBatch(getDBHelper(),random);

        // 测试批量删除list是多种class类型
        List<Object> differents = new ArrayList<>(insertBatch);
        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), "school");
        differents.add(schoolDO);

        rows = getDBHelper().delete(differents);
        assert rows == random + 1;
    }

    /**测试deleted的值设置为id的情况*/
    @Test
    public void deleteAndSetId() {
        // postgresql的删除现在用boolean，所以不支持这个设置为id的情况，这里就不测试了
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.POSTGRESQL) {
            return;
        }

        StudentDO studentDO = CommonOps.insertOne(getDBHelper());

        StudentDeleteSetIdDO stu1 = new StudentDeleteSetIdDO();
        stu1.setId(studentDO.getId());

        getDBHelper().delete(stu1);

        assert getDBHelper().getByKey(StudentDO.class, studentDO.getId()) == null; // 已经被删除了
        assert getDBHelper().getByKey(StudentDeleteSetIdDO.class, studentDO.getId()) == null; // 已经被删除了

        StudentDeleteSetIdDO2 stuDelete3 =
                getDBHelper().getByKey(StudentDeleteSetIdDO2.class, studentDO.getId());
        assert stuDelete3.getId().equals(stuDelete3.getDeleted()); // 验证一下设置的delete是否是对的
    }

    @Test
    public void testTrueDelete() {
        StudentHardDeleteDO studentHardDeleteDO = new StudentHardDeleteDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            studentHardDeleteDO.setId(CommonOps.getRandomLong());
        }
        studentHardDeleteDO.setName("john");
        getDBHelper().insert(studentHardDeleteDO);

        int rows = getDBHelper().delete(StudentHardDeleteDO.class, "where id=?", studentHardDeleteDO.getId());
        assert rows == 1;

        rows = getDBHelper().delete(StudentHardDeleteDO.class, "where id=?", studentHardDeleteDO.getId());

        // clickhouse没有办法正确返回修改条数
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 0;
        }

        // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况

        studentHardDeleteDO = new StudentHardDeleteDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            studentHardDeleteDO.setId(CommonOps.getRandomLong());
        }
        studentHardDeleteDO.setName("john");
        getDBHelper().insert(studentHardDeleteDO);

        rows = getDBHelper().delete(studentHardDeleteDO);
        assert rows == 1;

        rows = getDBHelper().delete(studentHardDeleteDO);
        // clickhouse没有办法正确返回修改条数
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 0;
        } else {
            assert rows == 1;
        }

        //
        studentHardDeleteDO = new StudentHardDeleteDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            studentHardDeleteDO.setId(CommonOps.getRandomLong());
        }
        studentHardDeleteDO.setName("john");
        getDBHelper().insert(studentHardDeleteDO);

        rows = getDBHelper().delete(StudentHardDeleteDO.class, "where name=?", "john");
        assert rows > 0;

        rows = getDBHelper().delete(StudentHardDeleteDO.class, "where name=?", "john");
        // clickhouse没有办法正确返回修改条数
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 0;
        } else {
            assert rows == 1;
        }

        // 批量物理删除
        List<StudentHardDeleteDO> list = new ArrayList<StudentHardDeleteDO>();
        int size = CommonOps.getRandomInt(10, 10);
        for(int i = 0; i < size; i++) {
            studentHardDeleteDO = new StudentHardDeleteDO();
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                studentHardDeleteDO.setId(CommonOps.getRandomLong());
            }
            studentHardDeleteDO.setName(CommonOps.getRandomName("jack"));
            getDBHelper().insert(studentHardDeleteDO);

            list.add(studentHardDeleteDO);
        }

        List<Long> ids = new ArrayList<Long>();
        for(StudentHardDeleteDO o : list) {
            ids.add(o.getId());
        }

        rows = getDBHelper().delete(list);
        // clickhouse没有办法正确返回修改条数
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == list.size();
        } else {
            assert rows == 1;
        }

        List<StudentHardDeleteDO> all = getDBHelper().getAll(StudentHardDeleteDO.class, "where id in (?)", ids);
        assert all.isEmpty();
    }

    @Test
    public void testTurnOffSoftDelete() {
        getDBHelper().deleteHard(StudentDO.class, "where 1=1");

        int counts1 = 10 + new Random().nextInt(10);
        CommonOps.insertBatchNoReturnId(getDBHelper(), counts1);

        // 先制造点软删除
        getDBHelper().delete(StudentDO.class, "Where 1=1");

        int counts2 = 10 + new Random().nextInt(10);
        CommonOps.insertBatchNoReturnId(getDBHelper(), counts2);

        long total = getDBHelper().getCount(StudentHardDeleteDO.class);
        long softTotal = getDBHelper().getCount(StudentDO.class);

        assert total == counts1 + counts2;
        assert softTotal == counts2;

        // 物理删除了
        getDBHelper().deleteHard(StudentDO.class, "where 1=1");

        total = getDBHelper().getCount(StudentHardDeleteDO.class);
        assert total == 0;
    }

    @Test
    public void testDeleteEx() {
        boolean ex = false;
        try {
            getDBHelper().delete(StudentDO.class, "  \t   "); // 自定义删除允许不传条件
        } catch (Exception e) {
            assert e instanceof InvalidParameterException;
            ex = true;
        }
        assert ex;


        ex = false;
        try {
            StudentDO studentDO = new StudentDO();
            getDBHelper().delete(studentDO);
        } catch (Exception e) {
            assert e instanceof NullKeyValueException;
            ex = true;
        }
        assert ex;
    }

    @Test
    public void testBatchDeleteWithDeleteScript() {
        StudentDO stu1 = CommonOps.insertOne(getDBHelper());
        StudentDO stu2 = CommonOps.insertOne(getDBHelper());

        StudentWithDeleteScriptDO s1 = new StudentWithDeleteScriptDO();
        s1.setId(stu1.getId());
        StudentWithDeleteScriptDO s2 = new StudentWithDeleteScriptDO();
        s2.setId(stu2.getId());

        int rows = getDBHelper().delete(ListUtils.newArrayList(s1, s2));
        assert rows == 2;

        List<StudentHardDeleteDO> list = getDBHelper().getAll(StudentHardDeleteDO.class,
                "where id in (?)", ListUtils.newArrayList(s1.getId(), s2.getId()));
        assert list.size() == 2;
        assert list.get(0).getName().equals("deleteddata");
        assert list.get(1).getName().equals("deleteddata");
    }

    @Data
    @Table("t_student")
    private static class StudentWithDeleteScriptDO {
        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;
        @Column(value = "deleted", softDelete = {"false", "true"})
        private Boolean deleted;
        @Column(value = "name", deleteValueScript = "'deleted' + 'data'")
        private String name;
    }

}
