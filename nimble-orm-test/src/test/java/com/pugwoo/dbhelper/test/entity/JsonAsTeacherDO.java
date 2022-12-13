package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.util.Date;

@Table("t_json_raw")
public class JsonAsTeacherDO {

    public static class TeacherBean {

        private String name;

        private Date birth;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getBirth() {
            return birth;
        }

        public void setBirth(Date birth) {
            this.birth = birth;
        }
    }

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column(value = "json", isJSON = true)
    private TeacherBean teacher;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TeacherBean getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherBean teacher) {
        this.teacher = teacher;
    }
}
