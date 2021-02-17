package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Table("t_types")
public class TypesDO {

    // 测试多字段key

    @Column(value = "id1", isKey = true)
    private Long id1;

    @Column(value = "id2", isKey = true)
    private Long id2;

    @Column("my_byte")
    private Byte myByte;

    @Column("my_short")
    private Short myShort;

    @Column("my_float")
    private Float myFloat;

    @Column("my_double")
    private Double myDouble;

    @Column("my_decimal")
    private BigDecimal myDecimal;

    @Column(value = "my_date")
    private Date myDate;

    @Column(value = "my_datetime")
    private Time myTime;

    @Column(value = "my_timestamp")
    private Timestamp myTimestamp;

    @Column(value = "my_mediumint")
    private Integer myMediumint;

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public Byte getMyByte() {
        return myByte;
    }

    public void setMyByte(Byte myByte) {
        this.myByte = myByte;
    }

    public Short getMyShort() {
        return myShort;
    }

    public void setMyShort(Short myShort) {
        this.myShort = myShort;
    }

    public Float getMyFloat() {
        return myFloat;
    }

    public void setMyFloat(Float myFloat) {
        this.myFloat = myFloat;
    }

    public Double getMyDouble() {
        return myDouble;
    }

    public void setMyDouble(Double myDouble) {
        this.myDouble = myDouble;
    }

    public BigDecimal getMyDecimal() {
        return myDecimal;
    }

    public void setMyDecimal(BigDecimal myDecimal) {
        this.myDecimal = myDecimal;
    }

    public Date getMyDate() {
        return myDate;
    }

    public void setMyDate(Date myDate) {
        this.myDate = myDate;
    }

    public Time getMyTime() {
        return myTime;
    }

    public void setMyTime(Time myTime) {
        this.myTime = myTime;
    }

    public Timestamp getMyTimestamp() {
        return myTimestamp;
    }

    public void setMyTimestamp(Timestamp myTimestamp) {
        this.myTimestamp = myTimestamp;
    }

    public Integer getMyMediumint() {
        return myMediumint;
    }

    public void setMyMediumint(Integer myMediumint) {
        this.myMediumint = myMediumint;
    }
}
