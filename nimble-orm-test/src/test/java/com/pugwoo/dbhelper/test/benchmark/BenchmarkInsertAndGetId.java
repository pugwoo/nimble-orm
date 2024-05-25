package com.pugwoo.dbhelper.test.benchmark;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.wooutils.thread.ThreadPoolUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
// 这里不能用@Transactional，就是要测没有事务情况下的
public class BenchmarkInsertAndGetId {

    @Autowired
    private DBHelper dbHelper;

    /**
     * 该测试用于测试获取自增ID的正确性。
     * 测试方式：起400个并发线程，不停地插入1万条数据并获取自增id，通过自增id再去查回数据比较，从而校验其正确性。
     * 结论：400万个均正确。
     */
    @Test
    @EnabledIfSystemProperty(named = "runBenchmarkTest", matches = "true")
    public void benchmark() {

        final AtomicLong insert = new AtomicLong(0);
        final AtomicLong insertFail = new AtomicLong(0);
        final AtomicLong getIdFail = new AtomicLong(0);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    System.out.println("insert:" + insert.get() + ",insertFail:" + insertFail.get()
                            + ",getIdFail:" + getIdFail.get());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();

        // 起400个线程
        ThreadPoolExecutor executeThem = ThreadPoolUtils.createThreadPool(400, 100000, 400, "benchmark");

        List<Future<String>> futureList = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            futureList.add(executeThem.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        StudentDO studentDO = new StudentDO();

                        String name = UUID.randomUUID().toString().replace("-", "");
                        studentDO.setName(name);

                        int row = dbHelper.insert(studentDO);
                        if (row != 1) {
                            insertFail.incrementAndGet();
                        } else {
                            insert.incrementAndGet();
                        }

                        StudentDO std2 = dbHelper.getOne(StudentDO.class, "where id=?", studentDO.getId());
                        if(std2 == null) {
                            System.err.println("id:" + studentDO.getId() + " not exist in db");
                            getIdFail.incrementAndGet();
                        } else if (!name.equals(std2.getName())) {
                            System.err.println("id:" + studentDO.getId() + " name not match");
                            getIdFail.incrementAndGet();
                        }
                    }
                }
            }, ""));
        }

        ThreadPoolUtils.waitAllFuturesDone(futureList);
        executeThem.shutdown();

        System.out.println("insert:" + insert.get() + ",insertFail:" + insertFail.get()
           + ",getIdFail:" + getIdFail.get());

        assert insert.get() > 0;
        assert insertFail.get() == 0;
        assert getIdFail.get() == 0;
    }

}
