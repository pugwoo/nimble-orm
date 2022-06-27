package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestDBHelper_insert {

    @Autowired
    private DBHelper dbHelper;

    @Test 
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

    @Test 
    public void testBatchInsert() {
        int TOTAL = 1000;

        List<StudentDO> list = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            StudentDO studentDO = new StudentDO();
            studentDO.setName(UUID.randomUUID().toString().replace("-", ""));
            list.add(studentDO);
        }

        dbHelper.setTimeoutWarningValve(1);

        long start = System.currentTimeMillis();
        int rows = dbHelper.insertBatchWithoutReturnId(list);
        long end = System.currentTimeMillis();
        System.out.println("batch insert cost:" + (end - start) + "ms");
        assert rows == TOTAL;

        start = System.currentTimeMillis();
        rows = dbHelper.insertBatchWithoutReturnId(new HashSet<>(list));
        end = System.currentTimeMillis();
        System.out.println("batch insert cost:" + (end - start) + "ms");
        assert rows == TOTAL;
    }

    @Test 
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

}
