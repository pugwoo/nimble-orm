package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Table("t_json")
public class JsonDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column(value = "json", isJSON = true)
    private Map<String, List<BigDecimal>> json;

    @Column(value = "json2", isJSON = true)
    private Map<String, Object> json2;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, List<BigDecimal>> getJson() {
        return json;
    }

    public void setJson(Map<String, List<BigDecimal>> json) {
        this.json = json;
    }

    public Map<String, Object> getJson2() {
        return json2;
    }

    public void setJson2(Map<String, Object> json2) {
        this.json2 = json2;
    }
}
