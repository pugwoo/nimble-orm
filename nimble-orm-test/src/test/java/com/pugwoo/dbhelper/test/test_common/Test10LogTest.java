package com.pugwoo.dbhelper.test.test_common;


import static com.pugwoo.dbhelper.utils.DOInfoReader.getAllPublicMethods;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.impl.part.P0_JdbcTemplateOp;
import com.pugwoo.dbhelper.impl.part.P5_DeleteOp;
import com.pugwoo.dbhelper.test.entity.StudentScriptDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.swing.Spring;
import javax.xml.crypto.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StopWatch;

public class Test10LogTest {


    @Test
    public void testGetAllPublicFunction() {
        Set<Method> allPublicFunction = getAllPublicMethods(SpringJdbcDBHelper.class);
        allPublicFunction.forEach(System.out::println);
        System.out.println("com.pugwoo.dbhelper.test.test_common.Test10LogTest.testGetAllPublicFunction(Test10LogTest.java:27)\n");
        System.out.println(allPublicFunction.size());
        assert allPublicFunction.size() == 76;
    }

    @Test
    public void testLogCallLine() {
        SpringJdbcDBHelper test = new SpringJdbcDBHelper();
        testNotInSpringJdbcDBHelperMethod(test);
    }

    void testNotInSpringJdbcDBHelperMethod(SpringJdbcDBHelper test){
        // 测试修改成 public
//        test.getFirstCallMethodStr();
    }

    @Test
    public void testLogCallCost() {
        StopWatch stopWatch = new StopWatch();
        SpringJdbcDBHelper dbHelper = new SpringJdbcDBHelper();
        int cnt = 1_000_000;

        stopWatch.start(String.format("调用%d次 getFirstCallMethodStr",cnt));
        for (int i = 0; i < cnt; i++) {
            // test
//            dbHelper.getFirstCallMethodStr();
        }
        stopWatch.stop();
        System.out.println("运行时间（毫秒）: " + stopWatch.getTotalTimeMillis());
        stopWatch.start(String.format("调用%d次 new Date()",cnt));
        for (int i = 0; i < cnt; i++) {
            new Date();
        }
        stopWatch.stop();
        System.out.println("运行时间（毫秒）: " + stopWatch.getTotalTimeMillis());
    }


}
