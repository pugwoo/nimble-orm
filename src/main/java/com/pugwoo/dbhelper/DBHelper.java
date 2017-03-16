package com.pugwoo.dbhelper;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.model.PageData;

/**
 * 2015年8月17日 18:18:57
 * @author pugwoo
 */
public interface DBHelper {
	
	/**
	 * 手动回滚@Transactional的事务。
	 * 对于已知需要回滚的动作，我更推荐主动调用让其回滚，而非抛出RuntimeException
	 */
	void rollback();
	
	/**
	 * 设置SQL执行超时的WARN log，超时时间为1秒
	 * @param timeMS 毫秒
	 */
	void setTimeoutWarningValve(long timeMS);
	
	// 几个常用的jdbcTemplate的方法，目的是用dbHelper时可以使用in (?)传入list的参数
	
	/**
	 * jdbcTemplate方式查询对象，clazz不需要Dbhelper的@Table等注解。<br>
	 * 【不会自动处理软删除记录】
	 * @param clazz 一般是Long,String等基本类型
	 * @param sql 必须是完整的sql
	 * @param args
	 * @return
	 */
	<T> T queryForObject(Class<T> clazz, String sql, Object... args);
	
	/**
	 * jdbcTemplate方式查询对象<br>
	 * 【不会自动处理软删除记录】
	 * @param sql 必须是完整的sql
	 * @param args
	 * @return
	 */
	SqlRowSet queryForRowSet(String sql, Object... args);
	
	/**
	 * jdbcTemplate方式查询对象<br>
	 * 【不会自动处理软删除记录】
	 * @param sql 必须是完整的sql
	 * @param args
	 * @return
	 */
	Map<String, Object> queryForMap(String sql, Object... args);
	
	/**
	 * 查询多列的结果<br>
	 * 【不会自动处理软删除记录】
	 * @param sql 必须是完整的sql
	 * @param args
	 * @return
	 */
	List<Map<String, Object>> queryForList(String sql, Object... args);
	
	/**
	 * 查询多列结果<br>
	 * 【不会自动处理软删除记录】
	 * @param clazz
	 * @param sql
	 * @param args
	 * @return
	 */
	<T> List<T> queryForList(Class<T> clazz, String sql, Object... args);
	
	// END
	
	/**
	 * 通过T的主键，将数据查出来并设置到T中<br>
	 * 【会自动处理软删除记录】
	 * 
	 * @param t 值设置在t中
	 * @return 存在返回true，否则返回false
	 */
	<T> boolean getByKey(T t) throws NullKeyValueException;
	
	/**
	 * 适合于只有一个Key的情况<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @param keyValue
	 * @return 如果不存在则返回null
	 */
    <T> T getByKey(Class<?> clazz, Object keyValue) throws NullKeyValueException;
    
    /**
     * 通过多个key查询对象<br>
     * 【会自动处理软删除记录】
     * @param clazz
     * @param keyValues
     * @return 返回的值是LinkedHashMap对象，按照keyValues的顺序来，但如果key不存在，那么不会再返回值的map key中
     */
    <T, K> Map<K, T> getByKeyList(Class<?> clazz, List<K> keyValues);
	
	/**
	 * 适合于只有一个或多个Key的情况<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @param keyMap
	 * @return 如果不存在则返回null
	 */
	<T> T getByKey(Class<?> clazz, Map<String, Object> keyMap) throws NullKeyValueException;
	
	/**
	 * 查询列表，没有查询条件<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPage(Class<T> clazz, int page, int pageSize);

	/**
	 * 查询列表，postSql可以带查询条件<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @param postSql 包含where关键字起的后续SQL语句，不能包含limit子句
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPage(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args);
	
	/**
	 * 计算总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @return
	 */
	<T> int getCount(Class<T> clazz);
	
	/**
	 * 计算总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @param postSql
	 * @param args
	 * @return
	 */
	<T> int getCount(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 查询列表，没有查询条件；不查询总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize);
	
	/**
	 * 查询列表，postSql可以带查询条件；不查询总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @param page 从1开始
	 * @param pageSize
	 * @param postSql 包含where关键字起的后续SQL语句
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args);
	
	/**
	 * 查询列表，查询所有记录，如果数据量大请慎用<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @return 返回不会是null
	 */
	<T> List<T> getAll(Class<T> clazz);
	
