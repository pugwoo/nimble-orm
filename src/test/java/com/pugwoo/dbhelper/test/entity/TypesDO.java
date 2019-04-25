package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.math.BigDecimal;

@Table("t_types")
public class TypesDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
