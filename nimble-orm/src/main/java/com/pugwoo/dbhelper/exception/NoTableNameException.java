package com.pugwoo.dbhelper.exception;

public class NoTableNameException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public NoTableNameException() {
	}

	public NoTableNameException(String errMsg) {
		super(errMsg);
	}

	public NoTableNameException(Throwable e) {
		super(e);
	}
}
