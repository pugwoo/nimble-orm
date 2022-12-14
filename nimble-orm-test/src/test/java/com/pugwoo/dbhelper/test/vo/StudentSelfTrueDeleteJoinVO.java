package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.test.entity.StudentTrueDeleteDO;

@JoinTable(joinType = JoinTypeEnum.RIGHT_JOIN, on = "t1.id = t2.id")
public class StudentSelfTrueDeleteJoinVO {

    @JoinLeftTable
    private StudentTrueDeleteDO student1;

    @JoinRightTable
    private StudentTrueDeleteDO student2;

    public StudentTrueDeleteDO getStudent1() {
        return student1;
    }

    public void setStudent1(StudentTrueDeleteDO student1) {
        this.student1 = student1;
    }

    public StudentTrueDeleteDO getStudent2() {
        return student2;
    }

    public void setStudent2(StudentTrueDeleteDO student2) {
        this.student2 = student2;
    }
}
