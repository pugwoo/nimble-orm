package com.pugwoo.dbhelper.exception;

/**
 * sql参数个数不一致异常
 */
public class ParameterSizeNotMatchedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ParameterSizeNotMatchedException() {
	}
	
	public ParameterSizeNotMatchedException(String errmsg) {
		super(errmsg);
	}

}
