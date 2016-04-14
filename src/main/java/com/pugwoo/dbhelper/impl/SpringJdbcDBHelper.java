package com.pugwoo.dbhelper.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 */
public class SpringJdbcDBHelper implements DBHelper {
	
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
		sql.append(" FROM ").append(table.value());
		sql.append(" WHERE ").append(where);
		
		System.out.println("Exec SQL:" + sql.toString());
		try {
			jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(t.getClass(), t),
					keyValues.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
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
		sql.append(" FROM ").append(table.value());
		sql.append(" WHERE ").append(keyColumn.value()).append("=?");
		
		System.out.println("Exec SQL:" + sql.toString());
		try {
			return (T) jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(clazz),
					keyValue); // 此处可以用jdbcTemplate，因为没有in (?)表达式
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
		sql.append(" FROM ").append(table.value());
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
		
		System.out.println("Exec SQL:" + sql.toString());
		try {
			return (T) jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(clazz),
					values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
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
		sql.append(" FROM ").append(table.value());
		if(postSql != null) {
			sql.append(" ").append(postSql);
		}
		sql.append(limit(offset, limit));
		
		System.out.println("Exec SQL:" + sql.toString());
		if(postSql == null) {
			return namedParameterJdbcTemplate.query(sql.toString(),
					new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		} else {
			return namedParameterJdbcTemplate.query(
					NamedParameterUtils.trans(sql.toString()),
					NamedParameterUtils.transParam(args),
					new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		}
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
		sql.append(" FROM ").append(table.value());
		if(postSql != null) {
			sql.append(" ").append(postSql); // TODO 可以优化，查count(*)只需要where子句
		}
		
		System.out.println("Exec SQL:" + sql.toString());
		return namedParameterJdbcTemplate.queryForObject(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args),
				Integer.class); // 因为有in (?)所以用namedParameterJdbcTemplate
	}
	
	@Override
	public <T> int insert(T t) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		Field autoIncrementField = getAutoIncrementField(fields);
		
		sql.append(table.value()).append(" (");
		List<Object> values = new ArrayList<Object>();
		sql.append(joinAndGetValue(fields, ",", values, t));
		sql.append(") VALUES (");
		sql.append(join("?", fields.size(), ","));
		sql.append(")");
		
		System.out.println("Exec sql:" + sql.toString());
		int rows = jdbcTemplate.update(sql.toString(), values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		if(autoIncrementField != null && rows == 1) {
			Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()",
					Long.class);
			DOInfoReader.setValue(autoIncrementField, t, id);
		}
		return rows;
	}
	
	@Override
	public <T> int insertInOneSQL(List<T> list) {
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
		
		sql.append(table.value()).append(" (");
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
		
		System.out.println("Exec sql:" + sql.toString());
		return jdbcTemplate.update(sql.toString(), values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
	}
	
	@Override
	public <T> int updateNotNull(T t) {
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
		
		sql.append(table.value()).append(" SET ");
		List<Object> keyValues = new ArrayList<Object>();
		String setSql = joinSetAndGetValue(notKeyFields, keyValues, t, false);
		if(keyValues.isEmpty()) {
			return 0; // all field is null, not need to update
		}
		sql.append(setSql).append(" WHERE ");
		String where = joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		sql.append(where);
		
		System.out.println("Exec SQL:" + sql.toString());
		return jdbcTemplate.update(sql.toString(), keyValues.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
	}
	
	@Override
	public <T> int delete(T t) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(fields);
		
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		
		sql.append(table.value()).append(" WHERE ");
		List<Object> keyValues = new ArrayList<Object>();
		String where = joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		sql.append(where);
		
		System.out.println("Exec SQL:" + sql.toString());
		return jdbcTemplate.update(sql.toString(), keyValues.toArray());  // 此处可以用jdbcTemplate，因为没有in (?)表达式
	}
	
	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		if(postSql == null || postSql.trim().isEmpty()) { // warning: very dangerous
			throw new InvalidParameterException(); 
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		
		Table table = DOInfoReader.getTable(clazz);
		sql.append(table.value()).append(" ").append(postSql);
		
		return namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
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
    
    private static String join(List<Field> fields, String sep) {
    	return joinAndGetValue(fields, sep, null, null);
    }
    
    private static List<Object> getValue(List<Field> fields, Object obj) {
    	List<Object> values = new ArrayList<Object>();
    	for(Field field : fields) {
    		values.add(DOInfoReader.getValue(field, obj));
    	}
    	return values;
    }
    
    /**
     * 拼凑字段逗号,分隔子句（用于insert），并把参数放到values中
     * @param fields
     * @param sep
     * @param values
     * @param obj
     * @return
     */
	private static String joinAndGetValue(List<Field> fields, String sep,
			List<Object> values, Object obj) {
    	StringBuilder sb = new StringBuilder();
    	int size = fields.size();
    	for(int i = 0; i < size; i++) {
    		Column column = DOInfoReader.getColumnInfo(fields.get(i));
    		sb.append(column.value());
    		if(i < size - 1) {
    			sb.append(sep);
    		}
    		if(values != null && obj != null) {
    			values.add(DOInfoReader.getValue(fields.get(i), obj));
    		}
    	}
    	return sb.toString();
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
			sb.append(column.value()).append("=?");
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
				sb.append(column.value()).append("=?,");
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
}
