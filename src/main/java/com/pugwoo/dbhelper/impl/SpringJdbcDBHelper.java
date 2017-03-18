package com.pugwoo.dbhelper.impl;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.BadSQLSyntaxException;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NotSupportMethodException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.impl.part.P5_DeleteOp;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;

import net.sf.jsqlparser.JSQLParserException;

/**
 * 2015年1月12日 16:41:03 数据库操作封装：增删改查
 * @author pugwoo
 */
public class SpringJdbcDBHelper extends P5_DeleteOp {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringJdbcDBHelper.class);
	
	@Override
	public <T> int insertOrUpdate(T t) {
		if(t == null) {
			return 0;
		}
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		if(isWithKey(t, fields)) {
			return update(t);
		} else {
			return insert(t);
		}
	}
	
	@Override
	public <T> int insertOrUpdateWithNull(T t) {
		if(t == null) {
			return 0;
		}
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		if(isWithKey(t, fields)) {
			return updateWithNull(t);
		} else {
			return insertWithNull(t);
		}
	}
	
	@Override @Transactional
	public <T> int insertOrUpdate(List<T> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += insertOrUpdate(t);
			}
		}
		return rows;
	}
	
	@Override @Transactional
	public <T> int insertOrUpdateWithNull(List<T> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += insertOrUpdateWithNull(t);
			}
		}
		return rows;
	}
	
	@Override @Transactional
	public <T> int insertOrUpdateFull(List<T> dbList, List<T> newList) {
		return insertOrUpdateFull(dbList, newList, false);
	}
	
	@Override @Transactional
	public <T> int insertOrUpdateFullWithNull(List<T> dbList, List<T> newList) {
		return insertOrUpdateFull(dbList, newList, true);
	}
	
	private <T> int insertOrUpdateFull(List<T> dbList, List<T> newList, boolean withNull) {
		if(newList == null) {
			return 0;
		}
		if((dbList == null || dbList != null && dbList.isEmpty()) && newList.isEmpty()) {
			return 0; // 不需要处理了
		}
		
		List<Field> fields = DOInfoReader.getColumns(
				dbList != null && !dbList.isEmpty() ? dbList.get(0).getClass()
						: newList.get(0).getClass());
		
		// 1. dbList中有key的，但是newList中没有的，删除掉
		for(T t1 : dbList) {
			if(isWithKey(t1, fields)) {
				boolean isNewExists = false;
				for(T t2 : newList) {
					if(isKeyEquals(t1, t2, fields)) {
						isNewExists = true;
						break;
					}
				}
				if(!isNewExists) {
					deleteByKey(t1);
				}
			}
		}
		
		// 2. insert or update new list
		return withNull ? insertOrUpdateWithNull(newList) : insertOrUpdate(newList);
	}
	
	@Override
	public <T> int update(T t) throws NullKeyValueException {
		return _update(t, false, null);
	}
	
	@Override
	public <T> int update(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, false, postSql, args);
	}
	
	@Override
	public <T> int updateWithNull(T t) throws NullKeyValueException {
		return _update(t, true, null);
	}
	
	@Override
	public <T> int updateWithNull(T t, String postSql, Object... args) throws NullKeyValueException {
		return _update(t, true, postSql, args);
	}
	
	@Override @Transactional
	public <T> int updateWithNull(List<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += updateWithNull(t);
			}
		}
		return rows;
	}
	
	@Override @Transactional
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
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(t.getClass());
		
		if(notKeyFields.isEmpty()) {
			return 0; // log: not need to update
		}
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		
		preHandleUpdate(t, notKeyFields);
		
		sql.append(getTableName(table)).append(" SET ");
		List<Object> keyValues = new ArrayList<Object>();
		String setSql = joinSetAndGetValue(notKeyFields, keyValues, t, withNull);
		if(keyValues.isEmpty()) {
			return 0; // all field is empty, not need to update
		}
		sql.append(setSql);
		int setFieldSize = keyValues.size();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", keyValues, t);
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
		
		sql.append(autoSetSoftDeleted(where, t.getClass()));
		
		return jdbcExecuteUpdate(sql.toString(), keyValues.toArray());
	}
	
	@Override
	public <T> int updateCustom(T t, String setSql, Object... args) throws NullKeyValueException {
		if(setSql == null || setSql.trim().isEmpty()) {
			return 0; // 不需要更新
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		
		List<Object> keyValues = new ArrayList<Object>();
		if(args != null) {
			for(Object arg : args) {
				keyValues.add(arg);
			}
		}
		
		sql.append(getTableName(table)).append(" SET ");
		sql.append(setSql);
		// 加上更新时间
		for(Field field : fields) {
			Column column = DOInfoReader.getColumnInfo(field);
			if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
				sql.append(",").append(getColumnName(column)).append("=?");
				keyValues.add(new Date());
			}
		}
		
		int setFieldSize = keyValues.size();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		// 检查key值是否有null的，不允许有null
		for(int i = setFieldSize; i < keyValues.size(); i++) {
			if(keyValues.get(i) == null) {
				throw new NullKeyValueException();
			}
		}
		sql.append(autoSetSoftDeleted(where, t.getClass()));
		
		return jdbcExecuteUpdate(sql.toString(), keyValues.toArray()); // 不会有in(?)表达式
	}
	
	@Override
	public <T> int deleteByKey(T t) throws NullKeyValueException {
		Table table = DOInfoReader.getTable(t.getClass());
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		
		Field softDelete = DOInfoReader.getSoftDeleteColumn(t.getClass()); // 支持软删除
		
		List<Object> keyValues = new ArrayList<Object>();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", keyValues, t);
		for(Object value : keyValues) { // 检查key的值是不是null
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		
		StringBuilder sql = new StringBuilder();
		if(softDelete == null) { // 物理删除
			sql.append("DELETE FROM ");
			sql.append(getTableName(table));
		} else { // 软删除
			Column softDeleteColumn = DOInfoReader.getColumnInfo(softDelete);
			sql.append("UPDATE ").append(getTableName(table));
			sql.append(" SET ").append(getColumnName(softDeleteColumn));
			sql.append("=").append(softDeleteColumn.softDelete()[1]);
			// 特殊处理@Column setTimeWhenUpdate时间
			for(Field field : fields) {
				Column column = DOInfoReader.getColumnInfo(field);
				if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
					sql.append(",").append(getColumnName(column)).append("='");
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sql.append(df.format(new Date())).append("'");
				}
			}
		}
		sql.append(autoSetSoftDeleted(where, t.getClass()));

		return jdbcExecuteUpdate(sql.toString(), keyValues.toArray());
	}
		
	@Override
	public <T> int deleteByKey(Class<?> clazz, Object keyValue) throws NullKeyValueException {
		if(keyValue == null) {
			throw new NullKeyValueException();
		}
		
		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);
		List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);
		
		if (keyFields.size() != 1) {
			throw new NotSupportMethodException(
					"must have only one key column, actually has "
							+ keyFields.size() + " key columns");
		}
		Column keyColumn = DOInfoReader.getColumnInfo(keyFields.get(0));
		
		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除
		
		StringBuilder sql = new StringBuilder();
		if(softDelete == null) { // 物理删除
			sql.append("DELETE FROM ");
			sql.append(getTableName(table));
		} else { // 软删除
			Column softDeleteColumn = DOInfoReader.getColumnInfo(softDelete);
			sql.append("UPDATE ").append(getTableName(table));
			sql.append(" SET ").append(getColumnName(softDeleteColumn));
			sql.append("=").append(softDeleteColumn.softDelete()[1]);
			// 特殊处理@Column setTimeWhenUpdate时间
			for(Field field : fields) {
				Column column = DOInfoReader.getColumnInfo(field);
				if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
					sql.append(",").append(getColumnName(column)).append("='");
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sql.append(df.format(new Date())).append("'");
				}
			}
		}
		
		String where = "WHERE " + getColumnName(keyColumn) + "=?";
		sql.append(autoSetSoftDeleted(where, clazz));
		
		return jdbcExecuteUpdate(sql.toString(), keyValue);
	}
	
	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		if(postSql == null || postSql.trim().isEmpty()) { // warning: very dangerous
			// 不支持缺省条件来删除。如果需要全表删除，请直接运维人员truncate表。
			throw new InvalidParameterException(); 
		}
		
		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);
		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除

		StringBuilder sql = new StringBuilder();
		
		if(softDelete == null) { // 物理删除
			sql.append("DELETE FROM ");
			sql.append(getTableName(table));
		} else { // 软删除
			Column softDeleteColumn = DOInfoReader.getColumnInfo(softDelete);
			sql.append("UPDATE ").append(getTableName(table));
			sql.append(" SET ").append(getColumnName(softDeleteColumn));
			sql.append("=").append(softDeleteColumn.softDelete()[1]);
			// 特殊处理@Column setTimeWhenUpdate时间
			for(Field field : fields) {
				Column column = DOInfoReader.getColumnInfo(field);
				if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
					sql.append(",").append(getColumnName(column)).append("='");
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sql.append(df.format(new Date())).append("'");
				}
			}
		}
		
		sql.append(autoSetSoftDeleted(postSql, clazz));

		return namedJdbcExecuteUpdate(sql.toString(), args);
	}
	
	/**
	 * 判断两个对象的key是否相等,只有都存在所有的key，且key（如多个，则每个）都相等，才返回true
	 * @param t1
	 * @param t2
	 * @param fields
	 * @return
	 */
	private <T> boolean isKeyEquals(T t1, T t2, List<Field> fields) {
		if(t1 == null || t2 == null || fields == null) {
			 return false;
		}
		List<Field> keyFields = DOInfoReader.getKeyColumns(t1.getClass());
		if(keyFields == null || keyFields.isEmpty()) {
			return false;
		}
		for(Field keyField : keyFields) {
			Object key1 = DOInfoReader.getValue(keyField, t1);
			Object key2 = DOInfoReader.getValue(keyField, t2);
			if(key1 == null || key2 == null || !key1.equals(key2)) {
				return false;
			}
		}
		return true;
	}
	
	/**判断对象是否有主键值，必须全部有才返回true*/
	private <T> boolean isWithKey(T t, List<Field> fields) {
		if(t == null || fields == null || fields.isEmpty()) {
			return false;
		}
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		if(keyFields.isEmpty()) {
			return false;
		}
		
		for(Field keyField : keyFields) {
			if(DOInfoReader.getValue(keyField, t) == null) {
				return false;
			}
		}
		return true;
	}
	
	/**更新前预处理字段值*/
	private <T> void preHandleUpdate(T t, List<Field> fields) {
		if(t == null || fields.isEmpty()) {
			return;
		}
		for(Field field : fields) {
			Column column = DOInfoReader.getColumnInfo(field);
			if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
				DOInfoReader.setValue(field, t, new Date());
			}
		}
	}
	
	/**
	 * TODO 迁移到SQLUtils
	 * 
	 * 自动为【最后】where sql字句加上软删除查询字段
	 * @param whereSql 如果有where条件的，【必须】带上where关键字；如果是group by或空的字符串或null都可以
	 * @param fields
	 * @return 无论如何前面会加空格，更安全
	 */
	private <T> String autoSetSoftDeleted(String whereSql, Class<?> clazz) {
		if(whereSql == null) {
			whereSql = "";
		}
		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz);
		if(softDelete == null) {
			return " " + whereSql; // 不处理
		} else {
			Column softDeleteColumn = DOInfoReader.getColumnInfo(softDelete);
			String deletedExpression = getColumnName(softDeleteColumn) + "=" 
			                        + softDeleteColumn.softDelete()[0];
			try {
				return " " + SQLUtils.insertWhereAndExpression(whereSql, deletedExpression);
			} catch (JSQLParserException e) {
				LOGGER.error("Bad sql syntax,whereSql:{},deletedExpression:{}",
						whereSql, deletedExpression, e);
				throw new BadSQLSyntaxException();
			}
		}
	}
		
	/**
	 * 使用namedParameterJdbcTemplate模版执行update，支持in(?)表达式
	 * @param sql
	 * @param args
	 * @return
	 */
	private int namedJdbcExecuteUpdate(String sql, Object... args) {
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql),
				NamedParameterUtils.transParam(args)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, args);
		}
		return rows;
	}
	
	/**
	 * 使用jdbcTemplate模版执行update，不支持in (?)表达式  TODO 移走
	 * @param sql
	 * @param args
	 * @return 实际修改的行数
	 */
	private int jdbcExecuteUpdate(String sql, Object... args) {
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), args);// 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, args);
		}
		return rows;
	}
		
	/**
	 * 拼凑where子句，并把需要的参数写入到values中。返回sql【不】包含where关键字
	 * 
	 * TODO 迁移到SQLUtils
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
	
	// TODO 迁移到SQLUtils
	private static String getTableName(Table table) {
		return "`" + table.value() + "`";
	}
	// TODO 迁移到SQLUtils
	private static String getColumnName(Column column) {
		return "`" + column.value() + "`";
	}
}
