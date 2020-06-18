package com.pugwoo.dbhelper.test.benchmark;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
// 这里不能用@Transactional
public class BenchmarkCount {

    @Autowired
    private DBHelper dbHelper;

    /**
     * 测试getPage计算总数是否有串的情况
     * 测试数据库是mysql 5.7和8.0 innodb
     *
     * tomcat-jdbc + mysql-connector-java 8.0.x 5.1.x 均没有问题
     * HiKariCP + mysql-connector-java 8.0.x 5.1.x 均没有问题
     *
     * @throws Exception
     */
    @Test
    public void bench() throws Exception {
        final AtomicInteger totalQuery = new AtomicInteger(0);
        final AtomicInteger failQuery = new AtomicInteger(0);

        // insert 72
        final String name72 = UUID.randomUUID().toString().replace("-", "");
        for(int i = 0; i < 72; i++) {
            StudentDO studentDO = new StudentDO();
            studentDO.setName(name72);
            assert dbHelper.insert(studentDO) == 1;
        }

        // insert 76
        final String name76 = UUID.randomUUID().toString().replace("-", "");
        for(int i = 0; i < 76; i++) {
            StudentDO studentDO = new StudentDO();
            studentDO.setName(name76);
            assert dbHelper.insert(studentDO) == 1;
        }

        for(int i = 0; i < 200; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10,
                                "where name=?", name72);
                        if(page.getTotal() == 72) {
                            totalQuery.addAndGet(1);
                        } else {
                            failQuery.addAndGet(1);
                        }
                    }
                }
            }).start();
        }

        for(int i = 0; i < 200; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10,
                                "where name=?", name76);
                        if(page.getTotal() == 76) {
                            totalQuery.addAndGet(1);
                        } else {
                            failQuery.addAndGet(1);
                        }
                    }
                }
            }).start();
        }


        Thread.sleep(120000);
        System.out.println("total query:" + totalQuery.get());
        System.out.println("fail query:" + failQuery.get());

        assert totalQuery.get() > 0;
        assert failQuery.get() == 0;
    }


}
