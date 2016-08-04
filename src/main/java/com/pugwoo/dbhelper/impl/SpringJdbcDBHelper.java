package com.pugwoo.dbhelper.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NotSupportMethodException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.utils.AnnotationSupportRowMapper;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;

/**
 * 2015年1月12日 16:41:03 数据库操作封装：增删改查
 * @author pugwoo
 */
public class SpringJdbcDBHelper implements DBHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringJdbcDBHelper.class);
	
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private long timeoutWarningValve = 1000;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
	
	public void setTimeoutWarningValve(long timeMS) {
		timeoutWarningValve = timeMS;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> boolean getByKey(T t) throws NullKeyValueException {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(fields);
		List<Object> keyValues = new ArrayList<Object>();
		if(keyFields == null || keyFields.isEmpty()) {
			return false;
		}
		
		String where = joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		// 检查主键不允许为null
		for(Object value : keyValues) {
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		sql.append(join(fields, ","));
		sql.append(" FROM ").append(getTableName(table));
		sql.append(" WHERE ").append(where);
		
		try {
			LOGGER.debug("ExecSQL:{}", sql);
			long start = System.currentTimeMillis();
			jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(t.getClass(), t),
					keyValues.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
			long cost = System.currentTimeMillis() - start;
			if(cost > timeoutWarningValve) {
				LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, keyValues);
			}
			return true;
		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getByKey(Class<?> clazz, Object keyValue) throws NullKeyValueException {
		if(keyValue == null) {
			throw new NullKeyValueException();
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");

		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);
		List<Field> keyFields = DOInfoReader.getKeyColumns(fields);

		if (keyFields.size() != 1) {
			throw new NotSupportMethodException(
					"must have only one key column, actually has "
							+ keyFields.size() + " key columns");
		}
		Column keyColumn = DOInfoReader.getColumnInfo(keyFields.get(0));
		
		sql.append(join(fields, ","));
		sql.append(" FROM ").append(getTableName(table));
		sql.append(" WHERE ").append(getColumnName(keyColumn)).append("=?");
		
		try {
			LOGGER.debug("ExecSQL:{}", sql);
			long start = System.currentTimeMillis();
			T t = (T) jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(clazz),
					keyValue); // 此处可以用jdbcTemplate，因为没有in (?)表达式
			long cost = System.currentTimeMillis() - start;
			if(cost > timeoutWarningValve) {
				LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, keyValue);
			}
			return t;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getByKey(Class<?> clazz, Map<String, Object> keyMap) {
		if(keyMap == null || keyMap.isEmpty()) {
			throw new InvalidParameterException("keyMap require at least one key");
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		
		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);
		
		sql.append(join(fields, ","));
		sql.append(" FROM ").append(getTableName(table));
		sql.append(" WHERE ");
		
		List<Object> values = new ArrayList<Object>();
		boolean isFirst = true;
		for(String key : keyMap.keySet()) {
			if(!isFirst) {
				sql.append(" AND ");
			}
			isFirst = false;
			sql.append(key).append("=?");
			values.add(keyMap.get(key));
		}
		
		// 检查主键不允许为null
		for(Object value : values) {
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		
		try {
			LOGGER.debug("ExecSQL:{}", sql);
			long start = System.currentTimeMillis();
			T t = (T) jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(clazz),
					values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
			long cost = System.currentTimeMillis() - start;
			if(cost > timeoutWarningValve) {
				LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, values);
			}
			return t;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
    @Override
	public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize,
			String postSql, Object... args) {
		int offset = (page - 1) * pageSize;
		List<T> data = _getList(clazz, offset, pageSize, postSql, args);
		// 性能优化，当page=1 且拿到的数据少于pageSzie，则不需要查总数
		int total = 0;
		if(page == 1 && data.size() < pageSize) {
			total = data.size();
		} else {
			total = getTotal(clazz, postSql, args);
		}
		return new PageData<T>(total, data, pageSize);
	}
    
    @Override
	public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize) {		
		return getPage(clazz, page, pageSize, null);
	}
    
    @Override
    public <T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args) {
		int offset = (page - 1) * pageSize;
		List<T> data = _getList(clazz, offset, pageSize, postSql, args);
		return new PageData<T>(-1, data, pageSize);
    }
    
    @Override
	public <T> PageData<T> getPageWithoutCount(final Class<T> clazz, int page, int pageSize) {		
		return getPageWithoutCount(clazz, page, pageSize, null);
	}
	
    @Override
	public <T> List<T> getAll(final Class<T> clazz) {
		return _getList(clazz, null, null, null);
	}
    
    @Override
	public <T> List<T> getAll(final Class<T> clazz, String postSql, Object... args) {
		return _getList(clazz, null, null, postSql, args);
	}
    
    @Override
	public <T> T getOne(Class<T> clazz) {
    	List<T> list = _getList(clazz, 0, 1, null);
    	return list == null || list.isEmpty() ? null : list.get(0);
    }
	
    @Override
    public <T> T getOne(Class<T> clazz, String postSql, Object... args) {
    	List<T> list = _getList(clazz, 0, 1, postSql, args);
    	return list == null || list.isEmpty() ? null : list.get(0);
    }

	/**
	 * 查询列表
	 * 
	 * @param clazz
	 * @param offset 从0开始，null时不生效；当offset不为null时，要求limit存在
	 * @param limit null时不生效
	 * @param postSql sql的where/group/order等sql语句
	 * @param args 参数
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> List<T> _getList(Class<T> clazz, Integer offset, Integer limit,
			String postSql, Object... args) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");

		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);

		sql.append(join(fields, ","));
		sql.append(" FROM ").append(getTableName(table));
		if(postSql != null) {
			sql.append(" ").append(postSql);
		}
		sql.append(limit(offset, limit));
		
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		List<T> list = null;
		if(postSql == null) {
			list = namedParameterJdbcTemplate.query(sql.toString(),
					new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		} else {
			list = namedParameterJdbcTemplate.query(
					NamedParameterUtils.trans(sql.toString()),
					NamedParameterUtils.transParam(args),
					new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		}
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, args);
		}
		return list;
	}
	
	/**
	 * 查询列表总数
	 * @param clazz
	 * @return
	 */
	private int getTotal(Class<?> clazz, String postSql, Object... args) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(*)");
		
		Table table = DOInfoReader.getTable(clazz);
		sql.append(" FROM ").append(getTableName(table));
		if(postSql != null) {
			sql.append(" ").append(postSql); // XXX 可以优化，查count(*)只需要where子句
		}
		
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = namedParameterJdbcTemplate.queryForObject(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args),
				Integer.class); // 因为有in (?)所以用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, args);
		}
		return rows;
	}
	
	@Override
	public <T> int insert(T t) {
		return insert(t, false);
	}
	
	@Override
	public <T> int insertWithNull(T t) {
		return insert(t, true);
	}
	
	private <T> int insert(T t, boolean isWithNullValue) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		Field autoIncrementField = getAutoIncrementField(fields);
		
		sql.append(getTableName(table)).append(" (");
		List<Object> values = new ArrayList<Object>();
		sql.append(joinAndGetValue(fields, ",", values, t, isWithNullValue));
		sql.append(") VALUES (");
		sql.append(join("?", values.size(), ","));
		sql.append(")");
		
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		if(autoIncrementField != null && rows == 1) {
			Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()",
					Long.class);
			DOInfoReader.setValue(autoIncrementField, t, id);
		}
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, values);
		}
		return rows;
	}
		
	@Override
	public <T> int insertWithNullInOneSQL(List<T> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		
		list.removeAll(Collections.singleton(null));
		
		Class<?> clazz = null;
		for(T t : list) {
			if(clazz == null) {
				clazz = t.getClass();
			} else {
				if (!clazz.equals(t.getClass())) {
					throw new InvalidParameterException(
							"list elements must be same class");
				}
			}
		}
		
		if(clazz == null) {
			return 0;
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		
		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);
		
		sql.append(getTableName(table)).append(" (");
		sql.append(join(fields, ","));
		sql.append(") VALUES ");
		
		List<Object> values = new ArrayList<Object>();
		for(int i = 0; i < list.size(); i++) {
			sql.append("(");
			sql.append(join("?", fields.size(), ","));
			sql.append(")");
			if(i < list.size() - 1) {
				sql.append(",");
			}
			values.addAll(getValue(fields, list.get(i)));
		}
		
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, values);
		}
		return rows;
	}
	
	@Override
	public <T> int update(T t) throws NullKeyValueException {
		return _update(t, true, null);
	}
	
	@Override
	public <T> int update(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, true, postSql, args);
	}
	
	@Override
	public <T> int updateNotNull(T t) throws NullKeyValueException {
		return _update(t, false, null);
	}
	
	@Override
	public <T> int updateNotNull(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, false, postSql, args);
	}
	
	@Override
	public <T> int updateNotNull(List<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += updateNotNull(t);
			}
		}
		return rows;
	}
	
	@Override
	public <T> int update(List<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += update(t);
			}
		}
		return rows;
	}
	
	private <T> int _update(T t, boolean withNull, String postSql, Object... args) 
			throws NullKeyValueException {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(fields);
		List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(fields);
		
		if(notKeyFields.isEmpty()) {
			return 0; // log: not need to update
		}
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		
		sql.append(getTableName(table)).append(" SET ");
		List<Object> keyValues = new ArrayList<Object>();
		String setSql = joinSetAndGetValue(notKeyFields, keyValues, t, withNull);
		if(keyValues.isEmpty()) {
			return 0; // all field is empty, not need to update
		}
		sql.append(setSql).append(" WHERE ");
		int setFieldSize = keyValues.size();
		String where = joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		// 检查key值是否有null的，不允许有null
		for(int i = setFieldSize; i < keyValues.size(); i++) {
			if(keyValues.get(i) == null) {
				throw new NullKeyValueException();
			}
		}
		
		// 带上postSql
		if(postSql != null) {
			postSql = postSql.trim();
			if(!postSql.isEmpty()) {
				if(postSql.startsWith("where")) {
					postSql = " AND " + postSql.substring(5);
				}
				where = where + postSql;
				if(args != null) {
					keyValues.addAll(Arrays.asList(args));
				}
			}
		}
		
		sql.append(where);
		
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), keyValues.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, keyValues);
		}
		return rows;
	}
	
	@Override
	public <T> int deleteByKey(T t) throws NullKeyValueException {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(fields);
		
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		
		sql.append(getTableName(table)).append(" WHERE ");
		List<Object> keyValues = new ArrayList<Object>();
		String where = joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		// 检查key的值是不是null
		for(Object value : keyValues) {
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		sql.append(where);
		
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), keyValues.toArray());  // 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, keyValues);
		}
		return rows;
	}
	
	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		if(postSql == null || postSql.trim().isEmpty()) { // warning: very dangerous
			throw new InvalidParameterException(); 
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		
		Table table = DOInfoReader.getTable(clazz);
		sql.append(getTableName(table)).append(" ").append(postSql);
		
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, args);
		}
		return rows;
	}
	
	private static Field getAutoIncrementField(List<Field> fields) {
		for(Field field : fields) {
			Column column = DOInfoReader.getColumnInfo(field);
			if(column.isAutoIncrement()) {
				return field;
			}
		}
		return null;
	}
	
	// 例如：str=?,times=3,sep=,  返回 ?,?,?
    private static String join(String str, int times, String sep) {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < times; i++) {
    		sb.append(str);
    		if(i < times - 1) {
    			sb.append(sep);
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * 拼凑select的field的语句
     * @param fields
     * @param sep
     * @return
     */
    private static String join(List<Field> fields, String sep) {
    	return joinAndGetValue(fields, sep, null, null, false);
    }
    
    private static List<Object> getValue(List<Field> fields, Object obj) {
    	List<Object> values = new ArrayList<Object>();
    	for(Field field : fields) {
    		values.add(DOInfoReader.getValue(field, obj));
    	}
    	return values;
    }
    
    /**
     * 拼凑字段逗号,分隔子句（用于insert），并把参数obj的值放到values中
     * @param fields
     * @param sep
     * @param values
     * @param obj
     * @param isWithNullValue 是否把null值放到values中
     * @return
     */
	private static String joinAndGetValue(List<Field> fields, String sep,
			List<Object> values, Object obj, boolean isWithNullValue) {
    	StringBuilder sb = new StringBuilder();
    	for(Field field : fields) {
    		Column column = DOInfoReader.getColumnInfo(field);

    		boolean isAppendColumn = true;
    		if(values != null && obj != null) {
    			Object value = DOInfoReader.getValue(field, obj);
    			if(isWithNullValue) {
    				values.add(value);
    			} else {
    				if(value == null) {
    					isAppendColumn = false;
    				} else {
    					values.add(value);
    				}
    			}
    		}
    		
    		if(isAppendColumn) {
        		sb.append(getColumnName(column)).append(sep);
    		}
    	}
    	int len = sb.length();
    	return len == 0 ? "" : sb.toString().substring(0, len - 1);
	}

	/**
	 * 拼凑where子句，并把需要的参数写入到values中
	 * @param fields
	 * @param logicOperate 操作符，例如AND
	 * @param values
	 * @param obj
	 * @return
	 */
	private static String joinWhereAndGetValue(List<Field> fields,
			String logicOperate, List<Object> values, Object obj) {
		StringBuilder sb = new StringBuilder();
		int fieldSize = fields.size();
		for(int i = 0; i < fieldSize; i++) {
			Column column = DOInfoReader.getColumnInfo(fields.get(i));
			sb.append(getColumnName(column)).append("=?");
			if(i < fieldSize - 1) {
				sb.append(" ").append(logicOperate).append(" ");
			}
			values.add(DOInfoReader.getValue(fields.get(i), obj));
		}
		return sb.toString();
	}
	
	/**
	 * 拼凑set子句
	 * @param fields
	 * @param values
	 * @param obj
	 * @param withNull 当为true时，如果field的值为null，也加入
	 * @return
	 */
	private static String joinSetAndGetValue(List<Field> fields,
			List<Object> values, Object obj, boolean withNull) {
		StringBuilder sb = new StringBuilder();
		int fieldSize = fields.size();
		for(int i = 0; i < fieldSize; i++) {
			Column column = DOInfoReader.getColumnInfo(fields.get(i));
			Object value = DOInfoReader.getValue(fields.get(i), obj);
			if(withNull || value != null) {
				sb.append(getColumnName(column)).append("=?,");
				values.add(value);
			}
		}
		return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
	}
	
	/**
	 * 拼凑limit字句
	 * @param offset 可以为null
	 * @param limit 不能为null
	 * @return
	 */
	private static String limit(Integer offset, Integer limit) {
		StringBuilder sb = new StringBuilder();
		if (limit != null) {
			sb.append(" limit ");
			if(offset != null) {
				sb.append(offset).append(",");
			}
			sb.append(limit);
		}
		return sb.toString();
	}
	
	private static String getTableName(Table table) {
		return "`" + table.value() + "`";
	}
	
	private static String getColumnName(Column column) {
		return "`" + column.value() + "`";
	}
}
