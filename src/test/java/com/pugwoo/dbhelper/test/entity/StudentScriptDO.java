package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Table("t_student")
public class StudentScriptDO extends IdableSoftDeleteBaseDO {

    @Column(value = "name", insertValueScript = "1+1")
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
