package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test8Feature_StringAutoTrim {

    @Autowired
    private DBHelper dbHelper;

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
        schoolTableTrimDO.setName("  123  ");
        dbHelper.insert(schoolTableTrimDO);
        assert schoolTableTrimDO.getName().equals("123");
        assert dbHelper.getOne(SchoolTableTrimDO.class, "where id=?", schoolTableTrimDO.getId())
                .getName().equals("123");

        schoolTableTrimDO.setName("   456   ");
        dbHelper.update(schoolTableTrimDO);
        assert schoolTableTrimDO.getName().equals("456");
        assert dbHelper.getOne(SchoolTableTrimDO.class, "where id=?", schoolTableTrimDO.getId())
                .getName().equals("456");

        SchoolColumnTrimDO schoolColumnTrimDO = new SchoolColumnTrimDO();
        schoolColumnTrimDO.setName("  123  ");
        dbHelper.insert(schoolColumnTrimDO);
        assert schoolColumnTrimDO.getName().equals("123");
        assert dbHelper.getOne(SchoolColumnTrimDO.class, "where id=?", schoolColumnTrimDO.getId())
                .getName().equals("123");

        schoolColumnTrimDO.setName("   456   ");
        dbHelper.update(schoolColumnTrimDO);
        assert schoolColumnTrimDO.getName().equals("456");
        assert dbHelper.getOne(SchoolColumnTrimDO.class, "where id=?", schoolColumnTrimDO.getId())
                .getName().equals("456");
    }

}
