package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Table("t_student")
public class StudentRandomNameDO extends IdableSoftDeleteBaseDO {

    @Column(value = "name", setRandomStringWhenInsert = true)
    private String name;

    public String getName() {
        return name;
    }
}
