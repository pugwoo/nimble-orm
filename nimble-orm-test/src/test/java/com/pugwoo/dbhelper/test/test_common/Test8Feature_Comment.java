package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Test8Feature_Comment {

    public abstract DBHelper getDBHelper();

    @Test
    public void testGlobalComment() throws Exception {
        // 暂不支持clickhouse，clickhouse不支持传递注释
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            return;
        }

        String globalComment = UUID.randomUUID().toString();
        DBHelper.setGlobalComment(globalComment);

        AtomicBoolean isWithComment = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            List<Map> processlist = getDBHelper().getRaw(Map.class, "SHOW PROCESSLIST");
            for (Map<String, Object> p : processlist) {
                String info = (String) p.get("Info");
                System.out.println("===" + info);
                if (info != null && info.contains(globalComment) && info.contains("select sleep(1)")) {
                    isWithComment.set(true);
                }
            }
        });
        thread.start();

        getDBHelper().getRaw(String.class, "select sleep(1)");

        thread.join();

        assert isWithComment.get();

        StudentDO studentDO = CommonOps.insertOne(getDBHelper());
        assert getDBHelper().getByKey(StudentDO.class, studentDO.getId()).getName().equals(studentDO.getName());
    }

    @Test
    public void testLocalComment() throws Exception {
        // 暂不支持clickhouse，clickhouse不支持传递注释
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            return;
        }

        String localComment = UUID.randomUUID().toString();
        DBHelper.setLocalComment(localComment);

        AtomicBoolean isWithComment = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            List<Map> processlist = getDBHelper().getRaw(Map.class, "SHOW PROCESSLIST");
            for (Map<String, Object> p : processlist) {
                String info = (String) p.get("Info");
                System.out.println("===" + info);
                if (info != null && info.contains(localComment) && info.contains("select sleep(1)")) {
                    isWithComment.set(true);
                }
            }
        });
        thread.start();

        getDBHelper().getRaw(String.class, "select sleep(1)");

        thread.join();

        assert isWithComment.get();

        StudentDO studentDO = CommonOps.insertOne(getDBHelper());
        assert getDBHelper().getByKey(StudentDO.class, studentDO.getId()).getName().equals(studentDO.getName());
    }

}
