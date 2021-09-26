package com.pugwoo.dbhelper.exception;

public class BadSQLSyntaxException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public BadSQLSyntaxException() {
	}
	
	public BadSQLSyntaxException(String errMsg) {
		super(errMsg);
	}
	
	public BadSQLSyntaxException(Throwable e) {
		super(e);
	}
}
