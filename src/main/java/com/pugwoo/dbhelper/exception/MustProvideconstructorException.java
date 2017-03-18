package com.pugwoo.dbhelper.exception;

/**
 * deleteByKey的class必须有默认构造方法
 */
public class MustProvideconstructorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MustProvideconstructorException() {
		
	}
	
	public MustProvideconstructorException(String errmsg) {
		super(errmsg);
	}
}
