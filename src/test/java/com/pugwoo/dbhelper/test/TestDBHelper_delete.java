package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentTrueDeleteDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelper_delete {

    @Autowired
    private DBHelper dbHelper;

    @Test
    @Rollback(false)
    public void deleteByKey() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        assert dbHelper.getOne(StudentDO.class, "where id=?", studentDO.getId()).getName().equals(studentDO.getName()); // 反查确保id正确

        int rows = dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
        Assert.assertTrue(rows == 1);

        rows = dbHelper.deleteByKey(StudentDO.class, studentDO.getId());
        Assert.assertTrue(rows == 0);

        // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况

        studentDO = CommonOps.insertOne(dbHelper);

        assert dbHelper.getOne(StudentDO.class, "where id=?", studentDO.getId()).getName().equals(studentDO.getName()); // 反查确保id正确

        rows = dbHelper.deleteByKey(studentDO);
        Assert.assertTrue(rows == 1);

        rows = dbHelper.deleteByKey(studentDO);
        Assert.assertTrue(rows == 0);
    }

    @Test @Rollback(false)
    public void batchDelete() {

        int random = 10 + new Random().nextInt(10);

        List<StudentDO> insertBatch = CommonOps.insertBatch(dbHelper, random);
        int rows = dbHelper.deleteByKey(insertBatch);
        Assert.assertTrue(rows == insertBatch.size());

        insertBatch = CommonOps.insertBatch(dbHelper,random);
        rows = dbHelper.delete(StudentDO.class, "where 1=?", 1);
        Assert.assertTrue(rows >= random);


        insertBatch = CommonOps.insertBatch(dbHelper,random);

        List<Object> differents = new ArrayList<Object>();
        for(StudentDO studentDO : insertBatch) {
            differents.add(studentDO);
        }
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("school");
        dbHelper.insert(schoolDO);
        differents.add(schoolDO);

        rows = dbHelper.deleteByKey(differents);
        assert rows == random + 1;
    }

    @Test
    @Rollback(false)
    public void testTrueDelete() {
        StudentTrueDeleteDO studentTrueDeleteDO = new StudentTrueDeleteDO();
        studentTrueDeleteDO.setName("john");
        dbHelper.insert(studentTrueDeleteDO);

        int rows = dbHelper.deleteByKey(StudentTrueDeleteDO.class, studentTrueDeleteDO.getId());
        Assert.assertTrue(rows == 1);

        rows = dbHelper.deleteByKey(StudentTrueDeleteDO.class, studentTrueDeleteDO.getId());
        Assert.assertTrue(rows == 0);

        // 上下两种写法都可以，但是上面的适合当主键只有一个key的情况

        studentTrueDeleteDO = new StudentTrueDeleteDO();
        studentTrueDeleteDO.setName("john");
        dbHelper.insert(studentTrueDeleteDO);

        rows = dbHelper.deleteByKey(studentTrueDeleteDO);
        Assert.assertTrue(rows == 1);

        rows = dbHelper.deleteByKey(studentTrueDeleteDO);
        Assert.assertTrue(rows == 0);

        //
        studentTrueDeleteDO = new StudentTrueDeleteDO();
        studentTrueDeleteDO.setName("john");
        dbHelper.insert(studentTrueDeleteDO);

        rows = dbHelper.delete(StudentTrueDeleteDO.class, "where name=?", "john");
        Assert.assertTrue(rows > 0);

        rows = dbHelper.delete(StudentTrueDeleteDO.class, "where name=?", "john");
        Assert.assertTrue(rows == 0);


        // 批量物理删除
        List<StudentTrueDeleteDO> list = new ArrayList<StudentTrueDeleteDO>();
        int size = CommonOps.getRandomInt(10, 10);
        for(int i = 0; i < size; i++) {
            studentTrueDeleteDO = new StudentTrueDeleteDO();
            studentTrueDeleteDO.setName(CommonOps.getRandomName("jack"));
            dbHelper.insert(studentTrueDeleteDO);

            list.add(studentTrueDeleteDO);
        }

        List<Long> ids = new ArrayList<Long>();
        for(StudentTrueDeleteDO o : list) {
            ids.add(o.getId());
        }

        rows = dbHelper.deleteByKey(list);
        assert rows == list.size();

        List<StudentTrueDeleteDO> all = dbHelper.getAll(StudentTrueDeleteDO.class, "where id in (?)", ids);
        assert all.isEmpty();
    }

    @Test
    @Rollback(false)
    public void testTurnOffSoftDelete() {
        int counts1 = 100 + new Random().nextInt(100);
        CommonOps.insertBatch(dbHelper, counts1);

        // 先制造点软删除
        dbHelper.delete(StudentDO.class, "Where 1=1");

        int counts2 = 100 + new Random().nextInt(100);
        CommonOps.insertBatch(dbHelper, counts2);

        int total = dbHelper.getCount(StudentTrueDeleteDO.class);
        int softTotal = dbHelper.getCount(StudentDO.class);

        assert total >= counts1 + counts2;
        assert total >= softTotal + 100;
        assert softTotal >= counts2;

        dbHelper.turnOnSoftDelete(StudentDO.class); // 测速错误顺序
        dbHelper.turnOffSoftDelete(null); // 测试错误参数

        dbHelper.turnOffSoftDelete(StudentDO.class);

        int turnoffTotal = dbHelper.getCount(StudentDO.class);
        assert total == turnoffTotal;

        // 物理删除了
        dbHelper.delete(StudentDO.class, "where 1=1");

        dbHelper.turnOnSoftDelete(StudentDO.class);
        dbHelper.turnOnSoftDelete(null); // 测试错误参数

        total = dbHelper.getCount(StudentTrueDeleteDO.class);
        assert total == 0;

    }

    @Test
    @Rollback(false)
    public void testDeleteEx() {
        boolean ex = false;
        try {
            dbHelper.delete(StudentDO.class, "  \t   "); // 自定义删除允许不传条件
        } catch (Exception e) {
            assert e instanceof InvalidParameterException;
            ex = true;
        }
        assert ex;


        StudentDO studentDO = new StudentDO();
        ex = false;
        try {
            dbHelper.deleteByKey(StudentDO.class, null);
        } catch (Exception e) {
            assert e instanceof NullKeyValueException;
            ex = true;
        }
        assert ex;
    }
}
