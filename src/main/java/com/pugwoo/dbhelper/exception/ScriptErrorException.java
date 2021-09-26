package com.pugwoo.dbhelper.exception;

public class ScriptErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ScriptErrorException() {
    }

    public ScriptErrorException(String errMsg) {
        super(errMsg);
    }

    public ScriptErrorException(Throwable e) {
        super(e);
    }

}
