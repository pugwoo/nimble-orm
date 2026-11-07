package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 测试postSql末尾带行锁子句（如for update）时的查询。
 * 说明：MySQL/PostgreSQL要求行锁子句位于order by/limit之后、整条SQL的最后，
 * 而getOne/getPage等方法会由orm内部追加order by/limit，因此行锁子句需要由orm摘出后重新拼到最后；
 * 计数查询则应移除行锁子句。ClickHouse不支持行锁子句，故该测试只在MySQL/PostgreSQL下运行。
 */
public abstract class Test1Query_ForUpdate {

    public abstract DBHelper getDBHelper();

    /** 纯字符串层面的行锁子句提取/移除测试，不依赖数据库执行 */
    @Test
    public void testLockingClauseUtils() {
        // 常见的for update
        assert SQLUtils.extractLockingClause("where id=? for update").equals("for update");
        assert SQLUtils.removeLockingClause("where id=? for update").equals("where id=?");

        // 用户自带order by，行锁子句在order by之后
        assert SQLUtils.extractLockingClause("where id=? order by id FOR UPDATE NOWAIT").equals("FOR UPDATE NOWAIT");
        assert SQLUtils.removeLockingClause("where id=? order by id FOR UPDATE NOWAIT").equals("where id=? order by id");

        // skip locked、of子句
        assert SQLUtils.extractLockingClause("where id=? for update skip locked").equals("for update skip locked");
        assert SQLUtils.extractLockingClause("where id=? for update of t nowait").equals("for update of t nowait");
        assert SQLUtils.removeLockingClause("where id=? for update of t nowait").equals("where id=?");

        // 其它行锁写法
        assert SQLUtils.extractLockingClause("where id=? for share").equals("for share");
        assert SQLUtils.extractLockingClause("where id=? for no key update").equals("for no key update");
        assert SQLUtils.extractLockingClause("where id=? for key share").equals("for key share");
        assert SQLUtils.extractLockingClause("where id=? lock in share mode").equals("lock in share mode");

        // 末尾带分号
        assert SQLUtils.extractLockingClause("where id=? for update;").equals("for update");
        assert SQLUtils.removeLockingClause("where id=? for update;").equals("where id=?");

        // 没有行锁子句时，提取为空、移除时原样返回
        assert SQLUtils.extractLockingClause("where name like ?").equals("");
        assert SQLUtils.removeLockingClause("where name like ?").equals("where name like ?");
        assert SQLUtils.extractLockingClause("where update_time > ?").equals("");
        assert SQLUtils.removeLockingClause("where update_time > ?").equals("where update_time > ?");

        // 行锁字样出现在字符串字面量中时不能误判
        assert SQLUtils.extractLockingClause("where name='for update'").equals("");
        assert SQLUtils.removeLockingClause("where name='for update'").equals("where name='for update'");

        // 空值处理
        assert SQLUtils.extractLockingClause(null).equals("");
        assert SQLUtils.extractLockingClause("").equals("");
        assert SQLUtils.removeLockingClause(null) == null;
        assert SQLUtils.removeLockingClause("").equals("");
    }

    @Test
    public void testGetOneForUpdate() {
        StudentDO studentDO = CommonOps.insertOne(getDBHelper());

        // 修复前会拼出 "... FOR UPDATE ORDER BY `id` LIMIT 1 OFFSET 0" 而报SQL语法错误
        StudentDO one = getDBHelper().getOne(StudentDO.class, "where id=? for update", studentDO.getId());
        assert one != null;
        assert one.getId().equals(studentDO.getId());
        assert one.getName().equals(studentDO.getName());

        // 用户自带order by时，行锁子句同样要排到limit之后
        one = getDBHelper().getOne(StudentDO.class, "where id=? order by id desc for update", studentDO.getId());
        assert one != null;
        assert one.getId().equals(studentDO.getId());

        // isExist内部走getOne，同样覆盖
        assert getDBHelper().isExist(StudentDO.class, "where id=? for update", studentDO.getId());
        assert !getDBHelper().isExist(StudentDO.class, "where id=? for update", -1L);

        // 查不到记录时返回null，不应抛异常
        assert getDBHelper().getOne(StudentDO.class, "where id=? for update", -1L) == null;
    }

    @Test
    public void testGetPageForUpdate() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(getDBHelper(), 20, prefix);

        // total的计算会走getCount把postSql包进count子查询，子查询中不能带for update，这里一并验证
        PageData<StudentDO> page = getDBHelper().getPage(StudentDO.class, 1, 10,
                "where name like ? for update", prefix + "%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 20;

        PageData<StudentDO> page2 = getDBHelper().getPage(StudentDO.class, 2, 10,
                "where name like ? for update", prefix + "%");
        assert page2.getData().size() == 10;
        assert page2.getTotal() == 20;

        Set<String> names = new HashSet<>();
        for (StudentDO stu : page.getData()) {
            names.add(stu.getName());
        }
        for (StudentDO stu : page2.getData()) {
            names.add(stu.getName());
        }
        assert names.size() == 20;

        // 不查总数的分页
        PageData<StudentDO> pageWithoutCount = getDBHelper().getPageWithoutCount(StudentDO.class, 1, 10,
                "where name like ? for update", prefix + "%");
        assert pageWithoutCount.getData().size() == 10;
    }

    @Test
    public void testGetCountForUpdate() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(getDBHelper(), 5, prefix);

        // 直接计数时，for update应被移除（计数子查询中不允许出现行锁子句）
        long count = getDBHelper().getCount(StudentDO.class, "where name like ? for update", prefix + "%");
        assert count == 5;

        count = getDBHelper().getCount(StudentDO.class, "where name like ?", prefix + "%");
        assert count == 5;
    }

    @Test
    public void testGetAllForUpdate() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(getDBHelper(), 3, prefix);

        // getAll不带limit，但带order by时行锁子句也要排在order by之后
        List<StudentDO> all = getDBHelper().getAll(StudentDO.class,
                "where name like ? order by id for update", prefix + "%");
        assert all.size() == 3;
        for (StudentDO stu : all) {
            assert stu.getName().startsWith(prefix);
        }

        // stream方式同样覆盖
        try (Stream<StudentDO> stream = getDBHelper().getAllForStream(StudentDO.class,
                "where name like ? order by id for update", prefix + "%")) {
            List<StudentDO> list = stream.collect(Collectors.toList());
            assert list.size() == 3;
        }
    }
}
