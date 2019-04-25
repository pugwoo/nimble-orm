package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

/**这个用于把json当字符串存入的类*/
@Table("t_json_raw")
public class JsonRawDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column("json")
    private String json;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
