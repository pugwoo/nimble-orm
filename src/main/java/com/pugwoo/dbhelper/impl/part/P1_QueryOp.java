package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.exception.NotOnlyOneKeyColumnException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.AnnotationSupportRowMapper;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;

public abstract class P1_QueryOp extends P0_JdbcTemplateOp {
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> boolean getByKey(T t) throws NullKeyValueException {
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectSQL(t.getClass()));
		
		List<Object> keyValues = new ArrayList<Object>();
		sql.append(SQLUtils.getKeysWhereSQL(t, keyValues));
		
		try {
			log(sql);
			long start = System.currentTimeMillis();
			jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(t.getClass(), t),
					keyValues.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
			
			postHandleRelatedColumn(t);
			
			long cost = System.currentTimeMillis() - start;
			logSlow(cost, sql, keyValues);
			return true;
		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getByKey(Class<?> clazz, Object keyValue) throws NullKeyValueException,
	    NotOnlyOneKeyColumnException {
		
		if(keyValue == null) {
			throw new NullKeyValueException();
		}
		SQLAssert.onlyOneKeyColumn(clazz);
		
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectSQL(clazz));
		sql.append(SQLUtils.getKeysWhereSQL(clazz));
		
		try {
			log(sql);
			long start = System.currentTimeMillis();
			T t = (T) jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(clazz),
					keyValue); // 此处可以用jdbcTemplate，因为没有in (?)表达式
			long cost = System.currentTimeMillis() - start;
			logSlow(cost, sql, keyValue);
			return t;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T, K> Map<K, T> getByKeyList(Class<?> clazz, List<K> keyValues) {
		if(keyValues == null || keyValues.isEmpty()) {
			return new HashMap<K, T>();
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectSQL(clazz));
		sql.append(SQLUtils.getKeyInWhereSQL(clazz));
		
		log(sql);
		long start = System.currentTimeMillis();
		List<T> list = namedParameterJdbcTemplate.query(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(keyValues),
				new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, keyValues);
		
		if(list == null || list.isEmpty()) {
			return new HashMap<K, T>();
		}
		
		Field keyField = DOInfoReader.getOneKeyColumn(clazz);
		Map<K, T> map = new LinkedHashMap<K, T>();
		for(K key : keyValues) {
			if(key == null) {continue;}
			for(T t : list) {
				Object k = DOInfoReader.getValue(keyField, t);
				if(k != null && key.equals(k)) {
					map.put(key, t);
					break;
				}
			}
		}
		return map;
	}
	
	@Override
	public <T> int getCount(Class<T> clazz) {
		return getTotal(clazz, null);
	}
	
	@Override
	public <T> int getCount(Class<T> clazz, String postSql, Object... args) {
		return getTotal(clazz, postSql, args);
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
		sql.append(SQLUtils.getSelectSQL(clazz));
		sql.append(SQLUtils.autoSetSoftDeleted(postSql, clazz));
		sql.append(SQLUtils.genLimitSQL(offset, limit));
		
		log(sql);
		long start = System.currentTimeMillis();
		List<T> list = null;
		if(args == null || args.length == 0) {
			list = namedParameterJdbcTemplate.query(sql.toString(),
					new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		} else {
			list = namedParameterJdbcTemplate.query(
					NamedParameterUtils.trans(sql.toString()),
					NamedParameterUtils.transParam(args),
					new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		}
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, args);
		return list;
	}
	
	/**
	 * 查询列表总数
	 * @param clazz
	 * @return
	 */
	private int getTotal(Class<?> clazz, String postSql, Object... args) {
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectCountSQL(clazz));
		sql.append(SQLUtils.autoSetSoftDeleted(postSql, clazz));

		log(sql);
		long start = System.currentTimeMillis();
		int rows = namedParameterJdbcTemplate.queryForObject(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args),
				Integer.class); // 因为有in (?)所以用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, args);
		return rows;
	}
	
	// ======================= 处理 RelatedColumn数据 ========================
	
	private <T> void postHandleRelatedColumn(T t) {
		List<Field> relatedColumns = DOInfoReader.getRelatedColumns(t.getClass());
		
		for(Field field : relatedColumns) {
			RelatedColumn column = field.getAnnotation(RelatedColumn.class);
			if(column.value() == null || column.value().isEmpty()) {
				continue;
			}
			
			Field relateField = DOInfoReader.getFieldByDBField(t.getClass(), column.value());
			if(relateField == null) {
				continue;
			}
			
			Object value = DOInfoReader.getValue(relateField, t);
			Object relateValue = getByKey(field.getType(), value);
			DOInfoReader.setValue(field, t, relateValue);
		}
	}
	
	private <T> void postHandleRelatedColumn(List<T> tList) {
		
	}
	
}