	/**
	 * 查询列表，查询所有记录，如果数据量大请慎用<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @return 返回不会是null
	 */
	<T> List<T> getAll(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 查询一条记录，如果有多条，也只返回第一条。该方法适合于知道返回值只有一条记录的情况。<br>
	 * 【会自动处理软删除记录】
	 * @param clazz
	 * @return 如果不存在则返回null
	 */
	<T> T getOne(Class<T> clazz);
	
	/**
	 * 查询一条记录，如果有多条，也只返回第一条。该方法适合于知道返回值只有一条记录的情况。<br>
	 * 【会自动处理软删除记录】
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
	 * 插入多条记录，返回数据库实际修改的条数。<br>
	 * 实际上，这个是int insert(T t)的循环遍历而已。插入性能并不会因多条而提升。<br>
	 * 【注】只插入非null的值。该方法为一个事务，要么全部插入成功，要么全部插入失败。
	 * @param list
	 * @return
	 */
	@Transactional
	int insert(List<?> list);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。<br>
	 * 【注】只插入非null的值，如要需要插入null值，则用insertWithNullWhereNotExist。
	 * whereSql是判断条件，当条件成立时，不插入；当条件不成立时，插入。
	 * @param t
	 * @param whereSql 只能是where语句（可含可不含where关键字），不能包含order/group/limit等后续语句
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
	 * @param whereSql 只能是where语句（可含可不含where关键字），不能包含order/group/limit等后续语句
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
	 * 如果t有主键，则更新值；否则插入记录。只有非null的值会更新或插入。
	 * @param t
	 * @return 返回数据库实际修改的条数
	 */
	<T> int insertOrUpdate(T t);
	
	/**
	 * 如果t有主键，则更新值；否则插入记录。包括null的值会更新或插入。
	 * @param t
	 * @return 返回数据库实际修改的条数
	 */
	<T> int insertOrUpdateWithNull(T t);
	
	/**
	 * 如果t有主键，则更新值；否则插入记录。只有非null的值会更新或插入。
	 * @param list
	 * @return 返回数据库实际修改的条数
	 */
	@Transactional
	<T> int insertOrUpdate(List<T> list);
	
	/**
	 * 如果t有主键，则更新值；否则插入记录。包括null的值会更新或插入。
	 * @param list
	 * @return 返回数据库实际修改的条数
	 */
	@Transactional
	<T> int insertOrUpdateWithNull(List<T> list);
	
	/**
	 * 全量更新指定的列表，只处理非null字段。dbList表示原来的数据，必须都带上key。<br>
	 * newList表示新的数据，可以带有key也可以没有。<br>
	 * 对于dbList有的key但是newList中没有的key，将被删除。<br>
	 * 对于dbList有的key且newList也有的key，将被更新。<br>
	 * 对于dbList没有的key，但newList中有的key，将被更新。<br>
	 * 对于dbList没有的key，但newList也没有key的对象，将被插入。<br>
	 * @param dbList 可以是null，等同于空list
	 * @param newList 不能是null，否则该方法什么都不执行
	 * @return newList成功的值，不包括dbList中删除的
	 */
	@Transactional
	<T> int insertOrUpdateFull(List<T> dbList, List<T> newList);
	
	/**
	 * 文档同insertOrUpdateFull，只是会insert or update null值
	 * @param dbList
	 * @param newList
	 * @return
	 */
	@Transactional
	<T> int insertOrUpdateFullWithNull(List<T> dbList, List<T> newList);
		
	/**
	 * 更新单个实例数据库记录，必须带上object的key，包含更新null值的字段
	 * @param t
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int updateWithNull(T t) throws NullKeyValueException;
	
	/**
	 * 带条件的更新单个对象，必须带上object的key，主要用于mysql的update ... where ...这样的CAS修改
	 * 
	 * @param t
	 * @param postSql where及后续的sql，包含where关键字
	 * @param args
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int updateWithNull(T t, String postSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 更新单条数据库记录,必须带上object的key。【只更新非null字段】
	 * @param t
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int update(T t) throws NullKeyValueException;
	
	/**
	 * 更新单条数据库记录,必须带上object的key，主要用于mysql的update ... where ...这样的CAS修改。
	 * 【只更新非null字段】
	 * 
	 * @param t
	 * @param postSql where及后续的sql，包含where关键字
	 * @param args
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException
	 */
	<T> int update(T t, String postSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 自定义set字句更新，一般用于单个sql进行值更新，例如set reads = reads + 1这种情况。因此不提供CAS功能。
	 * @param t 必须提供key
	 * @param setSql 【不】包含set关键字，多个则用逗号隔开，例如a=a+1,c=b
	 * @param args set子句的参数
	 * @return
	 * @throws NullKeyValueException 当t没有带上key时，抛出该异常
	 */
	<T> int updateCustom(T t, String setSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 更新数据库记录，更新包含null的字段，返回数据库实际修改条数。
	 * 【注】批量更新的方法并不会比程序中循环调用int updateNotNull(T t)更快
	 * @param list
	 * @return
	 * @throws NullKeyValueException
	 */
	@Transactional
	<T> int updateWithNull(List<T> list) throws NullKeyValueException;
	
	/**
	 * 更新数据库记录，返回数据库实际修改条数。
	 * 【注】批量更新的方法并不会比程序中循环调用int update(T t)更快
	 * 【只更新非null字段】
	 * @param list
	 * @return
	 * @throws NullKeyValueException
	 */
	@Transactional
	<T> int update(List<T> list) throws NullKeyValueException;
	
	/**
	 * 删除数据库记录，返回数据库实际修改条数。
	 * 该操作【会】自动使用软删除进行删除
	 * 
	 * @param t
	 * @return
	 */
	<T> int deleteByKey(T t) throws NullKeyValueException;
	
	/**
	 * 删除数据库记录，返回实际修改数据库条数，这个接口只支持单个字段是key的情况。
	 * 该操作【会】自动使用软删除进行删除
	 * 
	 * @param clazz
	 * @param keyValue
	 * @return
	 * @throws NullKeyValueException
	 */
	<T> int deleteByKey(Class<?> clazz, Object keyValue) throws NullKeyValueException;

	/**
	 * 自定义条件删除数据，该操作【会】自动使用软删除标记
	 * @param clazz
	 * @param postSql 必须提供，必须写where，不允许留空
	 * @param args
	 * @return
	 */
	<T> int delete(Class<T> clazz, String postSql, Object... args);
	
}
