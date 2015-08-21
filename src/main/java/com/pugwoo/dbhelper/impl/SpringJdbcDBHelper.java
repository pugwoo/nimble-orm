package com.pugwoo.dbhelper.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NotSupportMethodException;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.utils.AnnotationSupportRowMapper;
import com.pugwoo.dbhelper.utils.DOInfoReader;

/**
 * 2015年1月12日 16:41:03 数据库操作封装：增删改查
 */
public class SpringJdbcDBHelper implements DBHelper {
	
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> boolean getByKey(T t) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(fields);
		List<Object> keyValues = new ArrayList<Object>();
		
		String where = joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		sql.append(join(fields, ","));
		sql.append(" FROM ").append(table.value());
		if(!where.isEmpty()) {
			sql.append(" WHERE ").append(where);
		}
		
		System.out.println("Exec SQL:" + sql.toString());
		try {
			jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(t.getClass(), t),
					keyValues.toArray());
			return true;
		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getByKey(Class<?> clazz, Object keyValue) {
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
					keyValue);
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
		
		System.out.println("Exec SQL:" + sql.toString());
		try {
			return (T) jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(clazz),
					values.toArray());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
    @Override
	public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize) {
		int offset = (page - 1) * pageSize;
		List<T> data = _getList(clazz, offset, pageSize);
		int total = getTotal(clazz);
		return new PageData<T>(total, data);
	}
	
    @Override
	public <T> List<T> getAll(final Class<T> clazz) {
		return _getList(clazz, null, null);
	}

	/**
	 * 查询列表，没有查询条件
	 * 
	 * @param clazz
	 * @param offset 从0开始，null时不生效；当offset不为null时，要求limit存在
	 * @param limit null时不生效
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> List<T> _getList(Class<T> clazz, Integer offset, Integer limit) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");

		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);

		sql.append(join(fields, ","));
		sql.append(" FROM ").append(table.value());
		sql.append(limit(offset, limit));
		
		System.out.println("Exec SQL:" + sql.toString());
		return jdbcTemplate.query(sql.toString(), new AnnotationSupportRowMapper(clazz));
	}
	
	/**
	 * 查询列表总数
	 * @param clazz
	 * @return
	 */
	private int getTotal(Class<?> clazz) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(*)");
		
		Table table = DOInfoReader.getTable(clazz);
		sql.append(" FROM ").append(table.value());
		
		System.out.println("Exec SQL:" + sql.toString());
		return jdbcTemplate.queryForObject(sql.toString(), Integer.class);
	}
	
	/**
	 * 插入一条记录，返回数据库实际修改条数。<br>
	 * 如果包含了自增id，则自增Id会被设置。
	 * 
	 * @param t
	 * @return
	 */
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
		int rows = jdbcTemplate.update(sql.toString(), values.toArray());
		if(autoIncrementField != null && rows == 1) {
			Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()",
					Long.class);
			DOInfoReader.setValue(autoIncrementField, t, id);
		}
		return rows;
	}
	
	/**
	 * 插入几条数据，通过拼凑成一条sql插入
	 *【注】批量插入不支持回设自增id。
	 * 
	 * @param list
	 * @return 返回影响的行数
	 */
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
		return jdbcTemplate.update(sql.toString(), values.toArray());
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
	
	// str=?,times=3,sep=,  返回 ?,?,?
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
