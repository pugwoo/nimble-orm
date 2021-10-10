package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.test.entity.StudentDO;

/**
 * 这里专门构建一个和DO出现字段同名的情况
 * 这里要处理为：
 */
public class StudentSameColumnNameVO extends StudentDO {

    // 这个属性覆盖了父类的name
    @Column(value = "name", computed = "CONCAT(name,'FFFFFFFF')")
    private String name;

    // 这个属性和同类的相同，会被忽略
    @Column(value = "name", computed = "CONCAT(name,'EEEEEEEE')")
    private String name2;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }
}
