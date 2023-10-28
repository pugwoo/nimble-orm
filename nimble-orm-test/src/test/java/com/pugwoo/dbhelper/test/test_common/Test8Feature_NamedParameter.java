package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.benchmark.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public abstract class Test8Feature_NamedParameter {

    public abstract DBHelper getDBHelper();

    /**
     * 测试sql中带注释的情况，预期可以被正常处理，主要是处理sql中的?，替换成:paramN
     */
    @Test
    public void testSqlWithComment() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(getDBHelper(), 10, prefix);

        List<StudentDO> all = getDBHelper().getRaw(StudentDO.class,
                "/* ? */ -- a ? a \n select * from t_student where /* ? ? ? */ name /*???*/  -- ?? ?? \n like ?",
                prefix + "%");
        assert all.size() == 10;
    }

}
