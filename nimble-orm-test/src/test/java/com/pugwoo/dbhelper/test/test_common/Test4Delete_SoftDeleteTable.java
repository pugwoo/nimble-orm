package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Test4Delete_SoftDeleteTable {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testDelete() {
        List<StudentDO> studentDOS = CommonOps.insertBatch(dbHelper, 1);

        // 设置完整的字段

        dbHelper.delete(studentDOS.get(0));

        // 测试数据已经被删除了

        // 从另外一张表里查出数据，再比较

    }

}
