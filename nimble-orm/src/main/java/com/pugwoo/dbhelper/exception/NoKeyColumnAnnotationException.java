package com.pugwoo.dbhelper.exception;

public class NoKeyColumnAnnotationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NoKeyColumnAnnotationException() {
	}
	
	public NoKeyColumnAnnotationException(String errMsg) {
		super(errMsg);
	}

	public NoKeyColumnAnnotationException(Throwable e) {
		super(e);
	}
}
