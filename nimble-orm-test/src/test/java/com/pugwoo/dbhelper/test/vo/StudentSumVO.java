package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import lombok.Data;

@Data
@Table(value = "", sameTableNameAs = StudentDO.class)
public class StudentSumVO {

    @Column(value = "ageSum", computed = "sum(age)")
    private Integer ageSum;

}
