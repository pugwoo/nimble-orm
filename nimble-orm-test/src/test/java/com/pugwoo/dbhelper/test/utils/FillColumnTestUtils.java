package com.pugwoo.dbhelper.test.utils;

/**
 * 用于测试 @FillColumn 功能的工具类
 */
public class FillColumnTestUtils {

    /**
     * 根据学校ID获取学校名称（模拟方法）
     */
    public static String getSchoolNameById(Object schoolId) {
        if (schoolId == null) {
            return null;
        }
        return "School_" + schoolId;
    }

    /**
     * 根据学校名称获取学校组名称（模拟方法）
     */
    public static String getSchoolGroupNameBySchoolName(Object schoolName) {
        if (schoolName == null) {
            return null;
        }
        return "Group_" + schoolName;
    }

    /**
     * 根据多个班级ID获取班级名称（模拟方法）
     */
    public static String getClassNameByIds(Object classId1, Object classId2) {
        if (classId1 == null && classId2 == null) {
            return "notfound";
        }
        return "Class_" + classId1 + "_" + classId2;
    }
}
