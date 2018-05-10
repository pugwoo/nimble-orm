package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.IDBHelperDataService;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NotOnlyOneKeyColumnException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.AnnotationSupportRowMapper;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;

import net.sf.jsqlparser.JSQLParserException;

public abstract class P1_QueryOp extends P0_JdbcTemplateOp {
	
	@Override @SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> boolean getByKey(T t) throws NullKeyValueException {
		if(t == null) {return false;}
		Class<?> clazz = t.getClass();
		StringBuilder sql = new StringBuilder(SQLUtils.getSelectSQL(t.getClass(), false, false));
		
		List<Object> keyValues = new ArrayList<Object>();
		sql.append(SQLUtils.getKeysWhereSQL(t, keyValues));
		
		try {
			log(sql);
			doInterceptBeforeQuery(t.getClass(), sql, keyValues.toArray());
			
			long start = System.currentTimeMillis();
			jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(t.getClass(), t),
					keyValues.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
			
			postHandleRelatedColumn(t);
			logSlow(System.currentTimeMillis() - start, sql.toString(), keyValues);
			
			t = doInterceptAfterQuery(clazz, t, sql, keyValues.toArray());
			return t != null;
		} catch (EmptyResultDataAccessException e) {
			t = null;
			t = doInterceptAfterQuery(clazz, t, sql, keyValues.toArray());
			return t != null;
		}
	}
	
	@Override @SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getByKey(Class<T> clazz, Object keyValue) throws NullKeyValueException,
	    NotOnlyOneKeyColumnException {
		
		if(keyValue == null) {
			throw new NullKeyValueException();
		}
		SQLAssert.onlyOneKeyColumn(clazz);
		
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectSQL(clazz, false, false));
		sql.append(SQLUtils.getKeysWhereSQL(clazz));
		
