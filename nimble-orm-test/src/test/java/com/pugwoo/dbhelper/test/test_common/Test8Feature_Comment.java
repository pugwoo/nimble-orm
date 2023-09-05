package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootTest
public class Test8Feature_Comment {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testGlobalComment() throws Exception {
        String globalComment = UUID.randomUUID().toString();
        DBHelper.setGlobalComment(globalComment);

        AtomicBoolean isWithComment = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            List<Map> processlist = dbHelper.getRaw(Map.class, "SHOW PROCESSLIST");
            for (Map<String, Object> p : processlist) {
                String info = (String) p.get("Info");
                System.out.println("===" + info);
                if (info != null && info.contains(globalComment) && info.contains("select sleep(1)")) {
                    isWithComment.set(true);
                }
            }
        });
        thread.start();

        dbHelper.getRaw(String.class, "select sleep(1)");

        thread.join();

        assert isWithComment.get();

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        assert dbHelper.getByKey(StudentDO.class, studentDO.getId()).getName().equals(studentDO.getName());
    }

    @Test
    public void testLocalComment() throws Exception {
        String localComment = UUID.randomUUID().toString();
        DBHelper.setLocalComment(localComment);

        AtomicBoolean isWithComment = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            List<Map> processlist = dbHelper.getRaw(Map.class, "SHOW PROCESSLIST");
            for (Map<String, Object> p : processlist) {
                String info = (String) p.get("Info");
                System.out.println("===" + info);
                if (info != null && info.contains(localComment) && info.contains("select sleep(1)")) {
                    isWithComment.set(true);
                }
            }
        });
        thread.start();

        dbHelper.getRaw(String.class, "select sleep(1)");

        thread.join();

        assert isWithComment.get();

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        assert dbHelper.getByKey(StudentDO.class, studentDO.getId()).getName().equals(studentDO.getName());
    }

}
