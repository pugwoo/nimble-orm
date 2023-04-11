package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.CasVersionDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentHardDeleteDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class Test4Delete_HardDelete {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testHardDelete() {
        List<StudentDO> students = CommonOps.insertBatch(dbHelper, 3);

        int rows = dbHelper.deleteHard(students.get(1));
        assert rows == 1;

        // 用hardDO反查，应该查询不到
        StudentHardDeleteDO one = dbHelper.getOne(StudentHardDeleteDO.class, "where id=?", students.get(1).getId());
        assert null == one;

        // 删除2条记录
        students.remove(1);
        rows = dbHelper.deleteHard(students);
        assert rows == 2;

        one = dbHelper.getOne(StudentHardDeleteDO.class, "where id=?", students.get(0).getId());
        assert null == one;

        one = dbHelper.getOne(StudentHardDeleteDO.class, "where id=?", students.get(1).getId());
        assert null == one;

        // 前缀
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        students = CommonOps.insertBatch(dbHelper, 3, prefix);
        rows = dbHelper.deleteHard(StudentDO.class, "where name like ?", prefix + "%");
        assert rows == 3;

        one = dbHelper.getOne(StudentHardDeleteDO.class, "where id=?", students.get(0).getId());
        assert null == one;
        one = dbHelper.getOne(StudentHardDeleteDO.class, "where id=?", students.get(1).getId());
        assert null == one;
        one = dbHelper.getOne(StudentHardDeleteDO.class, "where id=?", students.get(2).getId());
        assert null == one;
    }

    @Test
    public void testHardDelete2() {
        List<CasVersionDO> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CasVersionDO d = new CasVersionDO();
            d.setName(getUuidName());
            dbHelper.insert(d);
            list.add(d);
        }

        int rows = dbHelper.deleteHard(list.get(1));
        assert rows == 1;

        assert dbHelper.getOne(CasVersionDO.class, "where id=?", list.get(1).getId()) == null;

        list.remove(1);
        rows = dbHelper.deleteHard(list);
        assert rows == 2;

        assert dbHelper.getOne(CasVersionDO.class, "where id=?", list.get(0).getId()) == null;
        assert dbHelper.getOne(CasVersionDO.class, "where id=?", list.get(1).getId()) == null;

        // 再进行一次
        list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CasVersionDO d = new CasVersionDO();
            d.setName(getUuidName());
            dbHelper.insert(d);
            list.add(d);
        }

        rows = dbHelper.delete(CasVersionDO.class, "where id in (?)", ListUtils.transform(list, d -> d.getId()));
        assert rows == 3;

        assert dbHelper.getOne(CasVersionDO.class, "where id=?", list.get(0).getId()) == null;
        assert dbHelper.getOne(CasVersionDO.class, "where id=?", list.get(1).getId()) == null;
        assert dbHelper.getOne(CasVersionDO.class, "where id=?", list.get(2).getId()) == null;
    }

    public String getUuidName() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

}
