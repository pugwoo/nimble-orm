package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.math.BigDecimal;

@Table("t_area_location")
public class AreaLocationDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    /**层级代号*/
    @Column(value = "layer_code")
    private String layerCode;

    /**区域代号*/
    @Column(value = "area_code")
    private String areaCode;

    @Column(value = "longitude")
    private BigDecimal longitude;

    @Column(value = "latitude")
    private BigDecimal latitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLayerCode() {
        return layerCode;
    }

    public void setLayerCode(String layerCode) {
        this.layerCode = layerCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

}
