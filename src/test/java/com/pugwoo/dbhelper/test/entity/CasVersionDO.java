package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Table("t_cas_version")
public class CasVersionDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Integer id;

    @Column(value = "name")
    private String name;

    @Column(value = "version", casVersion = true)
    private Integer version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}