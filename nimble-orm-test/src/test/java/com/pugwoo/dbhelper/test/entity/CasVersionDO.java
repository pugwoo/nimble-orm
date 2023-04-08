package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

@Data
@Table("t_cas_version")
public class CasVersionDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Integer id;

    @Column(value = "name")
    private String name;

    @Column(value = "version", casVersion = true)
    private Integer version;

}