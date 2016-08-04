package com.pugwoo.dbhelper;

import java.util.List;
import java.util.Map;

import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.model.PageData;

/**
 * 2015年8月17日 18:18:57
 * @author pugwoo
 */
public interface DBHelper {
	
	/**
	 * 设置SQL执行超时的WARN log，超时时间为1秒
	 * @param timeMS 毫秒
	 */
	void setTimeoutWarningValve(long timeMS);
	
	/**
	 * 通过T的主键，将数据查出来并设置到T中
	 * @param t 值设置在t中
	 * @return 存在返回true，否则返回false
	 */
	<T> boolean getByKey(T t) throws NullKeyValueException;
	
	/**
	 * 适合于只有一个Key的情况
	 * @param clazz
	 * @param key
	 * @return 如果不存在则返回null
	 */
    <T> T getByKey(Class<?> clazz, Object keyValue) throws NullKeyValueException;
	
	/**
	 * 适合于只有一个或多个Key的情况
	 * @param clazz
	 * @param keyMap
	 * @return 如果不存在则返回null
	 */
	<T> T getByKey(Class<?> clazz, Map<String, Object> keyMap) throws NullKeyValueException;
	
	/**
	 * 查询列表，没有查询条件
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPage(Class<T> clazz, int page, int pageSize);

	/**
	 * 查询列表，postSql可以带查询条件
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @param postSql 包含where关键字起的后续SQL语句
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPage(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args);
	
	/**
	 * 查询列表，没有查询条件；不查询总数
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize);
	
	/**
	 * 查询列表，postSql可以带查询条件；不查询总数
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @param postSql 包含where关键字起的后续SQL语句
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args);
	
	/**
	 * 查询列表，查询所有记录，如果数据量大请慎用
	 * @param clazz
	 * @return 返回不会是null
	 */
	<T> List<T> getAll(Class<T> clazz);
	
	/**
	 * 查询列表，查询所有记录，如果数据量大请慎用
	 * @param clazz
	 * @return 返回不会是null
	 */
	<T> List<T> getAll(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 查询一条记录，如果有多条，也只返回第一条。该方法适合于知道返回值只有一条记录的情况。
	 * @param clazz
	 * @return 如果不存在则返回null
	 */
	<T> T getOne(Class<T> clazz);
	
	/**
	 * 查询一条记录，如果有多条，也只返回第一条。该方法适合于知道返回值只有一条记录的情况。
	 * @param clazz
	 * @param postSql
	 * @param args
	 * @return 如果不存在则返回null
	 */
	<T> T getOne(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。<br>
	 * 【注】只插入非null的值，如要需要插入null值，则用insertWithNull。
	 * @param t
	 * @return
	 */
	<T> int insert(T t);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。<br>
	 * 【注】只插入非null的值，如要需要插入null值，则用insertWithNullWhereNotExist。
	 * whereSql是判断条件，当条件成立时，不插入；当条件不成立时，插入。
	 * @param t
	 * @param whereSql 不含where关键字，不能包含order/group/limit等后续语句
	 * @param args
	 * @return
	 */
	<T> int insertWhereNotExist(T t, String whereSql, Object... args);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。
	 * @param t
	 * @return
	 */
	<T> int insertWithNull(T t);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。<br>
	 * whereSql是判断条件，当条件成立时，不插入；当条件不成立时，插入。
	 * @param t
	 * @param whereSql 不含where关键字，不能包含order/group/limit等后续语句
	 * @param args
	 * @return
	 */
	<T> int insertWithNullWhereNotExist(T t, String whereSql, Object... args);
	
	/**
	 * 插入几条数据，通过拼凑成一条sql插入
	 *【重要】批量插入不支持回设自增id。批量插入会把所有属性都插入，不支持只插入非null的值。
	 * (说明:这个方法之前叫insertInOneSQL)
	 * @param list
	 * @return 返回影响的行数
	 */
	<T> int insertWithNullInOneSQL(List<T> list);
		
	/**
	 * 更新单个实例数据库记录，必须带上object的key，只更新非null的字段
	 * @param t
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int updateNotNull(T t) throws NullKeyValueException;
	
	/**
	 * 带条件的更新单个对象，必须带上object的key，主要用于mysql的update ... where ...这样的CAS修改
	 * 
	 * @param t
	 * @param postSql where及后续的sql，包含where关键字
	 * @param args
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int updateNotNull(T t, String postSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 更新单条数据库记录,必须带上object的key
	 * @param t
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int update(T t) throws NullKeyValueException;
	
	/**
	 * 更新单条数据库记录,必须带上object的key，主要用于mysql的update ... where ...这样的CAS修改
	 * @param t
	 * @param postSql where及后续的sql，包含where关键字
	 * @param args
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int update(T t, String postSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 更新数据库记录，只更新非null的字段，返回数据库实际修改条数。
	 * 【注】批量更新的方法并不会比程序中循环调用int updateNotNull(T t)更快
	 * @param t
	 * @return
	 * @throws NullKeyValueException
	 */
	<T> int updateNotNull(List<T> list) throws NullKeyValueException;
	
	/**
	 * 更新数据库记录，返回数据库实际修改条数。
	 * 【注】批量更新的方法并不会比程序中循环调用int update(T t)更快
	 * @param t
	 * @return
	 * @throws NullKeyValueException
	 */
	<T> int update(List<T> list) throws NullKeyValueException;
	
	/**
	 * 删除数据库记录，返回数据库实际修改条数
	 * @param t
	 * @return
	 */
	<T> int deleteByKey(T t) throws NullKeyValueException;
	
	/**
	 * 自定义条件删除数据
	 * @param clazz
	 * @param postSql 必须提供，必须写where
	 * @param args
	 * @return
	 */
	<T> int delete(Class<T> clazz, String postSql, Object... args);
}
