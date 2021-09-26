package com.pugwoo.dbhelper.exception;

/**
 * 数据行转换成Java对象时失败异常
 */
public class RowMapperFailException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public RowMapperFailException() {
	}

	public RowMapperFailException(String errMsg) {
		super(errMsg);
	}
	
	public RowMapperFailException(Throwable e) {
		super(e);
	}
}
