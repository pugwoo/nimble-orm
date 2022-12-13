package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.ScriptErrorException;
import com.pugwoo.dbhelper.test.entity.StudentScriptDO;
import com.pugwoo.dbhelper.utils.ScriptUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestScripts {

    @Autowired
    private DBHelper dbHelper;

    @Test
    
    public void testInsertScript() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert studentDO.getName().equals(student2.getName());
        assert student2.getName().equals("111");
    }


    @Test
    
    public void testUpdateScript1() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        dbHelper.update(student2);

        StudentScriptDO student3 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert student3.getName().equals("222");
    }

    @Test
    
    public void testUpdateScript2() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        dbHelper.updateCustom(student2, "set age=?", 18);

        StudentScriptDO student3 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert student3.getName().equals("222");
    }


    @Test
    
    public void testUpdateScript3() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        StudentScriptDO student2 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        dbHelper.updateAll(StudentScriptDO.class, "set age=?", "where id=?",18, studentDO.getId());

        StudentScriptDO student3 = dbHelper.getByKey(StudentScriptDO.class, studentDO.getId());
        assert student3.getName().equals("222");
    }

    @Test
    
    public void testDeleteScript() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        dbHelper.deleteByKey(studentDO);

        StudentRawDO student2 = dbHelper.getByKey(StudentRawDO.class, studentDO.getId());
        assert student2.getName().equals("333");
    }

    @Test
    
    public void testDeleteScript2() {
        StudentScriptDO studentDO = new StudentScriptDO();
        dbHelper.insert(studentDO);

        dbHelper.delete(StudentScriptDO.class, "where id=?", studentDO.getId());

        StudentRawDO student2 = dbHelper.getByKey(StudentRawDO.class, studentDO.getId());
        assert student2.getName().equals("333");
    }

    @Test
    
    public void testWrongScript() {
        StudentTestScriptErrorDO student = new StudentTestScriptErrorDO();

        int i = 0;
        try {
            dbHelper.insert(student);
        } catch (Exception e) {
            assert e instanceof ScriptErrorException;
            i = 1;
        }
        assert i == 1;


        Object value = ScriptUtils.getValueFromScript(new StudentScriptDO(), true, "xxxx");
        assert value == null;

        i = 0;
        try {
            ScriptUtils.getValueFromScript(new StudentScriptDO(), false, "xxxx");
        } catch (Exception e) {
            assert e instanceof ScriptErrorException;
            i = 1;
        }
        assert i == 1;

        value = ScriptUtils.getValueFromScript(true, "xxxx");
        assert value == null;

        i = 0;
        try {
            ScriptUtils.getValueFromScript(false, "xxxx");
        } catch (Exception e) {
            assert e instanceof ScriptErrorException;
            i = 1;
        }
        assert i == 1;
    }


    @Table("t_student")
    public static class StudentRawDO {

        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;

        @Column(value = "name")
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Table("t_student")
    public static class StudentTestScriptErrorDO {

        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;

        @Column(value = "name", insertValueScript = "1++++", ignoreScriptError = false) // 故意弄错的脚本
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }




}
