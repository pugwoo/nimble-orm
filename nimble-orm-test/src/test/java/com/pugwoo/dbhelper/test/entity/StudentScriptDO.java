package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Table("t_student")
public class StudentScriptDO extends IdableSoftDeleteBaseDO {

    @Column(value = "name", insertValueScript = "1+'1'+1", updateValueScript = "2+'2'+2", deleteValueScript = "3+'3'+3")
    private String name;

    @Column(value = "age", insertValueScript = "0")
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
