package com.pugwoo.dbhelper.test.benchmark;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.task.ExecuteThem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
// 这里不能用@Transactional，就是要测没有事务情况下的
public class BenchmarkInsertAndGetId {

    @Autowired
    private DBHelper dbHelper;

    @Test
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
        ExecuteThem executeThem = new ExecuteThem(400);

        for (int i = 0; i < 400; i++) {
            executeThem.add(new Runnable() {
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
            });
        }

        executeThem.waitAllTerminate();
        System.out.println("insert:" + insert.get() + ",insertFail:" + insertFail.get()
           + ",getIdFail:" + getIdFail.get());
    }

    @Test
    public void tmp() {
        StudentDO studentDO = new StudentDO();

        String name = UUID.randomUUID().toString().replace("-", "");
        studentDO.setName(name);

        int row = dbHelper.insertWhereNotExist(studentDO, "where name=?", name);

        System.out.println(JSON.toJson(studentDO));

        StudentDO student2 = dbHelper.getOne(StudentDO.class, "where id=?", studentDO.getId());
        System.out.println(JSON.toJson(student2));
    }

    @Test
    public void benchmarkWhereNotExist() {

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
        ExecuteThem executeThem = new ExecuteThem(400);

        for (int i = 0; i < 400; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        StudentDO studentDO = new StudentDO();

                        String name = UUID.randomUUID().toString().replace("-", "");
                        studentDO.setName(name);

                        int row = dbHelper.insertWhereNotExist(studentDO, "where name=?", name);
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
                            System.err.println("id:" + studentDO.getId() + " name not match, orgin:"
                               + name + ",in fact:" + std2.getName());
                            getIdFail.incrementAndGet();
                        }
                    }
                }
            });
        }

        executeThem.waitAllTerminate();
        System.out.println("insert:" + insert.get() + ",insertFail:" + insertFail.get()
                + ",getIdFail:" + getIdFail.get());
    }

}
