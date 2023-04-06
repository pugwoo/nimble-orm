package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.IdableSoftDeleteBaseDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.lang.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 测试批量更新
 */
@SpringBootTest
public class Test2Update_Batch {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testUpdateBatch() {
        // 插入11条数据，然后再批量update
        List<StudentDO> list = CommonOps.insertBatch(dbHelper, 11);
        List<Long> ids = ListUtils.transform(list, IdableSoftDeleteBaseDO::getId);
        Map<Long, StudentDO> map = ListUtils.toMap(list, IdableSoftDeleteBaseDO::getId, o -> o);

        for (StudentDO studentDO : list) {
            studentDO.setName(studentDO.getName() + "x");
        }

        int rows = dbHelper.update(list);
        assert rows == list.size();

        List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        assert all.size() == list.size();
        for (StudentDO studentDO : all) { // 验证修改确实成功
            assert studentDO.getName().equals(map.get(studentDO.getId()).getName());
        }

        // 软删除2个，再批量update
        dbHelper.deleteByKey(list.get(3));
        dbHelper.deleteByKey(list.get(8));

        for (StudentDO studentDO : list) {
            studentDO.setName(studentDO.getName() + "y");
        }

        rows = dbHelper.update(list);
        assert rows == list.size() - 2; // 软删除了2个

        all = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        assert all.size() == list.size() - 2; // 软删除了2个
        for (StudentDO studentDO : all) { // 验证修改确实成功
            assert studentDO.getName().equals(map.get(studentDO.getId()).getName());
        }
    }

    // 批量update有null值的情况，看看数据库的值会不会被该，期望是不该，保留原值
    @Test
    public void testBatchUpdateWithNullValue() {
        List<StudentDO> list = CommonOps.insertBatch(dbHelper, 11);

        try {
            Thread.sleep(1200); // 故意睡1.2秒，让create time和update time不一样
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ListUtils.forEach(list, o -> {
            o.setSchoolId(o.getId());
            o.setName(o.getName() + "x"); // 验证至少修改2个字段
        });
        assert dbHelper.update(list) == 11;

        List<Long> ids = ListUtils.transform(list, IdableSoftDeleteBaseDO::getId);

        // 验证是否修改成功
        List<StudentDO> students = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        for (StudentDO student : students) {
            assert Objects.equals(student.getSchoolId(), student.getId());
            assert DateUtils.format(student.getUpdateTime()).compareTo(DateUtils.format(student.getCreateTime())) > 0;
        }

        // 将前9条设置为null，再更新
        for (int i = 0; i < 9; i++) {
            students.get(i).setSchoolId(null);
        }
        for (int i = 0; i < 11; i++) {
            students.get(i).setName(students.get(i).getName() + "y");
        }

        assert dbHelper.update(students) == 11; // 因为有update time的值，所以有更新

        // 重新查回来，验证null值实际上没有被修改
        students = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        for (StudentDO student : students) {
            assert Objects.equals(student.getSchoolId(), student.getId()); // null值不会被修改
            assert student.getName().endsWith("xy"); // 名字则被修改了
        }
    }


    // 批量update有casVersion的情况

    // 批量update有casVersion，且update有null值的情况

    // 批量update casVersion部分不匹配的情况，看看是否修改无变化

    // 批量update casVersion全部不匹配的情况，看看是否修改无变化

}