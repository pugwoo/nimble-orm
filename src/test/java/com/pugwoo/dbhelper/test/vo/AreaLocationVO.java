package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.test.entity.AreaLocationDO;

public class AreaLocationVO extends AreaLocationDO {

    /**
     * 物理存储上，是有layer_code和area_code来唯一标识一个位置
     * 而这个code，是由layer_code和area_code一起组合起来的位置
     */
    @Column(value = "code", computed = "CONCAT(layer_code,'/',area_code)")
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
