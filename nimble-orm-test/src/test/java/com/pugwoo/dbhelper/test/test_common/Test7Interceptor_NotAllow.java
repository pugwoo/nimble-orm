package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Test7Interceptor_NotAllow {

    @Autowired
    @Qualifier("dbHelperWithNotAllowInterceptor")
    private DBHelper dbHelper;

    @Test
    public void testQuery() {
        boolean isThrow = false;
        try {
            List<StudentDO> all = dbHelper.getAll(StudentDO.class);
        } catch (NotAllowQueryException e) {
            isThrow = true;
        }
        assert isThrow;
    }

}
