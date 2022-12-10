package com.pugwoo.dbhelper.exception;

public class NoColumnAnnotationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NoColumnAnnotationException() {
	}
	
	public NoColumnAnnotationException(String errMsg) {
		super(errMsg);
	}

	public NoColumnAnnotationException(Throwable e) {
		super(e);
	}

}
