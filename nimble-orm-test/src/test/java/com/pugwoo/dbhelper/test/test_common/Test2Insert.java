package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.NoColumnAnnotationException;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentRandomNameDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SpringBootTest
public class Test2Insert {

    @Autowired
    private DBHelper dbHelper;

    private String uuidName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Test 
    public void testInsert() {
        StudentDO studentDO = new StudentDO();
        studentDO.setName(uuidName());
        studentDO.setAge(12);
        dbHelper.insert(studentDO);

        StudentDO st = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert st.getName().equals(studentDO.getName());
        assert st.getAge().equals(studentDO.getAge());

        // 插入插入null值
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
            studentDO.setName(uuidName());
            list.add(studentDO);
        }

        dbHelper.setTimeoutWarningValve(1); // 改小超时阈值，让慢sql打印出来

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
    public void testInsertBatchWithoutReturnIdWithMapList() {
        int TOTAL = 1000;

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("name", uuidName());
            studentMap.put("age", 0);
            list.add(studentMap);
        }

        dbHelper.setTimeoutWarningValve(1); // 改小超时阈值，让慢sql打印出来

        long start = System.currentTimeMillis();
        int rows = dbHelper.insertBatchWithoutReturnId("t_student", list);
        long end = System.currentTimeMillis();
        System.out.println("batch insert cost:" + (end - start) + "ms");
        assert rows == TOTAL;

        start = System.currentTimeMillis();
        rows = dbHelper.insertBatchWithoutReturnId("t_student", new HashSet<>(list));
        end = System.currentTimeMillis();
        System.out.println("batch insert cost:" + (end - start) + "ms");
        assert rows == TOTAL;
    }

    /**测试插入时指定了id，依然可以准确获得id的场景*/
    @Transactional // 开启事务是为了让下面的测试共用一个连接，而不是因为获取自增id需要事务
    @Rollback(false)
    @Test 
    public void testInsertWithSpecificId() {
        CommonOps.insertOne(dbHelper); // 先插入一个，占了自增id，下面开始用户自行指定的id

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

        // 测试uuid的情况
        String uuidName = uuidName();
        UuidDO2 uuidDO2 = new UuidDO2();
        uuidDO2.setUuid(uuidName);
        uuidDO2.setName("nick");
        assert dbHelper.insert(uuidDO2) == 1;
        assert uuidName.equals(uuidDO2.getUuid());
    }

    @Data
    @Table("t_uuid")
    public static class UuidDO2 {

        // 故意对一个varchar的key设置isAutoIncrement = true，实际上mysql数据库是不允许的
        @Column(value = "uuid", isKey = true, isAutoIncrement = true)
        private String uuid;

        @Column(value = "name")
        private String name;

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
        studentDO.setName("nick1111111111111111111111111111111111111111111111111111111111");

        dbHelper.insert(studentDO);
        assert studentDO.getName().length()==32; // 注解配置了32位长度

        StudentDO student2 = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert student2.getName().length()==32;

        student2.setName("nick22222222222222222222222222222222222222222222222222222222222222222222");
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
        assert dbHelper.insertOrUpdate((StudentDO) null) == 0;
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

    @Test
    public void testInsertOrUpdateNoKeyDO() {
        NoKeyStudentDO student = new NoKeyStudentDO();
        student.setName(uuidName());

        assert dbHelper.insertOrUpdate(student) == 1; // 因为student没有key，所以会插入处理

        // 重新查询数据，可以查得到
        assert dbHelper.getOne(StudentDO.class, "where name=?", student.getName())
                .getName().equals(student.getName());
    }

    @Data
    @Table("t_student")
    public static class NoKeyStudentDO {
        @Column(value = "deleted", softDelete = {"0", "1"})
        private Boolean deleted;

        @Column(value = "name")
        private String name;
    }

    @Test
    public void testInsertOrUpdateNoColumnDO() {
        boolean isThrow = false;
        try {
            NoColumnDO noColumnDO = new NoColumnDO();
            dbHelper.insertOrUpdate(noColumnDO);
        } catch (NoColumnAnnotationException e) {
            isThrow = true;
        }
        assert isThrow;

        isThrow = false;
        try {
            NoColumnDO noColumnDO = new NoColumnDO();
            dbHelper.insertOrUpdateWithNull(noColumnDO);
        } catch (NoColumnAnnotationException e) {
            isThrow = true;
        }
        assert isThrow;

    }

    @Data
    @Table("t_student")
    public static class NoColumnDO {
        // 什么列也没有
    }

    @Test
    public void testInsertOrUpdateList() {
        // 测试插入2个，修改3个的情况
        List<StudentDO> students = new ArrayList<>();

        StudentDO s1 = new StudentDO();
        s1.setName(uuidName());

        StudentDO s2 = new StudentDO();
        s2.setName(uuidName());

        students.add(s1);
        students.add(s2);

        List<StudentDO> studentDOS = CommonOps.insertBatch(dbHelper, 3);
        ListUtils.forEach(studentDOS, o -> o.setName(uuidName()));

        students.addAll(studentDOS);

        assert dbHelper.insertOrUpdate(students) == 5;
        assert dbHelper.getByKey(StudentDO.class, s1.getId()).getName().equals(s1.getName());
        assert dbHelper.getByKey(StudentDO.class, s2.getId()).getName().equals(s2.getName());
        ListUtils.forEach(studentDOS, o -> {
            assert dbHelper.getByKey(StudentDO.class, o.getId()).getName().equals(o.getName());
        });
    }

    @Test
    public void testInsertNull() {
        List<StudentDO> students = new ArrayList<>();
        students.add(null);

        assert dbHelper.insert(students) == 0;
        assert dbHelper.insertBatchWithoutReturnId(students) == 0;
    }

    // 测试一个没有key的do，此时用户自己指定了id
    @Data
    @Table("t_student")
    public static class StudentNoKeyDO {
        @Column("id") // 不要注解key
        private Long id;
        @Column("name")
        private String name;
    }

    @Test
    public void testInsertNoKey() {
        List<StudentNoKeyDO> studentDOS = new ArrayList<>();
        for (int i = 0; i < 2; i++) { // 这个实际上会有很小概率的偶发重复key，但是概率很低
            StudentNoKeyDO s = new StudentNoKeyDO();
            s.setId((long) -Math.abs(new Random().nextInt())); // 取负值，这样不会影响自增
            s.setName(UUID.randomUUID().toString().replace("-", ""));
            studentDOS.add(s);
        }

        assert dbHelper.insert(studentDOS) == 2;
    }
}
