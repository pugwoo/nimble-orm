package com.pugwoo.dbhelper.exception;

public class NoTableAnnotationException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public NoTableAnnotationException() {
	}
	
	public NoTableAnnotationException(String errMsg) {
		super(errMsg);
	}

	public NoTableAnnotationException(Throwable e) {
		super(e);
	}
}
