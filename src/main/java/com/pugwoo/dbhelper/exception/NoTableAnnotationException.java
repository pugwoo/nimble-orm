package com.pugwoo.dbhelper.exception;

public class NoTableAnnotationException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoTableAnnotationException() {
		
	}
	
	public NoTableAnnotationException(String errmsg) {
		super(errmsg);
	}

}
