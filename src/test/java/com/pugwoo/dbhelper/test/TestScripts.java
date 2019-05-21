package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentScriptDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestScripts {

    @Autowired
    private DBHelper dbHelper;

    @Test
    @Rollback(false)
    public void testInsertScript() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert studentDO.getName().equals(student2.getName());
        assert student2.getName().equals("111");
    }


    @Test
    @Rollback(false)
    public void testUpdateScript1() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        dbHelper.update(student2);

        StudentScriptDO student3 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert student3.getName().equals("222");
    }

    @Test
    @Rollback(false)
    public void testUpdateScript2() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        dbHelper.updateCustom(student2, "set age=?", 18);

        StudentScriptDO student3 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert student3.getName().equals("222");
    }


    @Test
    @Rollback(false)
    public void testUpdateScript3() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        dbHelper.updateAll(StudentScriptDO.class, "set age=?", "where id=?",18, studentDO.getId());

        StudentScriptDO student3 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert student3.getName().equals("222");
    }

}
