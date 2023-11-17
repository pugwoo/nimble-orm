package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Random;

public abstract class Test8Feature_StringAutoTrim {

    public abstract DBHelper getDBHelper();

    @Data
    @Table(value = "t_school", autoTrimString = 1)
    public static class SchoolTableTrimDO {
        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;

        @Column(value = "name", maxStringLength = 32)
        private String name;
    }

    @Data
    @Table(value = "t_school")
    public static class SchoolColumnTrimDO {
        @Column(value = "id", isKey = true, isAutoIncrement = true)
        private Long id;

        @Column(value = "name", maxStringLength = 32, autoTrimString = 1)
        private String name;
    }

    @Test
    public void testTrim() {
        SchoolTableTrimDO schoolTableTrimDO = new SchoolTableTrimDO();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            schoolTableTrimDO.setId(CommonOps.getRandomLong());
        }
        schoolTableTrimDO.setName("  123  ");
        getDBHelper().insert(schoolTableTrimDO);
        assert schoolTableTrimDO.getName().equals("123");
        assert getDBHelper().getOne(SchoolTableTrimDO.class, "where id=?", schoolTableTrimDO.getId())
                .getName().equals("123");

        schoolTableTrimDO.setName("   456   ");
        getDBHelper().update(schoolTableTrimDO);
        assert schoolTableTrimDO.getName().equals("456");
        assert getDBHelper().getOne(SchoolTableTrimDO.class, "where id=?", schoolTableTrimDO.getId())
                .getName().equals("456");

        SchoolColumnTrimDO schoolColumnTrimDO = new SchoolColumnTrimDO();
        schoolColumnTrimDO.setName("  123  ");
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            schoolColumnTrimDO.setId(CommonOps.getRandomLong());
        }

        getDBHelper().insert(schoolColumnTrimDO);
        assert schoolColumnTrimDO.getName().equals("123");
        assert getDBHelper().getOne(SchoolColumnTrimDO.class, "where id=?", schoolColumnTrimDO.getId())
                .getName().equals("123");

        schoolColumnTrimDO.setName("   456   ");
        getDBHelper().update(schoolColumnTrimDO);
        assert schoolColumnTrimDO.getName().equals("456");
        assert getDBHelper().getOne(SchoolColumnTrimDO.class, "where id=?", schoolColumnTrimDO.getId())
                .getName().equals("456");
    }

}
