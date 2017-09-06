package com.pugwoo.dbhelper;

import java.util.List;

/**
 * 2017年9月5日 11:12:00
 * 数据库操作拦截器，拦截器的命名为[before|after][select|update|insert|delete]，拦截器将影响全局，请谨慎使用。
 * 拦截器是栈式的，按拦截器的顺序，before顺序调用，after逆序调用。
 * 
 * 关于拦截器的设计想法：
 * 1. 查询拦截器，用于数据安全拦截和数据查询审计。查询拦截器单独的获得总数count进行拦截，只拦截查询行记录。
 * 2. 
 * @author pugwoo
 */
public class DBHelperInterceptor {

	/**
	 * select前执行
	 * @param clazz 查询的对象
	 * @param sql 查询的完整sql
	 * @param args 查询的完整参数。理论上，拦截器就有可能修改args里面的object的值的，请小心。
	 * @return 返回true，则查询继续; 返回false将终止查询并抛出NotAllowQueryException
	 */
	public boolean beforeSelect(Class<?> clazz, String sql, Object[] args) {
		return true;
	}
	
	/**
	 * select后执行
	 * @param clazz 查询的对象 
	 * @param result 查询结果值，对于返回值是一个的，也放入该list中。对于没有的，这里会传入空list。
	 * @param count 当查询总数或有分页总数时，该数有值。该值为-1时，表示未知总数。
	 * @param sql 查询的完整sql
	 * @param args 查询的完整参数
	 * @return DBHelper会使用返回值作为新的查询结果值，因此，没修改时请务必将result返回。
	 *         对于机密的数据，请直接设置result的对象属性为null。
	 */
	public <T> List<T> afterSelect(Class<?> clazz, List<T> result, int count,
			String sql, Object args[]) {
		return result;
	}
	
	/**
	 * insert前执行
	 * @param clazz 查询的对象
	 * @param list 插入的对象列表。理论上，可以修改list中的元素，请小心。
	 * @return
	 */
	public boolean beforeInsert(Class<?> clazz, List<Object> list) {
		return true;
	}
	
	/**
	 * insert后执行
	 * @param clazz 查询的对象
	 * @param list 插入的对象列表，对于有自增id的值，返回的list已经设置上了主键。
	 * @param affectedRows 实际影响的数据库条数
	 */
	public void afterInsert(Class<?> clazz, List<Object> list, int affectedRows) {
	}
	
	// 对于更新拦截器，提供两种类型：1. 只知道现在要更新的对象的值。2. 还知道现在数据库里面的值，多一次查询。
}
