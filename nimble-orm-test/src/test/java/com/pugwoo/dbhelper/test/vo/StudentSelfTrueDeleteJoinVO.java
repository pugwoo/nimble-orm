package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.test.entity.StudentHardDeleteDO;

@JoinTable(joinType = JoinTypeEnum.RIGHT_JOIN, on = "t1.id = t2.id")
public class StudentSelfTrueDeleteJoinVO {

    @JoinLeftTable
    private StudentHardDeleteDO student1;

    @JoinRightTable
    private StudentHardDeleteDO student2;

    public StudentHardDeleteDO getStudent1() {
        return student1;
    }

    public void setStudent1(StudentHardDeleteDO student1) {
        this.student1 = student1;
    }

    public StudentHardDeleteDO getStudent2() {
        return student2;
    }

    public void setStudent2(StudentHardDeleteDO student2) {
        this.student2 = student2;
    }
}
