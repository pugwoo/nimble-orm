package com.pugwoo.dbhelper.test.entity;

import lombok.Data;
import lombok.ToString;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Data
@ToString
@Table("t_uuid")
public class UuidDO {

    @Column(value = "uuid", isKey = true)
    private String uuid;

    @Column(value = "name")
    private String name;

}