package com.pugwoo.dbhelper;

import java.util.List;

/**
 * 特别说明：在回调方法callback，尽量不使用dbhelper及其关联的service，以免Spring循环依赖。
 * 如确实需要依赖dbhelper及其关联的service，可以考虑使用懒加载的方式。
 */
public interface IDBHelperSlowSqlCallback {

	/**
	 * 超时sql回调
	 * @param executeMsTime 执行毫米数
	 * @param sql 执行sql
	 * @param args 执行sql参数，如果没有则为空数组；另外如果是getRaw和executeRaw的map param参数，则传入的第一个元素是map
	 */
	void callback(long executeMsTime, String sql, List<Object> args);
	
}
