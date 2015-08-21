package com.pugwoo.dbhelper;

import java.util.List;
import java.util.Map;

import com.pugwoo.dbhelper.model.PageData;

/**
 * 2015年8月17日 18:18:57
 */
public interface DBHelper {

	/**
	 * 
	 * @param t 值设置在t中
	 * @return 存在返回true，否则返回false
	 */
	public <T> boolean getByKey(T t);
	
	/**
	 * 适合于只有一个Key的情况
	 * @param clazz
	 * @param key
	 * @return
	 */
	public <T> T getByKey(Class<?> clazz, Object keyValue);
	
	/**
	 * 适合于只有一个或多个Key的情况
	 * @param clazz
	 * @param keyMap
	 * @return
	 */
	public <T> T getByKey(Class<?> clazz, Map<String, Object> keyMap);
	
	/**
	 * 查询列表，没有查询条件
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @return
	 */
	public <T> PageData<T> getPage(Class<T> clazz, int page, int pageSize);
	
	/**
	 * 查询列表，查询所有记录
	 * @param clazz
	 * @return
	 */
	public <T> List<T> getAll(Class<T> clazz);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。
	 * 
	 * @param t
	 * @return
	 */
	public <T> int insert(T t);
	
	/**
	 * 插入几条数据，通过拼凑成一条sql插入
	 *【注】批量插入不支持回设自增id。
	 * 
	 * @param list
	 * @return 返回影响的行数
	 */
	public <T> int insertInOneSQL(List<T> list);
	
}
