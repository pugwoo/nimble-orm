package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.exception.CasVersionNotMatchException;
import com.pugwoo.dbhelper.test.entity.CasVersionDO;
import com.pugwoo.dbhelper.test.entity.IdableSoftDeleteBaseDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

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

        // update后，再查询一次，看看数据是否正确
        List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        assert all.size() == list.size();
        for (StudentDO studentDO : all) {
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

        // update后，再查询一次，看看数据是否正确
        all = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        assert all.size() == list.size() - 2;
        for (StudentDO studentDO : all) {
            assert studentDO.getName().equals(map.get(studentDO.getId()).getName());
        }
    }

    // 批量update有null值的情况，看看数据库的值会不会被改，期望是不改，保留原值
    @Test
    public void testBatchUpdateWithNullValue() {
        List<StudentDO> list = CommonOps.insertBatch(dbHelper, 11);
        ListUtils.forEach(list, o -> {
            o.setSchoolId(o.getId());
            o.setName(o.getName() + "x"); // 验证至少修改2个字段
        });
        assert dbHelper.update(list) == 11;

        Map<Long, StudentDO> map = ListUtils.toMap(list, IdableSoftDeleteBaseDO::getId, o -> o);
        List<Long> ids = ListUtils.transform(list, IdableSoftDeleteBaseDO::getId);

        // 验证是否修改成功
        List<StudentDO> students = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        for (StudentDO student : students) {
            assert Objects.equals(student.getSchoolId(), student.getId());
            assert student.getName().equals(map.get(student.getId()).getName());
        }

        // 将前9条设置为null，再更新
        for (int i = 0; i < 9; i++) {
            students.get(i).setSchoolId(null);
        }
        for (int i = 0; i < 11; i++) {
            students.get(i).setName(students.get(i).getName() + "y");
        }

        map = ListUtils.toMap(students, IdableSoftDeleteBaseDO::getId, o -> o);

        assert dbHelper.update(students) == 11; // 因为有update time的值，所以有更新

        // 重新查回来，验证null值实际上没有被修改
        students = dbHelper.getAll(StudentDO.class, "where id in (?)", ids);
        for (StudentDO student : students) {
            assert Objects.equals(student.getSchoolId(), student.getId()); // null值不会被修改
            assert student.getName().endsWith("xy"); // 名字则被修改了
            assert student.getName().equals(map.get(student.getId()).getName());
        }
    }

    // 批量update有casVersion的情况
    @Test
    public void testCasVersionUpdate() {
        String name = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        List<CasVersionDO> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            CasVersionDO cas = new CasVersionDO();
            cas.setName(name);
            list.add(cas);
        }

        int rows = dbHelper.insertBatchWithoutReturnId(list);
        assert rows == 7;
        for (CasVersionDO cas : list) {
            assert cas.getVersion() == 1;
        }

        // 查询回来
        list = dbHelper.getAll(CasVersionDO.class, "where name=?", name);
        assert list.size() == 7;
        for (CasVersionDO cas : list) {
            assert cas.getVersion() == 1;
        }

        // 再修改一次
        for (CasVersionDO cas : list) {
            cas.setName(cas.getName() + "x");
        }
        assert dbHelper.update(list) == 7;
        for (CasVersionDO cas : list) {
            assert cas.getVersion() == 2;
        }

        // 再查询回来
        list = dbHelper.getAll(CasVersionDO.class, "where name=?", name + "x");
        assert list.size() == 7;
        for (CasVersionDO cas : list) {
            assert cas.getVersion() == 2;
            assert (name + "x").equals(cas.getName());
        }

        // 故意改错2条casversion
        list.get(1).setVersion(1);
        list.get(4).setVersion(4);
        for (CasVersionDO cas : list) {
            cas.setName(cas.getName() + "y");
        }

        boolean isThrow = false;
        try {
            dbHelper.update(list);
        } catch (CasVersionNotMatchException e) {
            isThrow = true;
            assert e.getAffectedRows() == 7 - 2; // 2条casversion不匹配，所以只有5条被修改
            // 此时业务方可以选择回滚，这里并不回滚
        }
        assert isThrow;

        // 查询回来验证
        list = dbHelper.getAll(CasVersionDO.class, "where name=?", name + "x");
        assert list.size() == 2;

        list = dbHelper.getAll(CasVersionDO.class, "where name=?", name + "xy");
        assert list.size() == 5;
    }

    // 批量update有casVersion，且update有null值的情况

    // 批量update casVersion部分不匹配的情况，看看是否修改无变化

    // 批量update casVersion全部不匹配的情况，看看是否修改无变化

}
