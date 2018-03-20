package com.pugwoo.dbhelper.exception;

public class BadSQLSyntaxException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public BadSQLSyntaxException() {
		
	}
	
	public BadSQLSyntaxException(String errmsg) {
		super(errmsg);
	}
	
	public BadSQLSyntaxException(Throwable e) {
		super(e);
	}
}
