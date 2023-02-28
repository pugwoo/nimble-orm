package com.pugwoo.dbhelper.exception;

public class NotAllowModifyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotAllowModifyException() {
	}

	public NotAllowModifyException(String errMsg) {
		super(errMsg);
	}

	public NotAllowModifyException(Throwable e) {
		super(e);
	}
}
