package com.pugwoo.dbhelper.exception;

import java.lang.reflect.Field;

/**
 * 数据行转换成Java对象时失败异常
 */
public class RowMapperFailException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	// 处理失败的field
	private Field field;
	
	public RowMapperFailException() {
	}

	public RowMapperFailException(String errMsg) {
		super(errMsg);
	}
	
	public RowMapperFailException(Throwable e) {
		super(e);
	}

	public RowMapperFailException(Throwable e, Field field) {
		super(e);
		this.field = field;
	}

	public Field getField() {
		return field;
	}

}
