package com.pugwoo.dbhelper.exception;

/**
 * 当乐观锁版本不匹配时，抛出异常
 */
public class CasVersionNotMatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int affectedRows;

    public CasVersionNotMatchException() {
    }

    public CasVersionNotMatchException(String errMsg) {
        super(errMsg);
    }

    public CasVersionNotMatchException(int affectedRows, String errMsg) {
        super(errMsg);
        this.affectedRows = affectedRows;
    }


    public CasVersionNotMatchException(Throwable e) {
        super(e);
    }

    public int getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }
}
