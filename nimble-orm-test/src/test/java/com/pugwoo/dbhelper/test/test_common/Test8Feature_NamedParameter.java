package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.test_clickhouse.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class Test8Feature_NamedParameter {

    @Autowired
    private DBHelper dbHelper;

    /**
     * 测试sql中带注释的情况，预期可以被正常处理，主要是处理sql中的?，替换成:paramN
     */
    @Test
    public void testSqlWithComment() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(dbHelper, 10, prefix);

        List<StudentDO> all = dbHelper.getAll(StudentDO.class,
                "where /* ? ? ? */ name /*???*/  -- ?? ?? \n like ?", prefix + "%");
        assert all.size() == 10;
    }

}
