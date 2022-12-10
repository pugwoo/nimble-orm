package com.pugwoo.dbhelper.exception;

public class NotOnlyOneKeyColumnException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotOnlyOneKeyColumnException() {
	}
	
	public NotOnlyOneKeyColumnException(String errMsg) {
		super(errMsg);
	}

	public NotOnlyOneKeyColumnException(Throwable e) {
		super(e);
	}
}
