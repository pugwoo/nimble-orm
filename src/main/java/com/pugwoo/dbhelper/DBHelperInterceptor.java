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
	 * select前执行。不会拦截getCount计算总数和getAllKey只查询key这2个接口。
	 * @param clazz 查询的对象
	 * @param sql 查询的完整sql
	 * @param args 查询的完整参数。理论上，拦截器就有可能修改args里面的object的值的，请小心。不建议修改args的值。
	 * @return 返回true，则查询继续; 返回false将终止查询并抛出NotAllowQueryException
	 */
	public boolean beforeSelect(Class<?> clazz, String sql, List<Object> args) {
		return true;
	}
	
	/**
	 * select后执行。不会拦截getCount计算总数和getAllKey只查询key这2个接口。
	 * @param clazz 查询的对象，这个参数之所以是必须的，因为查询结果可能为空，此时便获取不到clazz
	 * @param sql 查询的完整sql
	 * @param args 查询的完整参数
	 * @param result 查询结果值，对于返回值是一个的，也放入该list中。对于没有的，这里会传入空list。
	 * @param count 当查询总数或有分页总数时，该数有值。该值为-1时，表示未知总数。
	 * @return DBHelper会使用返回值作为新的查询结果值，因此，没修改时请务必将result返回。
	 *         对于机密的数据，请直接设置result的对象属性为null。
	 */
	public <T> List<T> afterSelect(Class<?> clazz, String sql, List<Object> args,
			List<T> result, int count) {
		return result;
	}
	
	/**
	 * insert前执行
	 * @param list 插入的对象列表。可以修改list中的元素，请小心操作；但删除list元素则不一定会影响insert。
	 * @return 返回true继续执行，返回false中断执行并抛出NotAllowQueryException
	 */
	public boolean beforeInsert(List<Object> list) {
		return true;
	}
	
	/**
	 * insert后执行
	 * @param list 插入的对象列表，对于有自增id的值，返回的list已经设置上了主键。
	 * @param affectedRows 实际影响的数据库条数。注意list可能有值而affectedRows数据小于list个数，说明有的没有插入到数据库
	 */
	public void afterInsert(List<Object> list, int affectedRows) {
	}
	
	// 对于更新拦截器，只提供当前要更新的值。由于更新有自写set语句的接口，因此提供两个beforeUpdate

	/**
	 * update前执行
	 * @param clazz 更新的类
	 * @param t 更新的实例
	 * @param setSql 如果使用了updateCustom方法，传入的setSql将传入。否则该值为null
	 * @param setSqlArgs 如果使用了updateCustom方法，传入的args将传入，否则该值为null。
	 *        注意，修改此值会修改实际被设置的值，谨慎!
	 * @return 返回true继续执行，返回false中断执行并抛出NotAllowQueryException
	 */
    public boolean beforeUpdate(List<Object> tList, String setSql, List<Object> setSqlArgs) {
    	return true;
    }
    
    /**
     * update前执行
     * @param clazz 更新的类
     * @param sql 自定义更新的update sql，完整的sql
     * @param args sql的参数,理论上可以修改到args的值，请小心操作。
     * @param customsSets 允许拦截器自行增加若干set语句，每个语句一个，不需要带set关键字，例如a=?
     * @param customsParams 允许拦截器自行添加若干set语句，这里是对应的参数
     * @return 返回true继续执行，返回false中断执行并抛出NotAllowQueryException
     */
    public boolean beforeUpdateCustom(Class<?> clazz, String sql,
    		List<String> customsSets, List<Object> customsParams,
    		List<Object> args) {
    	return true;
    }
    
    /**
     * update后执行
     * @param affectedRows 实际修改数据库条数
     */
    public void afterUpdate(List<Object> tList, int affectedRows) {
    }
    
    // 删除相关的
    
    /**
     * delete前执行，包括软删除和物理删除
     * @param clazz
     * @param t
     * @return 返回true继续执行，返回false中断执行并抛出NotAllowQueryException
     */
    public boolean beforeDelete(List<Object> tList) {
    	return true;
    }

    /**
     * delete后执行
     * @param affectedRows 实际修改数据库条数
     */
    public void afterDelete(List<Object> tList, int affectedRows) {
    }

}
