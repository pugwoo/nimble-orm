package com.pugwoo.dbhelper.model;

public class RunningSqlData {

    /**dbHelper名称，需要用户自行指定*/
    private String dbHelperName;

    /**调用者代码位置，细到行号*/
    private String caller;

    /**启动时的时间戳，毫秒*/
    private Long startTimestampMs;

    /**执行的SQL*/
    private String sql;

    /**是否是批量执行*/
    private Boolean isBatch;

    /**批量操作的行数*/
    private Integer batchSize;

    /**查询已经运行的毫秒数*/
    public Long getRunningMs() {
        return System.currentTimeMillis() - startTimestampMs;
    }

    public String getDbHelperName() {
        return dbHelperName;
    }

    public void setDbHelperName(String dbHelperName) {
        this.dbHelperName = dbHelperName;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public Long getStartTimestampMs() {
        return startTimestampMs;
    }

    public void setStartTimestampMs(Long startTimestampMs) {
        this.startTimestampMs = startTimestampMs;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Boolean getIsBatch() {
        return isBatch;
    }

    public void setIsBatch(Boolean isBatch) {
        this.isBatch = isBatch;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
}
