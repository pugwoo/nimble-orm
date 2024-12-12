package com.pugwoo.dbhelper.exception;

public class EnumNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EnumNotSupportedException() {
    }

    public EnumNotSupportedException(String errMsg) {
        super(errMsg);
    }

    public EnumNotSupportedException(Throwable e) {
        super(e);
    }

}
