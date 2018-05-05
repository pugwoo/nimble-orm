package com.pugwoo.dbhelper;

import java.util.List;

public interface IDBHelperSlowSqlCallback {

	/**
	 * 超时sql回调
	 * @param executeMsTime 执行毫米数
	 * @param sql 执行sql
	 * @param args 执行sql参数，如果没有则为空数组
	 */
	void callback(long executeMsTime, String sql, List<Object> args);
	
}
