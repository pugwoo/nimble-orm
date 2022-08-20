package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Table("t_school")
public class SchoolForReadNullDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column(value = "name", maxStringLength = 32, readIfNullScript = "'myname'")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
