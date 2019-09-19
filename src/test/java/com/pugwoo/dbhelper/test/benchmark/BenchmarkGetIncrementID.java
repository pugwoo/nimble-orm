package com.pugwoo.dbhelper.test.benchmark;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BenchmarkGetIncrementID {

    @Autowired
    private DBHelper dbHelper;

    @Rollback(false)
    @Test
    public void bench() throws Exception {

        final AtomicInteger succQuery = new AtomicInteger(0);
        final AtomicInteger failQuery = new AtomicInteger(0);

        for(int i = 0; i < 200; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        StudentDO studentDO = new StudentDO();
                        studentDO.setName(UUID.randomUUID().toString());

                        int row = dbHelper.insert(studentDO);
                        if(row > 0 && studentDO.getId() != null) {
                            succQuery.incrementAndGet();
                        } else {
                            failQuery.incrementAndGet();
                        }
                    }
                }
            }).start();
        }

        Thread.sleep(60000 * 30);
        System.out.println("total query:" + succQuery.get());
        System.out.println("fail query:" + failQuery.get());
    }
}
