package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

@Data
@Table(value = "", virtualTablePath = "virtual_table.sql")
public class StudentVirtualTableVO2 {

    @Column(value = "id")
    private Long id;

    @Column(value = "name")
    private String name;

    @Column(value = "schoolName")
    private String schoolName;

}
