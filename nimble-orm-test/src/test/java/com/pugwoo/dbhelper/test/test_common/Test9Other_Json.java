package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.json.NimbleOrmDateUtils;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.JsonAsTeacherDO;
import com.pugwoo.dbhelper.test.entity.JsonDO;
import com.pugwoo.dbhelper.test.entity.JsonRawDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2018年12月29日 17:39:42
 * json是mysql5.7+支持的一个重要特性，一定程度上让mysql具备面向文档、schema-free的功能
 */
public abstract class Test9Other_Json {

    public abstract DBHelper getDBHelper();

    @Test
    public void testJSON() {
        StudentDO studentDO = new StudentDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            studentDO.setId(CommonOps.getRandomLong());
        }
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

        getDBHelper().insert(studentDO);

        StudentDO studentDB = getDBHelper().getByKey(StudentDO.class, studentDO.getId());
        assert studentDB.getSchoolSnapshot() != null;
        assert studentDB.getSchoolSnapshot().getName().equals("SYSU");

        assert studentDB.getCourseSnapshot() != null;
        assert studentDB.getCourseSnapshot().size() == 2;
        assert studentDB.getCourseSnapshot().get(0).getName().equals("math");
        assert studentDB.getCourseSnapshot().get(1).getName().equals("eng");

        studentDO.getCourseSnapshot().get(1).setName("english");
        getDBHelper().update(studentDO);

        studentDB = getDBHelper().getByKey(StudentDO.class, studentDO.getId());
        assert studentDB.getCourseSnapshot().get(1).getName().equals("english");

    }

    private long insert(String score, Integer age) {
        JsonDO jsonDO = new JsonDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            jsonDO.setId(CommonOps.getRandomLong());
        }

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

        getDBHelper().insert(jsonDO);
        return jsonDO.getId();
    }

    @Test
    public void testJsonQuery() {

        String score = String.valueOf(CommonOps.getRandomInt());
        int age = CommonOps.getRandomInt();

        long id = insert(score, age);
        assert id > 0;

        JsonDO jsonDO1 = getDBHelper().getByKey(JsonDO.class, id);
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

        List<JsonDO> list = getDBHelper().getAll(JsonDO.class);
        assert list.size() > 0;

        // json查询的两种写法，不适合于：clickhouse、
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.MYSQL) {
            list = getDBHelper().getAll(JsonDO.class, "WHERE json2->'$.score'=?", score);
            assert list.size() == 1;
            assert score.equals(list.get(0).getJson2().get("score").toString());

            list = getDBHelper().getAll(JsonDO.class, "WHERE JSON_EXTRACT(json2, '$.score')=?", score);
            assert list.size() == 1;
            assert score.equals(list.get(0).getJson2().get("score").toString());
        }

        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.POSTGRESQL) {
            list = getDBHelper().getAll(JsonDO.class, "WHERE json2->>'score'=?", score);
            assert list.size() == 1;
            assert score.equals(list.get(0).getJson2().get("score").toString());
        }

        // TODO clickhouse的json字段查询待写单元测试
    }

    @Test 
    public void testJsonRaw() {
        JsonRawDO jsonRawDO = new JsonRawDO();
        jsonRawDO.setJson("{\"name\":\"wu\",\"birth\":\"1960-06-08 12:13:14\"}");
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            jsonRawDO.setId(CommonOps.getRandomLong());
        }
        getDBHelper().insert(jsonRawDO);
        assert jsonRawDO.getId() != null;

        JsonAsTeacherDO teacherDO = getDBHelper().getByKey(JsonAsTeacherDO.class, jsonRawDO.getId());
        assert teacherDO.getTeacher().getName().equals("wu");
        assert NimbleOrmDateUtils.formatDate(teacherDO.getTeacher().getBirth()).equals("1960-06-08");
        assert NimbleOrmDateUtils.format(teacherDO.getTeacher().getBirth()).equals("1960-06-08 12:13:14");

        jsonRawDO = new JsonRawDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            jsonRawDO.setId(CommonOps.getRandomLong());
        }
        jsonRawDO.setJson("{\"name\":\"wu\",\"birth\":\"\"}");
        getDBHelper().insert(jsonRawDO);
        teacherDO = getDBHelper().getByKey(JsonAsTeacherDO.class, jsonRawDO.getId());
        assert teacherDO.getTeacher().getName().equals("wu");
        assert teacherDO.getTeacher().getBirth() == null;

        jsonRawDO = new JsonRawDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            jsonRawDO.setId(CommonOps.getRandomLong());
        }
        jsonRawDO.setJson("{\"name\":\"wu\",\"birth\":null,null:null}");
        getDBHelper().insert(jsonRawDO);
        teacherDO = getDBHelper().getByKey(JsonAsTeacherDO.class, jsonRawDO.getId());
        assert teacherDO.getTeacher().getName().equals("wu");
        assert teacherDO.getTeacher().getBirth() == null;

        // test LocalDate LocalDateTime LocalTime
        JsonAsTeacherDO jsonAsTeacherDO = new JsonAsTeacherDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            jsonAsTeacherDO.setId(CommonOps.getRandomLong());
        }
        jsonAsTeacherDO.setTeacher(new JsonAsTeacherDO.TeacherBean());
        jsonAsTeacherDO.getTeacher().setLocalDateTime(LocalDateTime.now());
        jsonAsTeacherDO.getTeacher().setLocalDate(LocalDate.now());
        jsonAsTeacherDO.getTeacher().setLocalTime(LocalTime.now());
        assert getDBHelper().insert(jsonAsTeacherDO) == 1;

        JsonAsTeacherDO jsonAsTeacher2 = getDBHelper().getByKey(JsonAsTeacherDO.class, jsonAsTeacherDO.getId());
        assert jsonAsTeacher2.getId().equals(jsonAsTeacherDO.getId());
        assert jsonAsTeacher2.getTeacher().getLocalDateTime().equals(jsonAsTeacherDO.getTeacher().getLocalDateTime());
        assert jsonAsTeacher2.getTeacher().getLocalDate().equals(jsonAsTeacherDO.getTeacher().getLocalDate());
        assert jsonAsTeacher2.getTeacher().getLocalTime().equals(jsonAsTeacherDO.getTeacher().getLocalTime());

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
