package com.pugwoo.dbhelper.exception;

/**
 * 对于@JoinTable的VO，没有声明@JoinLeftTable和@JoinRightTable是抛出
 * @author pugwoo
 */
public class NoJoinTableMemberException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NoJoinTableMemberException() {
		
	}
	
	public NoJoinTableMemberException(String errmsg) {
		super(errmsg);
	}
	
}
