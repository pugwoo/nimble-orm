package com.pugwoo.dbhelper.test.test_clickhouse;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.test.test_clickhouse.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestQuery {

    @Autowired @Qualifier("clickhouseDbHelper")
    private DBHelper dbHelper;

    @Test
    public void testGetRaw() {

        dbHelper.executeRaw("truncate table t_student");
        CommonOps.insertBatchWithRandomId(dbHelper, 100, "nick");

        PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10);

        assert page.getData().size() == 10;
        assert page.getTotal() >= 100;

        for (StudentDO student : page.getData()) {
            assert student.getId() != null;
            assert student.getName().startsWith("nick");
        }
    }

}
