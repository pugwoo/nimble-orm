package com.pugwoo.dbhelper.exception;

public class InvalidParameterException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidParameterException() {
	}
	
	public InvalidParameterException(String errmsg) {
		super(errmsg);
	}

	public InvalidParameterException(Throwable e) {
		super(e);
	}
}
