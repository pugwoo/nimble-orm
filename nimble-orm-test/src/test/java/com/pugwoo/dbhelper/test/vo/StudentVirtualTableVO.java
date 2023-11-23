package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

@Data
@Table(value = "", virtualTableSQL = "SELECT t1.id AS id, t1.name AS NAME, t2.name AS schoolName" +
        " FROM t_student t1" +
        " LEFT JOIN t_school t2" +
        " ON t1.school_id=t2.id" +
        " WHERE t1.deleted=false AND t2.deleted=false")
public class StudentVirtualTableVO {

    @Column(value = "id")
    private Long id;

    @Column(value = "name")
    private String name;

    @Column(value = "schoolName")
    private String schoolName;

}
