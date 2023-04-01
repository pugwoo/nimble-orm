package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 测试批量更新
 */
@SpringBootTest
public class Test2Update_Batch {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testUpdateBatch() {
        List<StudentDO> list = CommonOps.insertBatch(dbHelper, 11);

        for (StudentDO studentDO : list) {
            studentDO.setName(studentDO.getName() + "x");
        }

        int rows = dbHelper.update(list);
        assert rows == list.size();
    }
}
