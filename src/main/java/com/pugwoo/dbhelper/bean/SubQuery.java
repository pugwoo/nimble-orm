package com.pugwoo.dbhelper.bean;

/**
 * 纯子查询参数，用于传入到SQL的查询参数in (?)中，将会
 * 自动构造子查询 (select 字段 from (select * from 表格 where子句))。
 * 该方式不支持引用了父表的子查询，一般这种子查询效率也非常慢，不推荐使用。
 * 
 * SubQuery支持参数还是SubQuery的情况。
 * 
 * 注意：当子查询条件中出现了Group By子句，请确保数据库关闭了ONLY_FULL_GROUP_BY。
 * 
 * ref: https://stackoverflow.com/questions/6135376/mysql-select-where-field-in-subquery-extremely-slow-why
 */
public class SubQuery {

	/**查询字段，仅支持一个*/
	private String field;
	
	private Class<?> clazz;
	
	private String postSql;
	
	private Object[] args;

	/**
	 * 
	 * @param field 仅支持一个字段，可以是计算列，当该字段名称和sql关键字相同时，请自行加上``
	 * @param clazz 
	 * @param postSql
	 * @param args
	 */
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
