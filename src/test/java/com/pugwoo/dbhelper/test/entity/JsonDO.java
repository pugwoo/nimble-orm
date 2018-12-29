package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.util.Map;

@Table("t_json")
public class JsonDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column(value = "json", isJSON = true)
    private Map<String, Object> json;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getJson() {
        return json;
    }

    public void setJson(Map<String, Object> json) {
        this.json = json;
    }
}
