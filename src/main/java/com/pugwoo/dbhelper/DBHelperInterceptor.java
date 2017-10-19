package com.pugwoo.dbhelper;

import java.util.List;

/**
 * 2017年9月5日 11:12:00
 * 数据库操作拦截器，拦截器的命名为[before|after][select|update|insert|delete]，拦截器将影响全局，请谨慎使用。
 * 拦截器是栈式的，按拦截器的顺序，before顺序调用，after逆序调用。
 * 
 * 关于拦截器的设计想法：
 * 1. 查询拦截器，用于数据安全拦截和数据查询审计。
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
	 * @param sql 查询的完整sql
	 * @param args 查询的完整参数
	 * @param result 查询结果值，对于返回值是一个的，也放入该list中。对于没有的，这里会传入空list。
	 * @param count 当查询总数或有分页总数时，该数有值。该值为-1时，表示未知总数。
	 * @return DBHelper会使用返回值作为新的查询结果值，因此，没修改时请务必将result返回。
	 *         对于机密的数据，请直接设置result的对象属性为null。
	 */
	public <T> List<T> afterSelect(Class<?> clazz, String sql, Object args[],
			List<T> result, int count) {
		return result;
	}
	
	/**
	 * insert前执行
	 * @param clazz 查询的对象
	 * @param list 插入的对象列表。理论上，可以修改list中的元素，请小心；但删除list元素则不一定会影响insert。
	 * @return 返回true继续执行，返回false中断执行并抛出NotAllowQueryException
	 */
	public <T> boolean beforeInsert(Class<?> clazz, List<T> list) {
		return true;
	}
	
	/**
	 * insert后执行
	 * @param clazz 查询的对象
	 * @param list 插入的对象列表，对于有自增id的值，返回的list已经设置上了主键。
	 * @param affectedRows 实际影响的数据库条数。注意list可能有值而affectedRows数据小于list个数，说明有的没有插入到数据库
	 */
	public <T> void afterInsert(Class<?> clazz, List<T> list, int affectedRows) {
	}
	
	// 对于更新拦截器，只提供当前要更新的值。由于更新提供了setSql和customSetSql,所以提供了多个接口来拦截。
	// 这意味着，拦截器提供了2个

	/**
	 * update前执行
	 * @param clazz 更新的类
	 * @param t 更新的实例
	 * @return 返回true继续执行，返回false中断执行并抛出NotAllowQueryException
	 */
    public <T> boolean beforeUpdate(Class<?> clazz, T t) {
    	return true;
    }
    
    /**
     * update前执行
     * @param clazz 更新的类
     * @param sql 自定义更新的update sql，完整的sql
     * @param args sql的参数
     * @return 返回true继续执行，返回false中断执行并抛出NotAllowQueryException
     */
    public <T> boolean beforeUpdateCustom(Class<?> clazz, String sql, Object[] args) {
    	return true;
    }
    
    /**
     * update后执行
     * @param affectedRows 实际修改数据库条数
     */
    public <T> void afterUpdate(Class<?> clazz, T t, int affectedRows) {
    }
    
    /**
     * update后执行
     * @param affectedRows 实际修改数据库条数
     */
    public <T> void afterUpdateCustom(Class<?> clazz, String sql, Object[] args, int affectedRows) {
    }
    
    // 删除相关的
    
    public <T> boolean beforeDelete(Class<?> clazz, T t) {
    	return true;
    }
    
    public <T> boolean beforeDeleteCustom(Class<?> clazz, String sql, Object[] args) {
    	return true;
    }
    
    public <T> void afterDelete(Class<?> clazz, T t, int affectedRows) {
    }
    
    public <T> void afterDeleteCustom(Class<?> clazz, String sql, Object[] args, int affectedRows) {
    }
    
}
