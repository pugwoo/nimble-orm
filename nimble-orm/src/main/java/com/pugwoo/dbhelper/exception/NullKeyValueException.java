package com.pugwoo.dbhelper.exception;

/**
 * 查询时，主键的值是null时抛异常
 */
public class NullKeyValueException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NullKeyValueException() {
	}
	
	public NullKeyValueException(String errMsg) {
		super(errMsg);
	}

	public NullKeyValueException(Throwable e) {
		super(e);
	}
}
