package com.pugwoo.dbhelper.exception;

/**
 * 当乐观锁版本不匹配时，抛出异常
 */
public class CasVersionNotMatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CasVersionNotMatchException() {
    }

    public CasVersionNotMatchException(String errmsg) {
        super(errmsg);
    }

    public CasVersionNotMatchException(Throwable e) {
        super(e);
    }
}
