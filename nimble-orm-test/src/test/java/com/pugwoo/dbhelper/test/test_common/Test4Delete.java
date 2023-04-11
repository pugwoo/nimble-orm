package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.test.entity.*;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class Test4Delete {

    @Autowired
    private DBHelper dbHelper;

    /**通过key删除*/
    @Test
    public void deleteByKey() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        int rows = dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
        assert rows == 1;
        rows = dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
        assert rows == 0;
        assert dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null;

        // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况

        studentDO = CommonOps.insertOne(dbHelper);

        rows = dbHelper.deleteByKey(studentDO);
        assert rows == 1;
        rows = dbHelper.deleteByKey(studentDO);
        assert rows == 0;
        assert dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null;
    }

    /**通过写where条件的自定义删除*/
    @Test
    public void testDeleteWhere() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        assert dbHelper.delete(StudentDO.class, "where name=?", studentDO.getName()) == 1;
        assert dbHelper.delete(StudentDO.class, "where name=?", studentDO.getName()) == 0;
    }

    /**通过key批量删除*/
    @Test 
    public void batchDelete() {
        int random = 10 + new Random().nextInt(10);

        List<StudentDO> insertBatch = CommonOps.insertBatch(dbHelper, random);
        int rows = dbHelper.deleteByKey(insertBatch);
        assert rows == insertBatch.size();
        for (StudentDO studentDO : insertBatch) {
            assert dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null;
        }

        DBHelper.turnOffSoftDelete(StudentDO.class);
        dbHelper.delete(StudentDO.class, "where 1=1");
        DBHelper.turnOnSoftDelete(StudentDO.class);
        CommonOps.insertBatch(dbHelper,random);
        rows = dbHelper.delete(StudentDO.class, "where 1=?", 1);
        assert rows == random;

        insertBatch = CommonOps.insertBatch(dbHelper,random);

        // 测试批量删除list是多种class类型
        List<Object> differents = new ArrayList<>(insertBatch);
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("school");
        dbHelper.insert(schoolDO);
        differents.add(schoolDO);

        rows = dbHelper.deleteByKey(differents);
        assert rows == random + 1;
    }

    /**测试deleted的值设置为id的情况*/
    @Test
    public void deleteAndSetId() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        StudentDeleteSetIdDO stu1 = new StudentDeleteSetIdDO();
        stu1.setId(studentDO.getId());

        dbHelper.deleteByKey(stu1);

        assert dbHelper.getByKey(StudentDO.class, studentDO.getId()) == null; // 已经被删除了
        assert dbHelper.getByKey(StudentDeleteSetIdDO.class, studentDO.getId()) == null; // 已经被删除了

        StudentDeleteSetIdDO2 stuDelete3 =
                dbHelper.getByKey(StudentDeleteSetIdDO2.class, studentDO.getId());
        assert stuDelete3.getId().equals(stuDelete3.getDeleted()); // 验证一下设置的delete是否是对的
    }

    @Test
    public void testTrueDelete() {
        StudentHardDeleteDO studentHardDeleteDO = new StudentHardDeleteDO();
        studentHardDeleteDO.setName("john");
        dbHelper.insert(studentHardDeleteDO);

        int rows = dbHelper.deleteByKey(StudentHardDeleteDO.class, studentHardDeleteDO.getId());
        assert rows == 1;

        rows = dbHelper.deleteByKey(StudentHardDeleteDO.class, studentHardDeleteDO.getId());
        assert rows == 0;

        // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况

        studentHardDeleteDO = new StudentHardDeleteDO();
        studentHardDeleteDO.setName("john");
        dbHelper.insert(studentHardDeleteDO);

        rows = dbHelper.deleteByKey(studentHardDeleteDO);
        assert rows == 1;

        rows = dbHelper.deleteByKey(studentHardDeleteDO);
        assert rows == 0;

        //
        studentHardDeleteDO = new StudentHardDeleteDO();
        studentHardDeleteDO.setName("john");
        dbHelper.insert(studentHardDeleteDO);

        rows = dbHelper.delete(StudentHardDeleteDO.class, "where name=?", "john");
        assert rows > 0;

        rows = dbHelper.delete(StudentHardDeleteDO.class, "where name=?", "john");
        assert rows == 0;


        // 批量物理删除
        List<StudentHardDeleteDO> list = new ArrayList<StudentHardDeleteDO>();
        int size = CommonOps.getRandomInt(10, 10);
        for(int i = 0; i < size; i++) {
            studentHardDeleteDO = new StudentHardDeleteDO();
            studentHardDeleteDO.setName(CommonOps.getRandomName("jack"));
            dbHelper.insert(studentHardDeleteDO);

            list.add(studentHardDeleteDO);
        }

        List<Long> ids = new ArrayList<Long>();
        for(StudentHardDeleteDO o : list) {
            ids.add(o.getId());
        }

        rows = dbHelper.deleteByKey(list);
        assert rows == list.size();

        List<StudentHardDeleteDO> all = dbHelper.getAll(StudentHardDeleteDO.class, "where id in (?)", ids);
        assert all.isEmpty();
    }

    @Test
    public void testTurnOffSoftDelete() {
        DBHelper.turnOffSoftDelete(StudentDO.class);
        dbHelper.delete(StudentDO.class, "where 1=1");
        DBHelper.turnOnSoftDelete(StudentDO.class);

        int counts1 = 10 + new Random().nextInt(10);
        CommonOps.insertBatchNoReturnId(dbHelper, counts1);

        // 先制造点软删除
        dbHelper.delete(StudentDO.class, "Where 1=1");

        int counts2 = 10 + new Random().nextInt(10);
        CommonOps.insertBatchNoReturnId(dbHelper, counts2);

        long total = dbHelper.getCount(StudentHardDeleteDO.class);
        long softTotal = dbHelper.getCount(StudentDO.class);

        assert total == counts1 + counts2;
        assert softTotal == counts2;

        DBHelper.turnOnSoftDelete(StudentDO.class); // 测速错误顺序
        DBHelper.turnOffSoftDelete((Class<?>) null); // 测试错误参数

        DBHelper.turnOffSoftDelete(StudentDO.class);

        long turnoffTotal = dbHelper.getCount(StudentDO.class);
        assert total == turnoffTotal;

        // 物理删除了
        dbHelper.delete(StudentDO.class, "where 1=1");

        DBHelper.turnOnSoftDelete(StudentDO.class);
        DBHelper.turnOnSoftDelete((Class<?>) null); // 测试错误参数

        total = dbHelper.getCount(StudentHardDeleteDO.class);
        assert total == 0;
    }

    @Test
    public void testDeleteEx() {
        boolean ex = false;
        try {
            dbHelper.delete(StudentDO.class, "  \t   "); // 自定义删除允许不传条件
        } catch (Exception e) {
            assert e instanceof InvalidParameterException;
            ex = true;
        }
        assert ex;


        ex = false;
        try {
            dbHelper.deleteByKey(StudentDO.class, null);
        } catch (Exception e) {
            assert e instanceof NullKeyValueException;
            ex = true;
        }
        assert ex;
    }

    @Test
    public void testBatchDeleteWithDeleteScript() {
        StudentDO stu1 = CommonOps.insertOne(dbHelper);
        StudentDO stu2 = CommonOps.insertOne(dbHelper);

        StudentWithDeleteScriptDO s1 = new StudentWithDeleteScriptDO();
        s1.setId(stu1.getId());
        StudentWithDeleteScriptDO s2 = new StudentWithDeleteScriptDO();
        s2.setId(stu2.getId());

        int rows = dbHelper.deleteByKey(ListUtils.newArrayList(s1, s2));
        assert rows == 2;

        DBHelper.turnOffSoftDelete(StudentWithDeleteScriptDO.class);

        List<StudentHardDeleteDO> list = dbHelper.getAll(StudentHardDeleteDO.class,
                "where id in (?)", ListUtils.newArrayList(s1.getId(), s2.getId()));
        assert list.size() == 2;
        assert list.get(0).getName().equals("deleteddata");
        assert list.get(1).getName().equals("deleteddata");

        DBHelper.turnOnSoftDelete(StudentWithDeleteScriptDO.class);
    }

    @Data
    @Table("t_student")
    private static class StudentWithDeleteScriptDO {
        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;
        @Column(value = "deleted", softDelete = {"0", "1"})
        private Boolean deleted;
        @Column(value = "name", deleteValueScript = "'deleted' + 'data'")
        private String name;
    }

}
