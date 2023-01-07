package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentRandomNameDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
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

    @Test
    public void testInsert2() {
        StudentDO studentDO = new StudentDO();
        studentDO.setName("nick888");
        // studentDO.setAge(28);

        int row = dbHelper.insert(studentDO); // 如果值为null，则用数据库默认值
        // int row = dbHelper.insertWithNull(studentDO); // 强制设置数据库
        System.out.println("affected rows:" + row);
        System.out.println(studentDO);

        // 测试批量写入
        List<StudentDO> students = new ArrayList<StudentDO>();
        for(int i = 0; i < 10; i++) {
            StudentDO stu = new StudentDO();
            stu.setName("test" + i);
            stu.setAge(i);
            students.add(stu);
        }
        row = dbHelper.insert(students);
        assert row == 10;

        // 测试批量写入，Set参数
        Set<StudentDO> studentSet = new HashSet<>();
        for(int i = 0; i < 10; i++) {
            StudentDO stu = new StudentDO();
            stu.setName("test" + i);
            stu.setAge(i);
            studentSet.add(stu);
        }
        row = dbHelper.insert(studentSet);
        assert row == 10;

        // 测试random值
        StudentRandomNameDO studentRandomNameDO = new StudentRandomNameDO();
        dbHelper.insert(studentRandomNameDO);
        assert studentRandomNameDO.getId() != null;
        assert !studentRandomNameDO.getName().isEmpty();
    }

    @Test
    public void testMaxStringLength() {
        StudentDO studentDO = new StudentDO();
        studentDO.setName("nick1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");

        dbHelper.insert(studentDO);
        assert studentDO.getName().length()==32; // 注解配置了32位长度

        StudentDO student2 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert student2.getName().length()==32;

        student2.setName("nick222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222");
        dbHelper.update(student2);
        assert student2.getName().length()==32;

        StudentDO student3 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert student3.getName().length()==32;
    }

    @Test
    public void testInsertOrUpdateWithNull() {
        assert dbHelper.insertOrUpdateWithNull(null) == 0;

        StudentDO studentDO = new StudentDO();
        studentDO.setName(CommonOps.getRandomName("tom"));
        assert dbHelper.insertOrUpdateWithNull(null) == 0;
        assert dbHelper.insertOrUpdateWithNull(studentDO) == 1;
        assert studentDO.getId() != null;

        StudentDO student2 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert student2.getName().equals(studentDO.getName());

        student2.setName(CommonOps.getRandomName("jim"));
        assert dbHelper.insertOrUpdateWithNull(student2) == 1;
        assert student2.getId().equals(studentDO.getId());

        StudentDO student3 = dbHelper.getByKey(StudentDO.class, student2.getId());
        assert student2.getName().equals(student3.getName());
    }

    @Test
    public void testInsertOrUpdate() {
        assert dbHelper.insertOrUpdate(null) == 0;

        StudentDO studentDO = new StudentDO();
        studentDO.setName(CommonOps.getRandomName("tom"));
        assert dbHelper.insertOrUpdate(null) == 0;
        assert dbHelper.insertOrUpdate(studentDO) == 1;
        assert studentDO.getId() != null;

        StudentDO student2 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert student2.getName().equals(studentDO.getName());

        student2.setName(CommonOps.getRandomName("jim"));
        assert dbHelper.insertOrUpdate(student2) == 1;
        assert student2.getId().equals(studentDO.getId());

        StudentDO student3 = dbHelper.getByKey(StudentDO.class, student2.getId());
        assert student2.getName().equals(student3.getName());
    }
}
