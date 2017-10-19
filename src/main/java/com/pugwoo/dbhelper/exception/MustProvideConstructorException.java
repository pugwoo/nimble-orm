package com.pugwoo.dbhelper.exception;

/**
 * deleteByKey的class必须有默认构造方法
 */
public class MustProvideConstructorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MustProvideConstructorException() {
		
	}
	
	public MustProvideConstructorException(String errmsg) {
		super(errmsg);
	}
}
