package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.MustProvideConstructorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 测试各种异常的情况
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestOtherExceptions {

    @Autowired
    private DBHelper dbHelper;

    @Test
    
    public void deleteEx() {
        boolean ex = false;
        try {
            dbHelper.deleteByKey(StudentDO.class, 1L);
        } catch (Exception e) {
            assert e instanceof MustProvideConstructorException;
            ex = true;
        }
        assert ex;

        ex = false;
        try {
            dbHelper.deleteByKey(StudentDO2.class, 1L);
        } catch (Exception e) {
            assert e instanceof MustProvideConstructorException;
            ex = true;
        }
        assert ex;
    }

    @Table("t_student")
    private static class StudentDO {

        public StudentDO(Long id) { // 故意让这个DO类没有默认构造函数
            this.id = id;
        }

        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    @Table("t_student")
    private static class StudentDO2 {

        private StudentDO2() { // 故意让这个DO类默认构造函数是私有的
            this.id = id;
        }

        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

}
