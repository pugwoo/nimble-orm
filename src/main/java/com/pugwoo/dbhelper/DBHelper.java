package com.pugwoo.dbhelper;

import com.pugwoo.dbhelper.exception.MustProvideConstructorException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.model.PageData;

import java.util.List;
import java.util.Map;

/**
 * 2015年8月17日 18:18:57
 * @author pugwoo
 */
public interface DBHelper {
	
	/**
	 * 手动回滚@Transactional的事务。
	 * 对于已知需要回滚的动作，我更推荐主动调用让其回滚，而非抛出RuntimeException。
	 * 当前如果没有事务而调用该方法，将抛出org.springframework.transaction.NoTransactionException。<br><br>
	 * 
	 * 对于手动回滚事务rollback抛出`org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only`
	 * 异常的情况，需要使用者保证满足以下两点中的任意一点：<br>
	 * 1) 调用rollback的代码所在的方法是注解了@Transactional最外层的方法；<br>
	 * 2) 调用rollback的代码最近的@Transactional注解加上propagation = Propagation.NESTED属性。<br>
	 */
	void rollback();
	
	/**
	 * 当前事务提交后执行
	 * @param runnable 执行的回调方法
	 * @return 当提交成功返回true，提交失败返回false；如果当前不在事务中时，返回false
	 */
	boolean executeAfterCommit(Runnable runnable);
	
	/**
	 * 设置SQL执行超时的WARN log，超时时间默认为1秒
	 * @param timeMS 毫秒
	 */
	void setTimeoutWarningValve(long timeMS);
	
	/**
	 * 设置允许的每页最大的个数，当页数超过允许的最大页数时，设置为最大页数。
	 * 默认对每页最大个数没有限制，该限制只对getPage和getPageWithoutCount接口生效。
	 * @param maxPageSize 允许的每页最大的个数
	 */
	void setMaxPageSize(int maxPageSize);
	
	/**
	 * 设置SQL执行超时回调，可用于自行实现将慢sql存放到db
	 * @param callback 执行的回调方法
	 */
	void setTimeoutWarningCallback(IDBHelperSlowSqlCallback callback);
	
	/**
	 * 数据库拦截器
	 * @param interceptors 拦截器列表，全量更新
	 */
	void setInterceptors(List<DBHelperInterceptor> interceptors);

	// ===============Query methods START ==================================
	
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
	 * @param clazz 查询的DO的类class
	 * @param keyValue 查询的主键key值
	 * @return 如果不存在则返回null
	 */
    <T> T getByKey(Class<T> clazz, Object keyValue) throws NullKeyValueException;
    
    /**
     * 通过多个key查询对象<br>
     * 【会自动处理软删除记录】
     * @param clazz 查询的DO的类class
     * @param keyValues 查询的主键key值列表
     * @return 返回的值是LinkedHashMap对象，按照keyValues的顺序来，但如果key不存在，那么不会再返回值的map key中
     */
    <T, K> Map<K, T> getByKeyList(Class<T> clazz, List<K> keyValues);
	
	/**
	 * 查询列表，没有查询条件<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @param page 从1开始
	 * @param pageSize 每页查询个数
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPage(Class<T> clazz, int page, int pageSize);

	/**
	 * 查询列表，postSql可以带查询条件<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @param page 从1开始
	 * @param pageSize 每页查询个数
	 * @param postSql 包含where关键字起的后续SQL语句，【不能】包含limit子句
	 * @param args postSql中的参数列表
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPage(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args);

	/**
	 * 计算总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @return 总数
	 */
	<T> int getCount(Class<T> clazz);
	
	/**
	 * 计算总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @param postSql 包含where关键字起的后续SQL语句
	 * @param args postSql中的参数列表
	 * @return 总数
	 */
	<T> int getCount(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 查询列表，没有查询条件；不查询总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @param page 从1开始
	 * @param pageSize 每页查询个数
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize);
	
