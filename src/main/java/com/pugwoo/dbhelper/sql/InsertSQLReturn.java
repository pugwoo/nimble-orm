package com.pugwoo.dbhelper.sql;

/**
 * 获得插入sql的返回数据
 */
public class InsertSQLReturn {

    /**
     * 插入sql
     */
    private String sql;

    /**
     * 是否包含null值
     */
    private boolean isContainsNullValue;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public boolean isContainsNullValue() {
        return isContainsNullValue;
    }

    public void setContainsNullValue(boolean containsNullValue) {
        isContainsNullValue = containsNullValue;
    }

}
