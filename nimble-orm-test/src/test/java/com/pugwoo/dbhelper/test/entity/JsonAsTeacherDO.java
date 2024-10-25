package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Data
@Table("t_json_raw")
public class JsonAsTeacherDO {

    @Data
    public static class TeacherBean {
        private String name;
        private Date birth;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private LocalTime localTime;
    }

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column(value = "json", isJSON = true)
    private TeacherBean teacher;

}
