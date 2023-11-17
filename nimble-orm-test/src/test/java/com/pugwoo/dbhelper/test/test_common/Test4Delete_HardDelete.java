package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.test.entity.CasVersionDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentHardDeleteDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class Test4Delete_HardDelete {

    public abstract DBHelper getDBHelper();

    @Test
    public void testHardDelete() {
        List<StudentDO> students = CommonOps.insertBatch(getDBHelper(), 3);

        int rows = getDBHelper().deleteHard(students.get(1));
        assert rows == 1;

        // 用hardDO反查，应该查询不到
        StudentHardDeleteDO one = getDBHelper().getOne(StudentHardDeleteDO.class, "where id=?", students.get(1).getId());
        assert null == one;

        // 删除2条记录
        students.remove(1);
        rows = getDBHelper().deleteHard(students);
        // clickhouse没有办法返回真实的删除条数
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 1;
        } else {
            assert rows == 2;
        }

        one = getDBHelper().getOne(StudentHardDeleteDO.class, "where id=?", students.get(0).getId());
        assert null == one;

        one = getDBHelper().getOne(StudentHardDeleteDO.class, "where id=?", students.get(1).getId());
        assert null == one;

        // 前缀
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        students = CommonOps.insertBatch(getDBHelper(), 3, prefix);
        rows = getDBHelper().deleteHard(StudentDO.class, "where name like ?", prefix + "%");
        // clickhouse没有办法返回真实的删除条数
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 1;
        } else {
            assert rows == 3;
        }

        one = getDBHelper().getOne(StudentHardDeleteDO.class, "where id=?", students.get(0).getId());
        assert null == one;
        one = getDBHelper().getOne(StudentHardDeleteDO.class, "where id=?", students.get(1).getId());
        assert null == one;
        one = getDBHelper().getOne(StudentHardDeleteDO.class, "where id=?", students.get(2).getId());
        assert null == one;
    }

    @Test
    public void testHardDelete2() {
        List<CasVersionDO> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CasVersionDO d = new CasVersionDO();
            d.setName(getUuidName());
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                d.setId(CommonOps.getRandomInt());
            }
            getDBHelper().insert(d);
            list.add(d);
        }

        int rows = getDBHelper().deleteHard(list.get(1));
        assert rows == 1;

        assert getDBHelper().getOne(CasVersionDO.class, "where id=?", list.get(1).getId()) == null;

        list.remove(1);
        rows = getDBHelper().deleteHard(list);
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 1;
        } else {
            assert rows == 2;
        }

        assert getDBHelper().getOne(CasVersionDO.class, "where id=?", list.get(0).getId()) == null;
        assert getDBHelper().getOne(CasVersionDO.class, "where id=?", list.get(1).getId()) == null;

        // 再进行一次
        list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CasVersionDO d = new CasVersionDO();
            d.setName(getUuidName());
            if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
                d.setId(CommonOps.getRandomInt());
            }
            getDBHelper().insert(d);
            list.add(d);
        }

        rows = getDBHelper().delete(CasVersionDO.class, "where id in (?)", ListUtils.transform(list, d -> d.getId()));
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            assert rows == 1;
        } else {
            assert rows == 3;
        }

        assert getDBHelper().getOne(CasVersionDO.class, "where id=?", list.get(0).getId()) == null;
        assert getDBHelper().getOne(CasVersionDO.class, "where id=?", list.get(1).getId()) == null;
        assert getDBHelper().getOne(CasVersionDO.class, "where id=?", list.get(2).getId()) == null;
    }

    public String getUuidName() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

}
