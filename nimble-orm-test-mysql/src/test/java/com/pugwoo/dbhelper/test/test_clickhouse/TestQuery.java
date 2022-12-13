package com.pugwoo.dbhelper.test.test_clickhouse;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.test.test_clickhouse.entity.StudentDO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestQuery {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testGetRaw() {

        CkCommonOps.insertSome(dbHelper, 100);

        PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10);

        assert page.getData().size() == 10;
        assert page.getTotal() >= 100;

        for (StudentDO student : page.getData()) {
            assert student.getId() != null;
            assert student.getName().startsWith("nick");
        }
    }

}
