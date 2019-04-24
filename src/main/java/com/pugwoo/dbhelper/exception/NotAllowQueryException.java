package com.pugwoo.dbhelper.exception;

public class NotAllowQueryException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NotAllowQueryException() {
	}
	
	public NotAllowQueryException(String errmsg) {
		super(errmsg);
	}

	public NotAllowQueryException(Throwable e) {
		super(e);
	}
}
