package com.pugwoo.dbhelper.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.BadSQLSyntaxException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.utils.DOInfoReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * SQL解析工具类
 * 
 * @author pugwoo
 * 2017年3月16日 23:02:47
 */
public class SQLUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SQLUtils.class);

	/**
	 * select 字段 from t_table, 不包含where子句及以后的语句
	 * @param clazz
	 * @return
	 */
	public static String getSelectSQL(Class<?> clazz) {
		Table table = DOInfoReader.getTable(clazz);
		List<Field> fields = DOInfoReader.getColumns(clazz);
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		
		sql.append(join(fields, ","));
		sql.append(" FROM ").append(getTableName(table));
		
		return sql.toString();
	}
	
	/**
	 * select 字段 from t_table, 不包含where子句及以后的语句
	 * @param clazz
	 * @return
	 */
	public static String getSelectCountSQL(Class<?> clazz) {
		Table table = DOInfoReader.getTable(clazz);
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(*)");
		sql.append(" FROM ").append(getTableName(table));
		
		return sql.toString();
	}
	
	/**
	 * 获得主键where子句，包含where关键字。会自动处理软删除条件
	 * 
	 * @param t
	 * @param keyValues 返回传入sql的参数，如果提供list则写入
	 * @return 返回值前面会带空格，以确保安全。
	 * @throws NoKeyColumnAnnotationException
	 * @throws NullKeyValueException
	 */
	public static <T> String getKeysWhereSQL(T t, List<Object> keyValues) 
	    throws NoKeyColumnAnnotationException, NullKeyValueException {
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		
		List<Object> _keyValues = new ArrayList<Object>();
		String where = joinWhereAndGetValue(keyFields, "AND", _keyValues, t);
		
		// 检查主键不允许为null
		for(Object value : keyValues) {
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		
		if(keyValues != null) {
			keyValues.addAll(_keyValues);
		}
		
		return autoSetSoftDeleted("WHERE " + where, t.getClass());
	}
	
	/**
	 * 获得主键where子句，包含where关键字。会自动处理软删除条件
	 * 
	 * @param clazz
	 * @throws NoKeyColumnAnnotationException
	 */
	public static String getKeysWhereSQL(Class<?> clazz) 
			throws NoKeyColumnAnnotationException {
		List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);
		String where = joinWhere(keyFields, "AND");
		return autoSetSoftDeleted("WHERE " + where, clazz);
	}
	
	/**
	 * 获得主键in(?)的where子句，包含where关键字。会自动处理软删除条件
	 * @param clazz
	 * @return
	 */
	public static String getKeyInWhereSQL(Field keyField, Class<?> clazz) {
		return autoSetSoftDeleted("WHERE " +
	           getColumnName(DOInfoReader.getColumnInfo(keyField)) + " in (?)", clazz);
	}
	
	/**
	 * 往where sql里面插入AND关系的表达式。
	 * 
	 * 例如：whereSql为 where a!=3 or a!=2 limit 1
	 *      condExpress为 deleted=0
	 * 那么返回：where deleted=0 and (a!=3 or a!=2) limit 1
	 * 
	 * @param whereSql 从where起的sql子句，如果有where必须带上where关键字。
	 * @param condExpression 例如a=?  不带where或and关键字。
	 * @return 注意返回字符串前面没有空格
	 * @throws JSQLParserException 
	 */
	public static String insertWhereAndExpression(String whereSql, String condExpression) 
			throws JSQLParserException {
		
		if(condExpression == null || condExpression.trim().isEmpty()) {
			return whereSql == null ? "" : whereSql;
		}
		if(whereSql == null || whereSql.trim().isEmpty()) {
			return "WHERE " + condExpression;
		}
		
		whereSql = whereSql.trim();
		if(!whereSql.toUpperCase().startsWith("WHERE ")) {
			return "WHERE " + condExpression + " " + whereSql;
		}
		
		
		String selectSql = "select * from dual "; // 辅助where sql解析用
		Statement statement = CCJSqlParserUtil.parse(selectSql + whereSql);
		Select selectStatement = (Select) statement;
		PlainSelect plainSelect = (PlainSelect)selectStatement.getSelectBody();
		
		Expression ce = CCJSqlParserUtil.parseCondExpression(condExpression);
		Expression oldWhere = plainSelect.getWhere();
		Expression newWhere = new FixedAndExpression(ce, oldWhere);
		plainSelect.setWhere(newWhere);
		
		return plainSelect.toString().substring(selectSql.length());
	}
	
	/**
	 * 自动为【最后】where sql字句加上软删除查询字段
	 * @param whereSql 如果有where条件的，【必须】带上where关键字；如果是group by或空的字符串或null都可以
	 * @param fields
	 * @return 无论如何前面会加空格，更安全
	 */
	public static <T> String autoSetSoftDeleted(String whereSql, Class<?> clazz) {
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
	 * 拼凑limit字句。前面有空格。
	 * @param offset 可以为null
	 * @param limit 不能为null
	 * @return
	 */
	public static String genLimitSQL(Integer offset, Integer limit) {
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
	
    /**
     * 拼凑select的field的语句
     * @param fields
     * @param sep
     * @return
     */
    private static String join(List<Field> fields, String sep) {
    	return joinAndGetValue(fields, sep, null, null, false);
    }
	
	/**
	 * 拼凑where子句，并把需要的参数写入到values中。返回sql【不】包含where关键字
	 * 
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
	 * 拼凑where子句。返回sql【不】包含where关键字
	 * @param fields
	 * @param logicOperate 操作符，例如AND
	 * @return
	 */
	private static String joinWhere(List<Field> fields, String logicOperate) {
		StringBuilder sb = new StringBuilder();
		int fieldSize = fields.size();
		for(int i = 0; i < fieldSize; i++) {
			Column column = DOInfoReader.getColumnInfo(fields.get(i));
			sb.append(getColumnName(column)).append("=?");
			if(i < fieldSize - 1) {
				sb.append(" ").append(logicOperate).append(" ");
			}
		}
		return sb.toString();
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
	 * 例如：str=?,times=3,sep=,  返回 ?,?,?
	 */
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
    
	private static String getTableName(Table table) {
		return "`" + table.value() + "`";
	}

	private static String getColumnName(Column column) {
		return "`" + column.value() + "`";
	}

}
