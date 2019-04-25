package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.JsonAsTeacherDO;
import com.pugwoo.dbhelper.test.entity.JsonDO;
import com.pugwoo.dbhelper.test.entity.JsonRawDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 2018年12月29日 17:39:42
 * json是mysql5.7+支持的一个重要特性，一定程度上让mysql具备面向文档、schema-free的功能
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestJson {

    @Autowired
    private DBHelper dbHelper;

    private long insert(String name, Integer age) {
        JsonDO jsonDO = new JsonDO();
        Map<String, Object> json = new HashMap();
        json.put("name", name);
        json.put("age", age);
        jsonDO.setJson(json);
        dbHelper.insert(jsonDO);
        return jsonDO.getId();
    }

    @Test
    @Rollback(false)
    public void testJsonQuery() {

        String name = UUID.randomUUID().toString();
        int age = new Random().nextInt();

        long id = insert(name, age);
        assert id > 0;

        JsonDO jsonDO1 = dbHelper.getByKey(JsonDO.class, id);
        assert jsonDO1 != null;
        assert jsonDO1.getId() == id;
        assert name.equals(jsonDO1.getJson().get("name"));
        assert new Integer(age).equals(jsonDO1.getJson().get("age"));

        List<JsonDO> list = dbHelper.getAll(JsonDO.class);
        assert list.size() > 0;

        // json查询的两种写法

        list = dbHelper.getAll(JsonDO.class, "WHERE JSON->'$.name'=?", name);
        assert list.size() == 1;
        assert name.equals(list.get(0).getJson().get("name"));

        list = dbHelper.getAll(JsonDO.class, "WHERE JSON_EXTRACT(JSON, '$.name')=?", name);
        assert list.size() == 1;
        assert name.equals(list.get(0).getJson().get("name"));
    }

    @Test @Rollback(false)
    public void testJsonRaw() {
        JsonRawDO jsonRawDO = new JsonRawDO();
        jsonRawDO.setJson("{\"name\":\"wu\",\"birth\":\"1960-06-08\"}");

        dbHelper.insert(jsonRawDO);
        assert jsonRawDO.getId() != null;

        JsonAsTeacherDO teacherDO = dbHelper.getByKey(JsonAsTeacherDO.class, jsonRawDO.getId());
        assert teacherDO.getTeacher().getName().equals("wu");

        System.out.println(teacherDO.getTeacher().getBirth());
    }

}
