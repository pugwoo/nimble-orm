package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.IDBHelperSlowSqlCallback;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentSchoolJoinVO;
import com.pugwoo.dbhelper.test.vo.StudentSelfTrueDeleteJoinVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试读操作相关
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestDBHelper_query {

    @Autowired
    private DBHelper dbHelper;

    @Test @Rollback(false)
    public void testCount() {

        dbHelper.delete(StudentDO.class, "where 1=1");
        int count = dbHelper.getCount(StudentDO.class);
        assert count == 0;

        dbHelper.delete(SchoolDO.class, "where 1=1");
        count = dbHelper.getCount(SchoolDO.class);
        assert count == 0;

        List<StudentDO> studentDOS = CommonOps.insertBatch(dbHelper, 99);

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("sysu");
        dbHelper.insert(schoolDO);
        assert schoolDO.getId() != null;

        for(StudentDO studentDO : studentDOS) {
            studentDO.setSchoolId(schoolDO.getId());
            dbHelper.update(studentDO);
        }

        count = dbHelper.getCount(StudentDO.class);
        assert count == 99;
        count = dbHelper.getCount(StudentDO.class, "where 1=1");
        assert count == 99;
        count = dbHelper.getCount(StudentDO.class, "where name like ?", "nick%");
        assert count == 99;
        count = dbHelper.getCount(StudentDO.class, "where name like ? group by name", "nick%");
        assert count == 99;

        count = dbHelper.getCount(StudentDO.class, "where name not like ? group by name", "nick%");
        assert count == 0;

        List<String> names = new ArrayList<String>();
        names.add(studentDOS.get(0).getName());
        names.add(studentDOS.get(10).getName());
        names.add(studentDOS.get(30).getName());
        count = dbHelper.getCount(StudentDO.class, "where name in (?)",names);
        assert count == 3;

        PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10);
        assert page.getData().size() == 10;
        assert page.getTotal() == 99;

        page = dbHelper.getPage(StudentDO.class, 1, 10, "where name like ?", "nick%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 99;

        page = dbHelper.getPage(StudentDO.class, 1, 10, "where name not like ?", "nick%");
        assert page.getData().size() == 0;
        assert page.getTotal() == 0;

        page = dbHelper.getPage(StudentDO.class, 1, 10, "where name in (?)", names);
        assert page.getData().size() == 3;
        assert page.getTotal() == 3;

        page = dbHelper.getPage(StudentDO.class, 1, 2, "where name in (?)", names);
        assert page.getData().size() == 2;
        assert page.getTotal() == 3;

        page = dbHelper.getPage(StudentDO.class, 1, 2, "where name in (?) group by name", names);
        assert page.getData().size() == 2;
        assert page.getTotal() == 3;


        page = dbHelper.getPage(StudentDO.class, 1, 100);
        assert page.getData().size() == 99;
        assert page.getTotal() == 99;

        count = dbHelper.getCount(StudentSchoolJoinVO.class);
        assert count == 99;
        count = dbHelper.getCount(StudentSchoolJoinVO.class, "where 1=1");
        assert count == 99;
        count = dbHelper.getCount(StudentSchoolJoinVO.class, "where t1.name like ?", "nick%");
        assert count == 99;
        count = dbHelper.getCount(StudentSchoolJoinVO.class, "where t1.name like ? group by t1.name", "nick%");
        assert count == 99;

        PageData<StudentSchoolJoinVO> page2 = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 10);
        assert page2.getData().size() == 10;
        assert page2.getTotal() == 99;

        page2 = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 100);
        assert page2.getData().size() == 99;
        assert page2.getTotal() == 99;

    }

    /**测试软删除DO查询条件中涉及到OR条件的情况*/
    @Test @Rollback(false)
    public void testQueryWithDeletedAndOr() {
        // 先清表
        dbHelper.delete(StudentDO.class, "where 1=1");

        CommonOps.insertBatch(dbHelper, 10);
        dbHelper.delete(StudentDO.class, "where 1=1"); // 确保至少有10条删除记录

        CommonOps.insertBatch(dbHelper, 10);
        List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where 1=1 or 1=1"); // 重点
        assert all.size() == 10; // 只应该查出10条记录，而不是20条以上的记录
        for(StudentDO studentDO : all) {
            assert !studentDO.getDeleted();
        }

        all = dbHelper.getAll(StudentDO.class, "where 1=1 and 1=1 or 1=1 or 1=1");
        assert all.size() == 10;
    }

    /**测试join真删除的类*/
    @Test @Rollback(false)
    public void testJoinTrueDelete() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        StudentSelfTrueDeleteJoinVO joinVO = dbHelper.getOne(StudentSelfTrueDeleteJoinVO.class, "where t1.id=?", studentDO.getId());
        assert joinVO.getStudent1().getId().equals(studentDO.getId());
        assert joinVO.getStudent2().getId().equals(studentDO.getId());
    }

    /**测试慢速记录*/
    @Test @Rollback(false)
    public void testSlowLog() {
        final StringBuilder sb = new StringBuilder();

        dbHelper.setTimeoutWarningValve(1);
        dbHelper.setTimeoutWarningCallback(new IDBHelperSlowSqlCallback() {
            @Override
            public void callback(long executeMsTime, String sql, List<Object> args) {
                System.out.println("==in slow callback== execMs:" + executeMsTime + "ms,"
                    + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args));
                sb.append(sql);
            }
        });

        CommonOps.insertOne(dbHelper);
        assert !sb.toString().isEmpty();

        dbHelper.setTimeoutWarningValve(1000);
    }

    /**测试分页最大数限制*/
    @Test @Rollback(false)
    public void testMaxPageSize() {
        dbHelper.setMaxPageSize(5);

        CommonOps.insertBatch(dbHelper, 10);
        PageData<StudentDO> pageData = dbHelper.getPage(StudentDO.class, 1, 10);
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        pageData = dbHelper.getPageWithoutCount(StudentDO.class, 1, 10);
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        pageData = dbHelper.getPageWithoutCount(StudentDO.class, 1, 10, "where 1=1");
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        dbHelper.setMaxPageSize(1000000);
    }

    /**事务相关测试*/
    @Transactional
    @Test @Rollback(false)
    public void testTransaction() throws InterruptedException {
        final StudentDO studentDO1 = CommonOps.insertOne(dbHelper);
        final StudentDO studentDO2 = CommonOps.insertOne(dbHelper);

        System.out.println("insert ok, id1:" + studentDO1.getId() +
                ",id2:" + studentDO2.getId());

        dbHelper.executeAfterCommit(new Runnable() {
            @Override
            public void run() {
                System.out.println("transaction commit, student1:" + studentDO1.getId()
                        + ",student2:" + studentDO2.getId());
            }
        });

        System.out.println("myTrans end");
//		dbHelper.rollback(); // org.springframework.transaction.NoTransactionException
        // throw new RuntimeException(); // 抛出异常也无法让事务回滚
        // 原因：https://stackoverflow.com/questions/13525106/transactions-doesnt-work-in-junit
        // 这意味着rollback()这个方法在单元测试中没法测
    }

    @Transactional
    @Test @Rollback(false)
    public void testGetRaw() {
        final StudentDO studentDO1 = CommonOps.insertOne(dbHelper);
        final StudentDO studentDO2 = CommonOps.insertOne(dbHelper);

        List<StudentDO> list = dbHelper.getRaw(StudentDO.class, "select id,name from t_student where name=?", studentDO1.getName());

        assert list.size() == 1;
        assert list.get(0).getName().equals(studentDO1.getName());

    }

}