	/**
	 * 查询列表，postSql可以带查询条件；不查询总数<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @param page 从1开始
	 * @param pageSize 每页查询个数
	 * @param postSql 包含where关键字起的后续SQL语句
	 * @return 返回的data不会是null
	 */
	<T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args);
	
	/**
	 * 查询列表，查询所有记录，如果数据量大请慎用<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @return 返回不会是null
	 */
	<T> List<T> getAll(Class<T> clazz);
	
	/**
	 * 查询列表，查询所有记录，postSql指定查询where及order by limit等后续语句。<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @param postSql where及后续语句，可包含order by,group by,limit等语句
	 * @return 返回不会是null
	 */
	<T> List<T> getAll(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 查询列表，但只查询主键出来，postSql指定查询where及order by limit等后续语句。<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @param postSql where及后续语句，可包含order by,group by,limit等语句
	 * @return 返回不会是null
	 */
	<T> List<T> getAllKey(Class<T> clazz, String postSql, Object... args);

	/**
	 * 查询一条记录，如果有多条，也只返回第一条。该方法适合于知道返回值只有一条记录的情况。<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 【-支持@JoinTable-】
	 * @return 如果不存在则返回null
	 */
	<T> T getOne(Class<T> clazz);
	
	/**
	 * 查询一条记录，如果有多条，也只返回第一条。该方法适合于知道返回值只有一条记录的情况。<br>
	 * 【会自动处理软删除记录】
	 * @param clazz 查询的DO类【-支持@JoinTable-】
	 * @param postSql where及后续语句，可包含order by,group by等语句
	 * @param args postSql中的参数列表
	 * @return 如果不存在则返回null
	 */
	<T> T getOne(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 是否出现至少一条记录
	 * @param clazz 查询的DO类
	 * @param postSql 不能有limit子句
	 * @param args postSql中的参数列表
	 * @return 如果存在则返回true，否则返回false
	 */
	<T> boolean isExist(Class<T> clazz, String postSql, Object... args);
	
	/**
	 * 是否出现至少N条记录(含N条)
	 * @param atLeastCounts 至少有N条记录（isExist方法等级于atLeastCounts=1）
	 * @param clazz 查询的DO类
	 * @param postSql 不能有limit子句
	 * @param args postSql中的参数列表
	 * @return 如果存在则返回true，否则返回false
	 */
	<T> boolean isExistAtLeast(int atLeastCounts, Class<T> clazz,
			String postSql, Object... args);
	
	/**
	 * 单独抽离出处理RelatedColumn的类，参数t不需要@Table的注解了
	 * @param t 需要处理RelatedColumn的对象
	 */
	<T> void handleRelatedColumn(T t);
	
	/**
	 * 单独抽离出处理RelatedColumn的类，参数list的元素不需要@Table的注解了。但要求list都同一class类型的对象。
	 * @param list 需要处理RelatedColumn的对象列表
	 */
	<T> void handleRelatedColumn(List<T> list);
	
	/**
	 * 单独抽离出处理RelatedColumn的类，参数t不需要@Table的注解了
	 * @param t 需要处理RelatedColumn的对象
	 * @param relatedColumnProperties 只处理制定的这些RelatedColumn注解的成员变量，这个的值是成员变量的名称
	 */
	<T> void handleRelatedColumn(T t, String... relatedColumnProperties);
	
	/**
	 * 单独抽离出处理RelatedColumn的类，参数list的元素不需要@Table的注解了。但要求list都同一class类型的对象。
	 * @param list 需要处理RelatedColumn的对象列表
	 * @param relatedColumnProperties 只处理制定的这些RelatedColumn注解的成员变量，这个的值是成员变量的名称
	 */
	<T> void handleRelatedColumn(List<T> list, String... relatedColumnProperties);
	
	// ===============Query methods END ==================================
	
	/**
	 * 插入一条记录<br>
	 * 如果包含了自增id，则自增Id会被设置。<br>
	 * 【注】只插入非null的值，如要需要插入null值，则用insertWithNull。
	 * @param t 需要插入的DO对象实例
	 * @return 实际修改的条数
	 */
	<T> int insert(T t);
	
	/**
	 * 插入多条记录，返回数据库实际修改的条数。<br>
	 * 实际上，这个是int insert(T t)的循环遍历而已。插入性能并不会因多条而提升。<br>
	 * 【注】只插入非null的值。该方法为一个事务，要么全部插入成功，要么全部插入失败。<br>
	 * 【特别说明】因为主键和拦截器方面的设计，dbhelper并没有提供真正批量插入的方式。对于插入性能要求高的场景，请以消息队列辅助消峰。
	 * @param list 需要插入的DO对象实例列表
	 * @return 实际修改的条数
	 */
	int insert(List<?> list);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。<br>
	 * 【注】只插入非null的值，如要需要插入null值，则用insertWithNullWhereNotExist。
	 * whereSql是判断条件，当条件成立时，不插入；当条件不成立时，插入。
	 * @param t 需要插入的DO对象实例
	 * @param whereSql 只能是where语句（可含可不含where关键字），【不能】包含order/group/limit等后续语句
	 * @param args whereSql中的参数
	 * @return 实际修改的条数
	 */
	<T> int insertWhereNotExist(T t, String whereSql, Object... args);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。
	 * @param t 需要插入的DO对象实例
	 * @return 实际修改的条数
	 */
	<T> int insertWithNull(T t);
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。<br>
	 * whereSql是判断条件，当条件成立时，不插入；当条件不成立时，插入。
	 * @param t 需要插入的DO对象实例
	 * @param whereSql 只能是where语句（可含可不含where关键字），不能包含order/group/limit等后续语句
	 * @param args whereSql中的参数
	 * @return 实际修改的条数
	 */
	<T> int insertWithNullWhereNotExist(T t, String whereSql, Object... args);

	/**
	 * 如果t有主键，则更新值；否则插入记录。只有非null的值会更新或插入。
	 * @param t 需要插入的DO对象实例
	 * @return 返回数据库实际修改的条数
	 */
	<T> int insertOrUpdate(T t);
	
	/**
	 * 如果t有主键，则更新值；否则插入记录。包括null的值会更新或插入。
	 * @param t 需要插入的DO对象实例
	 * @return 返回数据库实际修改的条数
	 */
	<T> int insertOrUpdateWithNull(T t);
	
	/**
	 * 如果t有主键，则更新值；否则插入记录。只有非null的值会更新或插入。
	 * @param list 需要插入的DO对象实例列表
	 * @return 返回数据库实际修改的条数
	 */
	<T> int insertOrUpdate(List<T> list);
	
	/**
	 * 如果t有主键，则更新值；否则插入记录。包括null的值会更新或插入。
	 * @param list 需要插入的DO对象实例列表
	 * @return 返回数据库实际修改的条数
	 */
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
	<T> int insertOrUpdateFull(List<T> dbList, List<T> newList);
	
	/**
	 * 文档同insertOrUpdateFull，只是会insert or update null值
	 * @param dbList 可以是null，等同于空list
	 * @param newList 不能是null，否则该方法什么都不执行
	 * @return newList成功的值，不包括dbList中删除的
	 */
	<T> int insertOrUpdateFullWithNull(List<T> dbList, List<T> newList);
		
	/**
	 * 更新单个实例数据库记录，必须带上object的key，包含更新null值的字段
	 * @param t 更新的对象实例
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException 当对象t的主键值为null时抛出
	 */
	<T> int updateWithNull(T t) throws NullKeyValueException;
	
	/**
	 * 带条件的更新单个对象，必须带上object的key，主要用于mysql的update ... where ...这样的CAS修改
	 * 
	 * @param t 更新的对象实例
	 * @param postSql where及后续的sql，包含where关键字
	 * @param args postSql中的参数
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException 当对象t的主键值为null时抛出
	 */
	<T> int updateWithNull(T t, String postSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 更新单条数据库记录,必须带上object的key。【只更新非null字段】
	 * @param t 更新的对象实例
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException 当对象t的主键值为null时抛出
	 */
	<T> int update(T t) throws NullKeyValueException;
	
	/**
	 * 更新单条数据库记录,必须带上object的key，主要用于mysql的update ... where ...这样的CAS修改。
	 * 【只更新非null字段】
	 * 
	 * @param t 更新的对象实例
	 * @param postSql where及后续的sql，包含where关键字
	 * @param args postSql中的参数
	 * @return 返回数据库实际修改条数
	 * @throws NullKeyValueException 当对象t的主键值为null时抛出
	 */
	<T> int update(T t, String postSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 自定义set字句更新，用于单个sql进行值更新，例如set reads = reads + 1这种情况。
	 * @param t 必须提供key
	 * @param setSql 可包含set关键字也可不包含，多个则用逗号隔开，【不能】包含where子句，例如a=a+1,c=b 或 set a=a+1,c=b
	 * @param args set子句的参数
	 * @return 实际修改的条数
	 * @throws NullKeyValueException 当t没有带上key时，抛出该异常
	 */
	<T> int updateCustom(T t, String setSql, Object... args) throws NullKeyValueException;
	
	/**
	 * 自定义更新多行记录，会自动去掉已软删除的行。
	 * 当拦截器存在时，该语句会获得所有被修改的行。
	 *  - 当修改的类有两个及以上个主键时，删除超过100个记录会很慢。
	 *  - 当修改的类只有一个主键时，删除超过1000个记录会比较慢。
	 * 使用该语句请尽量控制修改的条数（可以使用limit子句）并确保修改的列只有一个主键列。
	 * @param clazz 要更新的DO类
	 * @param setSql update sql中的set sql子句，可以包括set关键字也可以不包括
	 * @param whereSql update sql中的where sql子句，可以包括where关键字也可以不包括
	 * @param args whereSql中的参数
	 * @return 实际修改条数
	 */
	<T> int updateAll(Class<T> clazz, String setSql, String whereSql, Object... args);
	
	/**
	 * 更新数据库记录，更新包含null的字段，返回数据库实际修改条数。
	 * 【注】批量更新的方法并不会比程序中循环调用int updateNotNull(T t)更快
	 * @param list 要更新的对象列表
	 * @return 实际修改条数
	 * @throws NullKeyValueException 当对象列表中的对象的主键值为null时抛出
	 */
	<T> int updateWithNull(List<T> list) throws NullKeyValueException;
	
	/**
	 * 更新数据库记录，返回数据库实际修改条数。
	 * 【注】批量更新的方法并不会比程序中循环调用int update(T t)更快
	 * 【只更新非null字段】
	 * @param list 要更新的对象列表
	 * @return 实际修改条数
	 * @throws NullKeyValueException 当对象列表中的对象的主键值为null时抛出
	 */
	<T> int update(List<T> list) throws NullKeyValueException;
	
	/**
	 * 删除数据库记录，返回数据库实际修改条数。
	 * 该操作【会】自动使用软删除进行删除
	 * 
	 * @param t 要更新的对象
	 * @return 实际删除的条数
	 */
	<T> int deleteByKey(T t) throws NullKeyValueException;
	
	/**
	 * 删除数据库记录，返回数据库实际修改条数。
	 * 推荐使用单个主键的表使用该方法，当list所有对象都是同一个类时，将会拼凑为一条sql进行删除，效率提升多。
	 * 该操作【会】自动使用软删除进行删除
	 * @param list 要更新的对象列表
	 * @return 实际删除的条数
	 * @throws NullKeyValueException 当任意一个值没有带key时，抛出异常
	 */
	<T> int deleteByKey(List<T> list) throws NullKeyValueException;
	
	/**
	 * 删除数据库记录，返回实际修改数据库条数，这个接口只支持单个字段是key的情况。
	 * 该操作【会】自动使用软删除进行删除
	 * 
	 * @param clazz 必须有默认构造方法
	 * @param keyValue 要删除的对象的主键值
	 * @return 实际删除的条数
	 * @throws NullKeyValueException 当keyValue为null时，抛出异常
	 */
	<T> int deleteByKey(Class<T> clazz, Object keyValue) throws NullKeyValueException,
	    MustProvideConstructorException;

	/**
	 * 自定义条件删除数据，该操作【会】自动使用软删除标记。
	 * 该方法的实现是先根据条件查出数据，再批量删除，一遍拦截器可以记录下实际被删的数据
	 * @param clazz 必须有默认构造方法
	 * @param postSql 必须提供，必须写where，【不允许】留空
	 * @param args postSql的参数
	 * @return 实际删除的条数
	 */
	<T> int delete(Class<T> clazz, String postSql, Object... args);
	
}
