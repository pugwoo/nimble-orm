package com.pugwoo.dbhelper.exception;

public class ScriptErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ScriptErrorException() {
    }

    public ScriptErrorException(String errmsg) {
        super(errmsg);
    }

    public ScriptErrorException(Throwable e) {
        super(e);
    }

}
