package com.pugwoo.dbhelper.exception;

public class OnConditionIsNeedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OnConditionIsNeedException() {
	}
	
	public OnConditionIsNeedException(String errmsg) {
		super(errmsg);
	}

	public OnConditionIsNeedException(Throwable e) {
		super(e);
	}
	
}
