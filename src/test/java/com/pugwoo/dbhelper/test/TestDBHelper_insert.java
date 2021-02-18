package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.TypesDO;
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
import java.util.UUID;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelper_insert {

    @Autowired
    private DBHelper dbHelper;

    @Test @Rollback(false)
    public void testInsert() {
        StudentDO studentDO = new StudentDO();
        studentDO.setName("mytestname");
        studentDO.setAge(12);
        dbHelper.insert(studentDO);

        StudentDO st = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert st.getName().equals("mytestname");

        studentDO = new StudentDO();
        studentDO.setId(null);
        studentDO.setName(null);
        dbHelper.insertWithNull(studentDO);
        st = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert st.getName() == null;
    }

    @Test @Rollback(false)
    public void testBatchInsert() {
        int TOTAL = 10000;

        List<StudentDO> list = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            StudentDO studentDO = new StudentDO();
            studentDO.setName(UUID.randomUUID().toString().replace("-", ""));
            list.add(studentDO);
        }

        long start = System.currentTimeMillis();
        int rows = dbHelper.insertBatchWithoutReturnId(list);
        long end = System.currentTimeMillis();
        System.out.println("batch insert cost:" + (end - start) + "ms");
        assert rows == TOTAL;
    }

    @Test @Rollback(false)
    public void testInsertId() {
        // 测试插入时，如果自增id 同时 又指定了id，查回id是否正常
        Long id = (long) (new Random().nextInt(100000000) + 100001234);
        String name = UUID.randomUUID().toString().replace("-", "");
        StudentDO studentDO = new StudentDO();
        studentDO.setId(id);
        studentDO.setName(name);
        dbHelper.insert(studentDO);

        assert id.equals(studentDO.getId());

        StudentDO studentDO2 = dbHelper.getByKey(StudentDO.class, id);
        assert studentDO2.getName().equals(name);
    }

    @Test
    @Rollback(false)
    public void testInsertWhereNotExists() {
        StudentDO studentDO = new StudentDO();
        studentDO.setName(CommonOps.getRandomName("nick"));

        int row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
        Assert.assertTrue(row == 1);

        // 这个不会插入，写不写`where`关键字都可以
        row = dbHelper.insertWhereNotExist(studentDO, "where name=?", studentDO.getName());
        Assert.assertTrue(row == 0);

        // 这个不会插入
        row = dbHelper.insertWhereNotExist(studentDO, "name=?", studentDO.getName());
        Assert.assertTrue(row == 0);

        row = dbHelper.deleteByKey(studentDO);
        Assert.assertTrue(row == 1);

        // 删除后再插入
        studentDO.setId(null);
        row = dbHelper.insertWithNullWhereNotExist(studentDO, "name=?", studentDO.getName());
        Assert.assertTrue(row == 1);

        row = dbHelper.insertWithNullWhereNotExist(studentDO, "name=?", studentDO.getName());
        Assert.assertTrue(row == 0);

        row = dbHelper.insertWithNullWhereNotExist(studentDO, "name=?", studentDO.getName());
        Assert.assertTrue(row == 0);

        // 如果没有指定where条件，则等价于插入
        studentDO = new StudentDO();
        studentDO.setName(CommonOps.getRandomName("nick"));
        assert dbHelper.insertWhereNotExist(studentDO, null) == 1;
        assert studentDO.getId() != null;
        assert dbHelper.getByKey(StudentDO.class, studentDO.getId()).getName().equals(studentDO.getName());

        // 测试不需要自增id的情况
        TypesDO typesDO = new TypesDO();
        typesDO.setId1(new Random().nextLong());
        typesDO.setId2(new Random().nextLong());
        assert dbHelper.insertWhereNotExist(typesDO, "where id1=? and id2=?",
                typesDO.getId1(), typesDO.getId2()) == 1;
        assert dbHelper.insertWhereNotExist(typesDO, "where id1=? and id2=?",
                typesDO.getId1(), typesDO.getId2()) == 0;
        assert dbHelper.getOne(TypesDO.class, "where id1=? and id2=?",
                typesDO.getId1(), typesDO.getId2()) != null;
    }
}
