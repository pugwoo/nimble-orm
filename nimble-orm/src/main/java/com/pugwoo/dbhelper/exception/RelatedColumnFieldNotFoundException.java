package com.pugwoo.dbhelper.exception;

/**
 * RelatedColumn的localColumn或remoteColumn错误时抛出
 */
public class RelatedColumnFieldNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RelatedColumnFieldNotFoundException() {
    }

    public RelatedColumnFieldNotFoundException(String errMsg) {
        super(errMsg);
    }

    public RelatedColumnFieldNotFoundException(Throwable e) {
        super(e);
    }

}
