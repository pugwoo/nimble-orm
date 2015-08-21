package com.pugwoo.dbhelper.exception;

public class NotSupportMethodException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NotSupportMethodException() {
		
	}
	
	public NotSupportMethodException(String errmsg) {
		super(errmsg);
	}

}
