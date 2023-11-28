package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.test.entity.StudentDO;

@Table(value = "", sameTableNameAs = StudentDO.class)
public class StudentVOForGroup {

    /**
     * 软删除标记为，0 未删除，1已删除
     */
    @Column(value = "deleted", softDelete = {"false", "true"})
    private Boolean deleted;

    @Column(value = "name", maxStringLength = 32)
    private String name;

    @Column(value = "age", computed = "max(age)")
    private Integer age;

}
