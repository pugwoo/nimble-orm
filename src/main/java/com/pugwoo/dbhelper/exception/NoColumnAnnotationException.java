package com.pugwoo.dbhelper.exception;

public class NoColumnAnnotationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoColumnAnnotationException() {
		
	}
	
	public NoColumnAnnotationException(String errmsg) {
		super(errmsg);
	}

}
