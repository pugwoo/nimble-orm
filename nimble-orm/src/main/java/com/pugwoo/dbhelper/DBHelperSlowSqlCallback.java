package com.pugwoo.dbhelper;

import java.util.List;

/**
 * 特别说明：在回调方法callback，尽量不使用dbhelper及其关联的service，以免Spring循环依赖。
 * 如确实需要依赖dbhelper及其关联的service，可以考虑使用懒加载的方式。
 */
public interface DBHelperSlowSqlCallback {

	/**
	 * 超时sql回调，可以通过SQLAssemblyUtils工具获得真正执行的SQL
	 * @param executeMsTime 执行毫秒数
	 * @param sql 执行sql
	 * @param args 执行sql参数，如果没有则为空数组；另外如果是getRaw和executeRaw的map param参数，则传入的第一个元素是map
	 * @param batchSize 当大于0时是批量操作模式，此时args一般只会给第一行
	 */
	void callback(long executeMsTime, String sql, List<Object> args, int batchSize);
	
}
