package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentSelfTrueDeleteJoinVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

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



}
