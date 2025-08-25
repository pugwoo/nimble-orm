package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentVO;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 测试 @FillColumn 注解功能
 */
public abstract class Test1Query_FillColumn {

    public abstract DBHelper getDBHelper();

    @Test
    public void testFillColumnBasic() {
        // 插入测试数据
        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), "TestSchool");
        StudentDO studentDO = CommonOps.insertOne(getDBHelper(), schoolDO.getId());

        // 查询学生信息，应该自动填充学校名称
        StudentVO studentVO = getDBHelper().getOne(StudentVO.class, "where id=?", studentDO.getId());
        
        System.out.println("Student: " + studentVO.getName());
        System.out.println("School ID: " + studentVO.getSchoolId());
        System.out.println("School Name (filled): " + studentVO.getSchoolName());
        
        // 验证填充的学校名称不为空
        assert studentVO.getSchoolName().equals("School_" + schoolDO.getId());
        assert studentVO.getSchoolGroupName().equals("Group_" + studentVO.getSchoolName());
        assert studentVO.getClassName().equals("Class_" + studentVO.getId() + "_" + schoolDO.getId());
        assert studentVO.getNotFoundFillField().equals("notfound");
    }

    @Test
    public void testFillColumnList() {
        // 插入测试数据
        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), "TestSchool");
        CommonOps.insertOne(getDBHelper(), schoolDO.getId());
        CommonOps.insertOne(getDBHelper(), schoolDO.getId());

        // 查询学生列表，应该自动填充学校名称
        List<StudentVO> studentVOList = getDBHelper().getAll(StudentVO.class, 
            "where school_id=?", schoolDO.getId());
        
        assert studentVOList.size() == 2;
        
        for (StudentVO studentVO : studentVOList) {
            assert studentVO.getSchoolName().equals("School_" + schoolDO.getId());
            assert studentVO.getSchoolGroupName().equals("Group_" + studentVO.getSchoolName());
            assert studentVO.getClassName().equals("Class_" + studentVO.getId() + "_" + schoolDO.getId());
            assert studentVO.getNotFoundFillField().equals("notfound");
        }
    }

    @Test
    public void testHandleFillColumnManually() {
        // 插入测试数据
        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), "TestSchool");
        StudentDO studentDO = CommonOps.insertOne(getDBHelper(), schoolDO.getId());

        // 先查询基本的学生信息（不会自动处理FillColumn）
        StudentDO basicStudent = getDBHelper().getOne(StudentDO.class, "where id=?", studentDO.getId());
        
        // 创建StudentVO并复制基本信息
        StudentVO studentVO = new StudentVO();
        studentVO.setId(basicStudent.getId());
        studentVO.setName(basicStudent.getName());
        studentVO.setSchoolId(basicStudent.getSchoolId());
        
        // 手动处理FillColumn
        getDBHelper().handleFillColumn(studentVO);

        assert studentVO.getSchoolName().equals("School_" + schoolDO.getId());
        assert studentVO.getSchoolGroupName().equals("Group_" + studentVO.getSchoolName());
        assert studentVO.getClassName().equals("Class_" + studentVO.getId() + "_" + schoolDO.getId());
        assert studentVO.getNotFoundFillField().equals("notfound");
    }
}
