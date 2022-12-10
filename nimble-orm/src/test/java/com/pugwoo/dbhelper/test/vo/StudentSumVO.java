package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.test.entity.StudentDO;

public class StudentSumVO extends StudentDO {

    @Column(value = "ageSum", computed = "sum(age)")
    private Integer ageSum;

    public Integer getAgeSum() {
        return ageSum;
    }

    public void setAgeSum(Integer ageSum) {
        this.ageSum = ageSum;
    }

}