		try {
			log(sql);
			
			doInterceptBeforeQuery(clazz, sql, new Object[]{keyValue});
			long start = System.currentTimeMillis();
			T t = (T) jdbcTemplate.queryForObject(sql.toString(),
					new AnnotationSupportRowMapper(clazz),
					keyValue); // 此处可以用jdbcTemplate，因为没有in (?)表达式
			
			postHandleRelatedColumn(t);
			
			long cost = System.currentTimeMillis() - start;
			
			List<Object> args = new ArrayList<Object>();
			args.add(keyValue);
			logSlow(cost, sql.toString(), args);
			
			t = doInterceptAfterQuery(clazz, t, sql, new Object[]{keyValue});
			return t;
		} catch (EmptyResultDataAccessException e) {
			T t = null;
			t = doInterceptAfterQuery(clazz, t, sql, new Object[]{keyValue});
			return t;
		}
	}
	
	@Override @SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, K> Map<K, T> getByKeyList(Class<T> clazz, List<K> keyValues) {
		if(keyValues == null || keyValues.isEmpty()) {
			return new HashMap<K, T>();
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectSQL(clazz, false, false));
		sql.append(SQLUtils.getKeyInWhereSQL(clazz));
		
		log(sql);
		doInterceptBeforeQuery(clazz, sql, keyValues.toArray());
		long start = System.currentTimeMillis();
		List<T> list = namedParameterJdbcTemplate.query(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(keyValues),
				new AnnotationSupportRowMapper(clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate
		
		postHandleRelatedColumn(list);
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql.toString(), (List<Object>) keyValues);
		
		list = doInteceptAfterQuery(clazz, list, list.size(), sql, keyValues.toArray());
		
		// 转换to map
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
	public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize,
			String postSql, Object... args) {
    	if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		int offset = (page - 1) * pageSize;
		return _getPage(clazz, false, true, offset, pageSize, postSql, args);
	}
    
    @Override
	public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize) {		
		return getPage(clazz, page, pageSize, null);
	}
    
	@Override
	public <T> int getCount(Class<T> clazz) {
		return getTotal(clazz);
	}
	
	@Override
	public <T> int getCount(Class<T> clazz, String postSql, Object... args) {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		return _getPage(clazz, false, true, 0, 1, postSql, args).getTotal();
	}
	 
    @Override
    public <T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize,
			String postSql, Object... args) {
    	if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		int offset = (page - 1) * pageSize;
		return _getPage(clazz, false, false, offset, pageSize, postSql, args);
    }
    
    @Override
	public <T> PageData<T> getPageWithoutCount(final Class<T> clazz, int page, int pageSize) {		
		return getPageWithoutCount(clazz, page, pageSize, null);
	}
    
    @Override
	public <T> List<T> getAll(final Class<T> clazz) {
		return _getPage(clazz, false, false, null, null, null).getData();
	}
    
    @Override
	public <T> List<T> getAll(final Class<T> clazz, String postSql, Object... args) {
    	if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		return _getPage(clazz, false, false, null, null, postSql, args).getData();
	}
    
    @Override
    public <T> List<T> getAllKey(Class<T> clazz, String postSql, Object... args) {
    	if(postSql != null) {postSql = postSql.replace('\t', ' ');}
    	return _getPage(clazz, true, false, null, null, postSql, args).getData();
    }

    @Override
	public <T> T getOne(Class<T> clazz) {
    	List<T> list = _getPage(clazz, false, false, 0, 1, null).getData();
    	return list == null || list.isEmpty() ? null : list.get(0);
    }
	
    @Override
    public <T> T getOne(Class<T> clazz, String postSql, Object... args) {
    	if(postSql != null) {postSql = postSql.replace('\t', ' ');}
    	List<T> list = _getPage(clazz, false, false, 0, 1, postSql, args).getData();
    	return list == null || list.isEmpty() ? null : list.get(0);
    }
    
	/**
	 * 查询列表
	 * 
	 * @param clazz
	 * @param selectOnlyKey 是否只查询主键，只查询主键时，拦截器不进行拦截，RelatedColumn也不处理
	 * @param withCount 是否计算总数，将使用SQL_CALC_FOUND_ROWS配合select FOUND_ROWS();来查询
	 * @param offset 从0开始，null时不生效；当offset不为null时，要求limit存在
	 * @param limit null时不生效
	 * @param postSql sql的where/group/order等sql语句
	 * @param args 参数
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> PageData<T> _getPage(Class<T> clazz,
			boolean selectOnlyKey, boolean withCount,
			Integer offset, Integer limit,
			String postSql, Object... args) {
		
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectSQL(clazz, selectOnlyKey, withCount));
		sql.append(SQLUtils.autoSetSoftDeleted(postSql, clazz));
		sql.append(SQLUtils.genLimitSQL(offset, limit));
		
		log(sql);
		if(!selectOnlyKey) {
			doInterceptBeforeQuery(clazz, sql, args);
		}
		
		long start = System.currentTimeMillis();
		List<T> list;
		if(args == null || args.length == 0) {
			list = namedParameterJdbcTemplate.query(sql.toString(),
					new AnnotationSupportRowMapper(clazz, selectOnlyKey)); // 因为有in (?)所以用namedParameterJdbcTemplate
		} else {
			list = namedParameterJdbcTemplate.query(
					NamedParameterUtils.trans(sql.toString()),
					NamedParameterUtils.transParam(args),
					new AnnotationSupportRowMapper(clazz, selectOnlyKey)); // 因为有in (?)所以用namedParameterJdbcTemplate
		}
		
		int total = -1; // -1 表示没有查询总数，未知
		if(withCount) { // 注意：必须在查询完列表之后马上查询总数
			total = jdbcTemplate.queryForObject("select FOUND_ROWS()", Integer.class);
		}
		
		if(!selectOnlyKey) {
			postHandleRelatedColumn(list);
		}
		
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql.toString(), args == null ? new ArrayList<Object>() : Arrays.asList(args));
		
		if(!selectOnlyKey) {
			doInteceptAfterQuery(clazz, list, total, sql, args);
		}
		
		PageData<T> pageData = new PageData<T>();
		pageData.setData(list);
		pageData.setTotal(total);
		if(limit != null) {
			pageData.setPageSize(limit);
		}
		
		return pageData;
	}
	
	/**
	 * 查询列表总数。
	 * 0.3.1+版本起，带条件的count不使用count(*)计算总数，
	 * 而改用FOUND_ROWS()，目的是统一group by等复杂子句的总数计算方式。
	 * @param clazz
	 * @return
	 */
	private int getTotal(Class<?> clazz) {
		StringBuilder sql = new StringBuilder();
		sql.append(SQLUtils.getSelectCountSQL(clazz));
		sql.append(SQLUtils.autoSetSoftDeleted("", clazz));

		log(sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.queryForObject(sql.toString(), Integer.class); 
		
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql.toString(), null);
		return rows;
	}
	
	@Override
	public <T> boolean isExist(Class<T> clazz, String postSql, Object... args) {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		return getOne(clazz, postSql, args) != null;
	}
	
	@Override
	public <T> boolean isExistAtLeast(int atLeastCounts, Class<T> clazz,
			String postSql, Object... args) {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		if(atLeastCounts == 1) {
			return isExist(clazz, postSql, args);
		}
		return getCount(clazz, postSql, args) >= atLeastCounts;
	}
	
	//////////////////// 拦截器封装方法
	
	private void doInterceptBeforeQuery(Class<?> clazz, StringBuilder sql, Object[] args) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeSelect(clazz, sql.toString(), args);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	/**t为null表示没有记录，因此等价于空list*/
	private <T> T doInterceptAfterQuery(Class<?> clazz, T t, StringBuilder sql, Object[] args) {
		List<T> list = new ArrayList<T>();
		if (t != null) {
			list.add(t);
		}
		list = doInteceptAfterQuery(clazz, list, 1, sql, args);
		return list == null || list.isEmpty() ? null : list.get(0);
	}
	private <T> List<T> doInteceptAfterQuery(Class<?> clazz, List<T> list, int total,
			StringBuilder sql, Object[] args) {
		for (int i = interceptors.size() - 1; i >= 0; i--) {
			list = interceptors.get(i).afterSelect(clazz, sql.toString(), args, list, total);
		}
		return list;
	}
	
	// ======================= 处理 RelatedColumn数据 ========================
	
	@Override
	public <T> void handleRelatedColumn(T t) {
		postHandleRelatedColumn(t);
	}
	
	@Override
	public <T> void handleRelatedColumn(T t, String... relatedColumnProperties) {
		postHandleRelatedColumn(t, relatedColumnProperties);
	}
	
	@Override
	public <T> void handleRelatedColumn(List<T> list) {
		postHandleRelatedColumn(list);
	}
	
	@Override
	public <T> void handleRelatedColumn(List<T> list, String... relatedColumnProperties) {
		postHandleRelatedColumn(list, relatedColumnProperties);
	}
	
	/**单个关联*/
	private <T> void postHandleRelatedColumn(T t, String... relatedColumnProperties) {
		if(t == null) {
			return;
		}
		List<T> list = new ArrayList<T>();
		list.add(t);
		
		postHandleRelatedColumn(list, relatedColumnProperties);
	}
	
	/**批量关联，要求批量操作的都是相同的类*/
	private <T> void postHandleRelatedColumn(List<T> tList, String... relatedColumnProperties) {
		if(tList == null || tList.isEmpty()) {
			return;
		}
		
		JoinTable joinTable = DOInfoReader.getJoinTable(tList.get(0).getClass());
		if(joinTable != null) { // 处理join的方式
			List<Object> list1 = new ArrayList<Object>();
			List<Object> list2 = new ArrayList<Object>();
			
			Field joinLeftTableFiled = DOInfoReader.getJoinLeftTable(tList.get(0).getClass());
			Field joinRightTableFiled = DOInfoReader.getJoinRightTable(tList.get(0).getClass());
			for(T t : tList) {
				Object obj1 = DOInfoReader.getValue(joinLeftTableFiled, t);
				if(obj1 != null) {
					list1.add(obj1);
				}
				Object obj2 = DOInfoReader.getValue(joinRightTableFiled, t);
				if(obj2 != null) {
					list2.add(obj2);
				}
			}
			
			postHandleRelatedColumn(list1);
			postHandleRelatedColumn(list2);
			return;
		}
		
		SQLAssert.allSameClass(tList);
		Class<?> clazz = tList.get(0).getClass();
		
		List<Field> relatedColumns = DOInfoReader.getRelatedColumns(clazz);
		for(Field field : relatedColumns) {
			
			// 只处理指定的field
			if(relatedColumnProperties != null && relatedColumnProperties.length > 0) {
				boolean isContain = false;
				for(String property : relatedColumnProperties) {
					if(property != null && property.equals(field.getName())) {
						isContain = true;
						break;
					}
				}
				if(!isContain) {
					continue;
				}
			}
			
			RelatedColumn column = field.getAnnotation(RelatedColumn.class);
			if(column.localColumn().trim().isEmpty()) {
				LOGGER.warn("relatedColumn value is empty, field:{}", field);
				continue;
			}
			if(column.remoteColumn().trim().isEmpty()) {
				LOGGER.warn("remoteColumn value is empty, field:{}", field);
				continue;
			}
			
			Field localField = DOInfoReader.getFieldByDBField(clazz, column.localColumn());
			if(localField == null) {
				LOGGER.error("cannot find localField,db column name:{}", column.localColumn());
				continue;
			}
			
			// 批量查询数据库，提高效率的关键
			Class<?> remoteDOClass;
			if(field.getType() == List.class) {
				remoteDOClass = DOInfoReader.getGenericFieldType(field);
			} else {
				remoteDOClass = field.getType();
			}
			
			Field remoteField = DOInfoReader.getFieldByDBField(remoteDOClass,
					column.remoteColumn());
			if(remoteField == null) {
				LOGGER.error("cannot find remoteField,db column name:{}", column.remoteColumn());
				continue;
			}
			
			Set<Object> values = new HashSet<Object>(); // 用于去重
			for(T t : tList) {
				Object value = DOInfoReader.getValue(localField, t);
				if(value != null) {
					values.add(value);
				}
			}
			
			if(values.isEmpty()) {
				// 不需要查询数据库，但是对List的，设置空List，确保list不会是null
				if(field.getType() == List.class) {
					for(T t : tList) {
						DOInfoReader.setValue(field, t, new ArrayList<Object>());
					}
				}
				continue;
			}
			
			List<?> relateValues;
			if(column.dataService() != void.class && 
					IDBHelperDataService.class.isAssignableFrom(column.dataService())) {
				IDBHelperDataService dataService = (IDBHelperDataService)
						applicationContext.getBean(column.dataService());
				if(dataService == null) {
					LOGGER.error("dataService is null for {}", column.dataService());
					relateValues = new ArrayList<Object>();
				} else {
					relateValues = dataService.get(new ArrayList<Object>(values),
							clazz, column.localColumn(), 
							remoteDOClass, column.remoteColumn());
				}
			} else {
				String inExpr = column.remoteColumn() + " in (?)";
				if(column.extraWhere() == null || column.extraWhere().trim().isEmpty()) {
					relateValues = getAll(remoteDOClass, "where " + inExpr, values);
				} else {
					String where;
					try {
						where = SQLUtils.insertWhereAndExpression(column.extraWhere(), inExpr);
						relateValues = getAll(remoteDOClass, where, values);
					} catch (JSQLParserException e) {
						LOGGER.error("wrong RelatedColumn extraWhere:{}, ignore extraWhere",
								column.extraWhere());
						relateValues = getAll(remoteDOClass, "where " + inExpr, values);
					}
				}
			}
			
			if(field.getType() == List.class) {
				for(T t : tList) {
					List<Object> value = new ArrayList<Object>();
					for(Object obj : relateValues) {
						Object o1 = DOInfoReader.getValue(localField, t);
						Object o2 = DOInfoReader.getValue(remoteField, obj);
						if(o1 != null && o2 != null) {
							if(o1.getClass().equals(o2.getClass())) {
								if(o1.equals(o2)) {
									value.add(obj);
								}
							} else {
								LOGGER.warn("@RelatedColumn fields local:{},remote:{} is different classes. Use String compare.",
										localField, remoteField);
								if(o1.toString().equals(o2.toString())) {
									value.add(obj);
								}
							}
						}
					}
					if(value.isEmpty()) { // 没有匹配数据时，当原字段有值，则不修改原来的值
						if(DOInfoReader.getValue(field, t) == null) {
							DOInfoReader.setValue(field, t, value);
						}
					} else {
						DOInfoReader.setValue(field, t, value);
					}
				}
			} else {
				for(T t : tList) {
					for(Object obj : relateValues) {
						Object o1 = DOInfoReader.getValue(localField, t);
						Object o2 = DOInfoReader.getValue(remoteField, obj);
						if(o1 != null && o2 != null) {
							if(o1.getClass().equals(o2.getClass())) {
								if(o1.equals(o2)) {
									DOInfoReader.setValue(field, t, obj);
									break;
								}
							} else {
								LOGGER.warn("@RelatedColumn fields local:{},remote:{} is different classes. Use String compare.",
										localField, remoteField);
								if(o1.toString().equals(o2.toString())) {
									DOInfoReader.setValue(field, t, obj);
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	
}
