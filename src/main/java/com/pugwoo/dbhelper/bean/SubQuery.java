package com.pugwoo.dbhelper.bean;

/**
 * 纯子查询参数，用于传入到SQL的查询参数in (?)中，将会
 * 自动构造子查询 (select 字段 from (select * from 表格 where子句))。
 * 该方式不支持引用了父表的子查询，一般这种子查询效率也非常慢，不推荐使用。
 */
public class SubQuery {

	private String field;
	
	private Class<?> clazz;
	
	private String postSql;
	
	private Object[] args;

	public SubQuery(String field, Class<?> clazz, String postSql, Object... args) {
		this.field = field;
		this.clazz = clazz;
		this.postSql = postSql;
		this.args = args;
	}
	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public String getPostSql() {
		return postSql;
	}

	public void setPostSql(String postSql) {
		this.postSql = postSql;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
	
}
