package com.pugwoo.dbhelper.sql;

/**
 * 生成批量插入SQL的返回值，抽取这个DTO的主要目的是为了log时不打印太多的信息
 */
public class InsertSQLForBatchDTO {

    /**
     * 批量插入SQL
     */
    private String sql;

    /**
     * 用于log记录时，打印到的sql的end index
     */
    private int sqlLogEndIndex;

    /**
     * 用于log记录时，打印到的结束参数index
     */
    private int paramLogEndIndex;

    public InsertSQLForBatchDTO() {
    }

    public InsertSQLForBatchDTO(String sql, int sqlLogEndIndex, int paramLogEndIndex) {
        this.sql = sql;
        this.sqlLogEndIndex = sqlLogEndIndex;
        this.paramLogEndIndex = paramLogEndIndex;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getSqlLogEndIndex() {
        return sqlLogEndIndex;
    }

    public void setSqlLogEndIndex(int sqlLogEndIndex) {
        this.sqlLogEndIndex = sqlLogEndIndex;
    }

    public int getParamLogEndIndex() {
        return paramLogEndIndex;
    }

    public void setParamLogEndIndex(int paramLogEndIndex) {
        this.paramLogEndIndex = paramLogEndIndex;
    }
}
