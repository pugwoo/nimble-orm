package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;

@Data
@Table(value = "t_student")
public class StudentSumVO {

    @Column(value = "ageSum", computed = "sum(age)")
    private Integer ageSum;

}
