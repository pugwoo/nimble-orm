package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.json.NimbleOrmDateUtils;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.test.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.*;

/**
 * 2018年12月29日 17:39:42
 * json是mysql5.7+支持的一个重要特性，一定程度上让mysql具备面向文档、schema-free的功能
 */
@SpringBootTest
public class Test9Other_Json {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testJSON() {
        StudentDO studentDO = new StudentDO();
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("SYSU");

        studentDO.setSchoolSnapshot(schoolDO);

        List<CourseDO> courses = new ArrayList<CourseDO>();
        studentDO.setCourseSnapshot(courses);

        CourseDO course1 = new CourseDO();
        course1.setName("math");
        courses.add(course1);

        CourseDO course2 = new CourseDO();
        course2.setName("eng");
        courses.add(course2);

        dbHelper.insert(studentDO);

        StudentDO studentDB = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert studentDB.getSchoolSnapshot() != null;
        assert studentDB.getSchoolSnapshot().getName().equals("SYSU");

        assert studentDB.getCourseSnapshot() != null;
        assert studentDB.getCourseSnapshot().size() == 2;
        assert studentDB.getCourseSnapshot().get(0).getName().equals("math");
        assert studentDB.getCourseSnapshot().get(1).getName().equals("eng");

        studentDO.getCourseSnapshot().get(1).setName("english");
        dbHelper.update(studentDO);

        studentDB = dbHelper.getByKey(StudentDO.class, studentDO.getId());
        assert studentDB.getCourseSnapshot().get(1).getName().equals("english");

    }

    private long insert(String score, Integer age) {
        JsonDO jsonDO = new JsonDO();
        Map<String, List<BigDecimal>> json = new HashMap<>();

        List<BigDecimal> scores = new ArrayList<>();
        scores.add(new BigDecimal(score));
        scores.add(BigDecimal.ONE);

        List<BigDecimal> ages = new ArrayList<>();
        ages.add(new BigDecimal(age));
        ages.add(BigDecimal.TEN);

        json.put("score", scores);
        json.put("age", ages);
        json.put(null, null);
        jsonDO.setJson(json);

        Map<String, Object> json2 = new HashMap<>();
        json2.put("score", score);
        json2.put("age", age);
        jsonDO.setJson2(json2);

        dbHelper.insert(jsonDO);
        return jsonDO.getId();
    }

    @Test
    public void testJsonQuery() {

        String score = String.valueOf(new Random().nextInt());
        int age = new Random().nextInt();

        long id = insert(score, age);
        assert id > 0;

        JsonDO jsonDO1 = dbHelper.getByKey(JsonDO.class, id);
        assert jsonDO1 != null;
        assert jsonDO1.getId() == id;
        assert score.equals(jsonDO1.getJson().get("score").get(0).toString());
        assert String.valueOf(age).equals(jsonDO1.getJson().get("age").get(0).toString());

        // 检查类型
        for (List<BigDecimal> list : jsonDO1.getJson().values()) {
            if (list == null) {
                continue;
            }
            for (BigDecimal b : list) {
                assert b == null || b instanceof BigDecimal;
            }
        }

        List<JsonDO> list = dbHelper.getAll(JsonDO.class);
        assert list.size() > 0;

        // json查询的两种写法

        list = dbHelper.getAll(JsonDO.class, "WHERE json2->'$.score'=?", score);
        assert list.size() == 1;
        assert score.equals(list.get(0).getJson2().get("score").toString());

        list = dbHelper.getAll(JsonDO.class, "WHERE JSON_EXTRACT(json2, '$.score')=?", score);
        assert list.size() == 1;
        assert score.equals(list.get(0).getJson2().get("score").toString());
    }

    @Test 
    public void testJsonRaw() {
        JsonRawDO jsonRawDO = new JsonRawDO();
        jsonRawDO.setJson("{\"name\":\"wu\",\"birth\":\"1960-06-08 12:13:14\"}");

        dbHelper.insert(jsonRawDO);
        assert jsonRawDO.getId() != null;

        JsonAsTeacherDO teacherDO = dbHelper.getByKey(JsonAsTeacherDO.class, jsonRawDO.getId());
        assert teacherDO.getTeacher().getName().equals("wu");
        assert NimbleOrmDateUtils.formatDate(teacherDO.getTeacher().getBirth()).equals("1960-06-08");
        assert NimbleOrmDateUtils.format(teacherDO.getTeacher().getBirth()).equals("1960-06-08 12:13:14");

        jsonRawDO = new JsonRawDO();
        jsonRawDO.setJson("{\"name\":\"wu\",\"birth\":\"\"}");
        dbHelper.insert(jsonRawDO);
        teacherDO = dbHelper.getByKey(JsonAsTeacherDO.class, jsonRawDO.getId());
        assert teacherDO.getTeacher().getName().equals("wu");
        assert teacherDO.getTeacher().getBirth() == null;

        jsonRawDO = new JsonRawDO();
        jsonRawDO.setJson("{\"name\":\"wu\",\"birth\":null,null:null}");
        dbHelper.insert(jsonRawDO);
        teacherDO = dbHelper.getByKey(JsonAsTeacherDO.class, jsonRawDO.getId());
        assert teacherDO.getTeacher().getName().equals("wu");
        assert teacherDO.getTeacher().getBirth() == null;

    }

    private static class TimeDTO {
        private Date date;
        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @Test
    public void testDateTimestamp() throws Exception {
        Date now = new Date();
        assert now.equals(NimbleOrmDateUtils.parseThrowException(String.valueOf(now.getTime())));

        String json = "{\"date\":" + now.getTime() + "}";
        String json2 = "{\"date\":\"" + now.getTime() + "\"}";

        TimeDTO time1 = NimbleOrmJSON.parse(json, TimeDTO.class);
        TimeDTO time2 = NimbleOrmJSON.parse(json2, TimeDTO.class);

        assert time1.getDate().equals(now);
        assert time2.getDate().equals(now);
    }

}
