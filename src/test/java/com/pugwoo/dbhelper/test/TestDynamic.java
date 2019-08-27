package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.test.entity.StudentDO;

public class TestDynamic {

    public static void main(String[] args) {
        String tableName = DBHelperContext.getTableName(StudentDO.class);
        assert tableName == null;

        DBHelperContext.setTableName(StudentDO.class, "my_table");
        tableName = DBHelperContext.getTableName(StudentDO.class);
        assert tableName.equals("my_table");

        DBHelperContext.resetTableName();
        tableName = DBHelperContext.getTableName(StudentDO.class);
        assert tableName == null;
    }


}
