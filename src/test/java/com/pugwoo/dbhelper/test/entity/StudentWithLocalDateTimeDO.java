package com.pugwoo.dbhelper.test.entity;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Table("t_student")
public class StudentWithLocalDateTimeDO {

    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column(value = "create_time", setTimeWhenInsert = true)
    private LocalDateTime createTime;

    @Column(value = "update_time", setTimeWhenInsert = true)
    private LocalDate updateTime;

    @Column(value = "delete_time", setTimeWhenDelete = true)
    private Date deleteTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDate getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDate updateTime) {
        this.updateTime = updateTime;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }
}
