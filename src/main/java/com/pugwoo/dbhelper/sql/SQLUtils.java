package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.annotation.*;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.exception.*;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.model.SubQuery;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.ScriptUtils;
import com.pugwoo.dbhelper.utils.TypeAutoCast;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * SQL解析工具类
 * 
 * @author pugwoo
 * 2017年3月16日 23:02:47
 */
public class SQLUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SQLUtils.class);
	
	/**
	 * 展开子查询SubQuery子句。该方法不支持子查询嵌套，由上一层方法来嵌套调用以实现SubQuery子句嵌套。
	 * 该方法会自动处理软删除标记。
	 * @param subQuery
	 * @param values 带回去的参数列表
	 * @return
	 */
	public static String expandSubQuery(SubQuery subQuery, List<Object> values) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM (SELECT ");
		sql.append(subQuery.getField());
		sql.append(" FROM ").append(getTableName(subQuery.getClazz())); // 注意：subQuery这里不用table的alias
		sql.append(" ").append(SQLUtils.autoSetSoftDeleted(subQuery.getPostSql(), subQuery.getClazz()));
		sql.append(") sub ");
		
		if(subQuery.getArgs() != null) {
			values.addAll(Arrays.asList(subQuery.getArgs()));
		}
		return sql.toString();
	}
	
	/**
	 * select 字段 from t_table, 不包含where子句及以后的语句
	 * @param clazz
	 * @param selectOnlyKey 是否只查询key
	 * @param isSelect1 是否只select 1，不查询实际字段；当该值为true时，selectOnlyKey无效。
	 * @return
	 */
	public static String getSelectSQL(Class<?> clazz, boolean selectOnlyKey, boolean isSelect1) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");

		// 处理join方式clazz
		JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
		if(joinTable != null) {
			Field leftTableField = DOInfoReader.getJoinLeftTable(clazz);
			Field rightTableField = DOInfoReader.getJoinRightTable(clazz);
			
			JoinLeftTable joinLeftTable = leftTableField.getAnnotation(JoinLeftTable.class);
			JoinRightTable joinRightTable = rightTableField.getAnnotation(JoinRightTable.class);

			if(isSelect1) {
			    sql.append("1");
            } else {
                List<Field> fields1 = DOInfoReader.getColumnsForSelect(leftTableField.getType(), selectOnlyKey);
                List<Field> fields2 = DOInfoReader.getColumnsForSelect(rightTableField.getType(), selectOnlyKey);
                sql.append(join(fields1, ",", joinLeftTable.alias() + "."));
                sql.append(",");
                sql.append(join(fields2, ",", joinRightTable.alias() + "."));
            }

	        sql.append(" FROM ").append(getTableName(leftTableField.getType()))
	           .append(" ").append(joinLeftTable.alias()).append(" ");
	        sql.append(joinTable.joinType().getCode()).append(" ");
	        sql.append(getTableName(rightTableField.getType())).append(" ").append(joinRightTable.alias());
	        if(joinTable.on().trim().isEmpty()) {
	        	throw new OnConditionIsNeedException("join table :" + clazz.getName());
	        }
	        sql.append(" on ").append(joinTable.on().trim());
	        
		} else {
			Table table = DOInfoReader.getTable(clazz);

			if(isSelect1) {
			    sql.append("1");
            } else {
                List<Field> fields = DOInfoReader.getColumnsForSelect(clazz, selectOnlyKey);
                sql.append(join(fields, ","));
            }

			sql.append(" FROM ").append(getTableName(clazz)).append(" ").append(table.alias());
		}
		
		return sql.toString();
	}
	
	/**
	 * select count(1) from t_table, 不包含where子句及以后的语句
	 * @param clazz
	 * @return
	 */
	public static String getSelectCountSQL(Class<?> clazz) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(*)");
		
		// 处理join方式clazz
		JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
		if(joinTable != null) {
			Field leftTableField = DOInfoReader.getJoinLeftTable(clazz);
			Field rightTableField = DOInfoReader.getJoinRightTable(clazz);
			
			JoinLeftTable joinLeftTable = leftTableField.getAnnotation(JoinLeftTable.class);
			JoinRightTable joinRightTable = rightTableField.getAnnotation(JoinRightTable.class);

	        sql.append(" FROM ").append(getTableName(leftTableField.getType()))
	           .append(" ").append(joinLeftTable.alias()).append(" ");
	        sql.append(joinTable.joinType().getCode()).append(" ");
	        sql.append(getTableName(rightTableField.getType())).append(" ").append(joinRightTable.alias());
	        if(joinTable.on().trim().isEmpty()) {
	        	throw new OnConditionIsNeedException("join table VO:" + clazz.getName());
	        }
	        sql.append(" on ").append(joinTable.on().trim());
	        
		} else {
			Table table = DOInfoReader.getTable(clazz);
			sql.append(" FROM ").append(getTableName(clazz)).append(" ").append(table.alias());
		}
		
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
		for(Object value : _keyValues) {
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
	public static String getKeyInWhereSQL(Class<?> clazz) {
		Field keyField = DOInfoReader.getOneKeyColumn(clazz);
		return autoSetSoftDeleted("WHERE " +
	           getColumnName(keyField.getAnnotation(Column.class)) + " in (?)", clazz);
	}
	
	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * @param t
	 * @param values 必须
	 * @param isWithNullValue 标记是否将null字段放到insert语句中
	 * @return
	 */
	public static <T> String getInsertSQL(T t, List<Object> values, boolean isWithNullValue) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");

        List<Field> fields = DOInfoReader.getColumns(t.getClass());

        sql.append(getTableName(t.getClass())).append(" (");
        List<Object> _values = new ArrayList<Object>(); // 之所以增加一个临时变量，是避免values初始不是空的易错情况
        String fieldSql = joinAndGetValue(fields, ",", _values, t, isWithNullValue);
        sql.append(fieldSql);
        sql.append(") VALUES ");
        String dotSql = "(" + join("?", _values.size(), ",") + ")";
        sql.append(dotSql);
        values.addAll(_values);

        return sql.toString();
	}

	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * @param list 要插入的数据，非空
	 * @param values 返回的参数列表
	 * @return 插入的SQL
	 */
	public static <T> String getInsertSQLForBatch(List<T> list, List<Object[]> values) {
		StringBuilder sql = new StringBuilder("INSERT INTO ");

		List<Field> fields = DOInfoReader.getColumns(list.get(0).getClass());

		sql.append(getTableName(list.get(0).getClass())).append(" (");

		for (int i = 0; i < list.size(); i++) {
			List<Object> _values = new ArrayList<>();
			String fieldSql = joinAndGetValue(fields, ",", _values, list.get(i), true);
			if (i == 0) {
				sql.append(fieldSql);
				sql.append(") VALUES ");
				String dotSql = "(" + join("?", _values.size(), ",") + ")";
				sql.append(dotSql);
			}
			values.add(_values.toArray());
		}

		return sql.toString();
	}
	
	/**
	 * 生成insert into (...) select ?,?,? from where not exists (select 1 from where)语句
	 * @param t
	 * @param values
	 * @param whereSql
	 * @return
	 */
	public static <T> String getInsertWhereNotExistSQL(T t, List<Object> values,
			boolean isWithNullValue, String whereSql) {
		StringBuilder sql = new StringBuilder("INSERT INTO ");
		
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		String tableName = getTableName(t.getClass());
		
		sql.append(tableName).append(" (");
		sql.append(joinAndGetValue(fields, ",", values, t, isWithNullValue));
		sql.append(") select ");
		sql.append(join("?", values.size(), ","));
		sql.append(" from dual where not exists (select 1 from ");
		
		if(!whereSql.trim().toUpperCase().startsWith("WHERE ")) {
			whereSql = "where " + whereSql;
		}
		whereSql = autoSetSoftDeleted(whereSql, t.getClass());
		
		sql.append(tableName).append(" ").append(whereSql).append(" limit 1)");
		
		return sql.toString();
	}
	
	/**
	 * 生成update语句
	 * @param t
	 * @param values
	 * @param withNull
	 * @param postSql
	 * @return 返回值为null表示不需要更新操作，这个是这个方法特别之处
	 */
	public static <T> String getUpdateSQL(T t, List<Object> values,
			boolean withNull, String postSql) {
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(t.getClass());
		
		sql.append(getTableName(t.getClass())).append(" SET ");
		
		List<Object> setValues = new ArrayList<Object>();
		String setSql = joinSetAndGetValue(notKeyFields, setValues, t, withNull);
		if(setValues.isEmpty()) {
			return null; // all field is empty, not need to update
		}
		sql.append(setSql);
		values.addAll(setValues);
		
		List<Object> whereValues = new ArrayList<Object>();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", whereValues, t);
		// 检查key值是否有null的，不允许有null
		for(Object v : whereValues) {
			if(v == null) {
				throw new NullKeyValueException();
			}
		}
		values.addAll(whereValues);

		Field casVersionField = DOInfoReader.getCasVersionColumn(t.getClass());
		if(casVersionField != null) {
			List<Field> casVersionFields = new ArrayList<Field>();
			casVersionFields.add(casVersionField);
			List<Object> casValues = new ArrayList<Object>();
			String casWhere = joinWhereAndGetValue(casVersionFields, "AND", casValues, t);
			if(casValues.size() != 1 || casValues.get(0) == null) {
				throw new CasVersionNotMatchException("casVersion column value is null");
			}
			values.add(casValues.get(0));
			where = where + " AND " + casWhere;
		}
		
		// 带上postSql
		if(postSql != null) {
			postSql = postSql.trim();
			if(!postSql.isEmpty()) {
				if(postSql.startsWith("where")) {
					postSql = " AND " + postSql.substring(5);
				}
				where = where + postSql;
			}
		}
		
		sql.append(autoSetSoftDeleted(where, t.getClass()));
		
		return sql.toString();
	}
	
	/**
	 * 获得批量更新sql
	 * @param clazz
	 * @param setSql
	 * @param whereSql
	 * @param extraWhereSql 会放在最后，以满足update子select语句的要求
	 * @return
	 */
	public static <T> String getUpdateAllSQL(Class<T> clazz, String setSql, String whereSql,
			String extraWhereSql) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		
		List<Field> fields = DOInfoReader.getColumns(clazz);
		
		sql.append(getTableName(clazz)).append(" ");
		
		if(setSql.trim().toLowerCase().startsWith("set ")) {
			sql.append(setSql);
		} else {
			sql.append("SET ").append(setSql);
		}
		
		// 加上更新时间和updateValueScript
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);

			if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
				sql.append(",").append(getColumnName(column))
				   .append("=").append(getDateString(new Date()));
			}

			String updateValueScript = column.updateValueScript().trim();
			if(!updateValueScript.isEmpty()) {
				Object value = ScriptUtils.getValueFromScript(column.ignoreScriptError(), updateValueScript);
				if(value != null) {
					sql.append(",").append(getColumnName(column)).append("=")
							.append(TypeAutoCast.toSqlValueStr(value));
				}
			}
		}
		
		sql.append(autoSetSoftDeleted(whereSql, clazz, extraWhereSql));
		return sql.toString();
	}

	/**
	 * 获得自定义更新的sql
	 * @param t
	 * @param values
	 * @param setSql
	 * @return
	 */
	public static <T> String getCustomUpdateSQL(T t, List<Object> values, String setSql) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		
		sql.append(getTableName(t.getClass())).append(" ");
		
		if(setSql.trim().toLowerCase().startsWith("set ")) {
			sql.append(setSql);
		} else {
			sql.append("SET ").append(setSql);
		}
		
		// 加上更新时间和casVersion字段、updateValueScript字段
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);

			if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
				sql.append(",").append(getColumnName(column))
				   .append("=").append(getDateString(new Date()));
			}

			if(column.casVersion()) {
				Object value = DOInfoReader.getValue(field, t);
				if(value == null) {
					throw new CasVersionNotMatchException("casVersion column value is null");
				}
				Long _v = null;
				if(value instanceof Long) {
					_v = (Long) value;
				} else if (value instanceof Integer) {
					_v = ((Integer) value).longValue();
				} else {
					throw new CasVersionNotMatchException("casVersion column value type must be Integer or Long");
				}
				sql.append(",").append(getColumnName(column)).append("=").append(_v + 1);
			}

			String updateValueScript = column.updateValueScript().trim();
			if(!updateValueScript.isEmpty()) {
				Object value = ScriptUtils.getValueFromScript(t, column.ignoreScriptError(), updateValueScript);
				if(value != null) {
					sql.append(",").append(getColumnName(column)).append("=")
							.append(TypeAutoCast.toSqlValueStr(value));
				}
			}
		}
		
		List<Object> whereValues = new ArrayList<Object>();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", whereValues, t);
		
		for(Object value : whereValues) {
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		values.addAll(whereValues);

		Field casVersionField = DOInfoReader.getCasVersionColumn(t.getClass());
		if(casVersionField != null) {
			List<Field> casVersionFields = new ArrayList<Field>();
			casVersionFields.add(casVersionField);
			List<Object> casValues = new ArrayList<Object>();
			String casWhere = joinWhereAndGetValue(casVersionFields, "AND", casValues, t);
			if(casValues.size() != 1 || casValues.get(0) == null) {
				throw new CasVersionNotMatchException("casVersion column value is null");
			}
			values.add(casValues.get(0));
			where = where + " AND " + casWhere;
		}
		
		sql.append(autoSetSoftDeleted(where, t.getClass()));
		
		return sql.toString();
	}
	
	/**
	 * 获得软删除SQL
	 * @param t
	 * @param values
	 * @return
	 */
	public static <T> String getSoftDeleteSQL(T t, Column softDeleteColumn, List<Object> values) {
		String setSql = getColumnName(softDeleteColumn) + "="
	                    + softDeleteColumn.softDelete()[1];

		// 处理deleteValueScript
        List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(t.getClass());
        for(Field field : notKeyFields) {
            Column column = field.getAnnotation(Column.class);

            String deleteValueScript = column.deleteValueScript().trim();
            if(!deleteValueScript.isEmpty()) {
                Object value = DOInfoReader.getValue(field, t);
                if(value != null) {
                    setSql = setSql + "," + getColumnName(column)
                            + "=" + TypeAutoCast.toSqlValueStr(value);
                }
            }
        }

		return getCustomDeleteSQL(t, values, setSql);
	}
	
	/**
	 * 获得自定义删除SQL
	 * @param clazz
	 * @param postSql
	 * @return
	 */
	public static <T> String getCustomDeleteSQL(Class<T> clazz, String postSql) {
		StringBuilder sql = new StringBuilder();

		sql.append("DELETE FROM ");
		sql.append(getTableName(clazz));
		
		sql.append(autoSetSoftDeleted(postSql, clazz));

		// 物理删除也执行deleteValueScript，但是不关心其返回值
        List<Field> fields = DOInfoReader.getColumns(clazz);
        for(Field field : fields) {
            Column column = field.getAnnotation(Column.class);

            String deleteValueScript = column.deleteValueScript().trim();
            if(!deleteValueScript.isEmpty()) {
                ScriptUtils.getValueFromScript(column.ignoreScriptError(), deleteValueScript);
            }
        }

        return sql.toString();
	}
	
	public static <T> String getCustomSoftDeleteSQL(Class<T> clazz, String postSql) {
		
		List<Field> fields = DOInfoReader.getColumns(clazz);
		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz);
		Column softDeleteColumn = softDelete.getAnnotation(Column.class);
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("UPDATE ").append(getTableName(clazz));
		sql.append(" SET ").append(getColumnName(softDeleteColumn));
		sql.append("=").append(softDeleteColumn.softDelete()[1]);
		
		// 特殊处理@Column setTimeWhenDelete时间，还有deleteValueScript
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.setTimeWhenDelete() && Date.class.isAssignableFrom(field.getType())) {
				sql.append(",").append(getColumnName(column)).append("='");
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				sql.append(df.format(new Date())).append("'");
			}

			String deleteValueScript = column.deleteValueScript().trim();
			if(!deleteValueScript.isEmpty()) {
			    Object value = ScriptUtils.getValueFromScript(column.ignoreScriptError(), deleteValueScript);
			    if(value != null) {
			        sql.append(",").append(getColumnName(column)).append("=")
                       .append(TypeAutoCast.toSqlValueStr(value));
                }
			}
		}

		sql.append(autoSetSoftDeleted(postSql, clazz));
		
		return sql.toString();
	}

	/**
	 * 获得自定义更新的sql
	 * @param t
	 * @param values
	 * @param setSql
	 * @return
	 */
	public static <T> String getCustomDeleteSQL(T t, List<Object> values, String setSql) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");

		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());

		sql.append(getTableName(t.getClass())).append(" ");

		if(setSql.trim().toLowerCase().startsWith("set ")) {
			sql.append(setSql);
		} else {
			sql.append("SET ").append(setSql);
		}

		// 加上删除时间
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.setTimeWhenDelete() && Date.class.isAssignableFrom(field.getType())) {
				sql.append(",").append(getColumnName(column))
						.append("=").append(getDateString(new Date()));
			}
		}

		List<Object> whereValues = new ArrayList<Object>();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", whereValues, t);

		for(Object value : whereValues) {
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		values.addAll(whereValues);

		sql.append(autoSetSoftDeleted(where, t.getClass()));

		return sql.toString();
	}


	/**
	 * 获得硬删除SQL
	 * @param t
	 * @param values
	 * @return
	 */
	public static <T> String getDeleteSQL(T t, List<Object> values) {
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("DELETE FROM ");
		sql.append(getTableName(t.getClass()));
		
		List<Object> _values = new ArrayList<Object>();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", _values, t);
		for(Object value : _values) { // 检查key的值是不是null
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		values.addAll(_values);
		
		sql.append(autoSetSoftDeleted(where, t.getClass()));
		
		return sql.toString();
	}

	/**
	 * 往where sql里面插入AND关系的表达式。
	 * 
	 * 例如：whereSql为 where a!=3 or a!=2 limit 1
	 *      condExpress为 deleted=0
	 * 那么返回：where (deleted=0 and (a!=3 or a!=2)) limit 1
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
		
		// 为解决JSqlParse对复杂的condExpression不支持的问题，这里用替换的形式来达到目的
	    String magic = "A" + UUID.randomUUID().toString().replace("-", "");
		
		String selectSql = "select * from dual "; // 辅助where sql解析用
		Statement statement = CCJSqlParserUtil.parse(selectSql + whereSql);
		Select selectStatement = (Select) statement;
		PlainSelect plainSelect = (PlainSelect)selectStatement.getSelectBody();
		
		Expression ce = CCJSqlParserUtil.parseCondExpression(magic);
		Expression oldWhere = plainSelect.getWhere();
		Expression newWhere = new FixedAndExpression(oldWhere, ce);
		plainSelect.setWhere(newWhere);
		
		String result = plainSelect.toString().substring(selectSql.length());
		return result.replace(magic, condExpression);
	}

	public static <T> String autoSetSoftDeleted(String whereSql, Class<?> clazz) {
		return autoSetSoftDeleted(whereSql, clazz, "");
	}
	
	/**
	 * 自动为【最后】where sql字句加上软删除查询字段
	 * @param whereSql 如果有where条件的，【必须】带上where关键字；如果是group by或空的字符串或null都可以
	 * @param clazz 要操作的DO类
	 * @param extraWhere 附带的where语句，会加进去，不能带where关键字，仅能是where的条件字句，该子句会放到最后
	 * @return 无论如何前面会加空格，更安全
	 */
	public static <T> String autoSetSoftDeleted(String whereSql, Class<?> clazz, String extraWhere) {
		if(whereSql == null) {
			whereSql = "";
		}
		extraWhere = extraWhere == null ? "" : extraWhere.trim();
		if(!extraWhere.isEmpty()) {
			extraWhere = "(" + extraWhere + ")";
		}
		
		String deletedExpression = "";
		
		// 处理join方式clazz
		JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
		if(joinTable != null) {
			Field leftTableField = DOInfoReader.getJoinLeftTable(clazz);
			Field rightTableField = DOInfoReader.getJoinRightTable(clazz);
			
			JoinLeftTable joinLeftTable = leftTableField.getAnnotation(JoinLeftTable.class);
			JoinRightTable joinRightTable = rightTableField.getAnnotation(JoinRightTable.class);
			
			Field softDeleteT1 = DOInfoReader.getSoftDeleteColumn(leftTableField.getType());
			Field softDeleteT2 = DOInfoReader.getSoftDeleteColumn(rightTableField.getType());
			
			if(softDeleteT1 == null && softDeleteT2 == null) {
				try {
					return " " + insertWhereAndExpression(whereSql, extraWhere);
				} catch (JSQLParserException e) {
					LOGGER.error("Bad sql syntax,whereSql:{},deletedExpression:{}",
							whereSql, deletedExpression, e);
					throw new BadSQLSyntaxException(e);
				}
			}
			
			StringBuilder deletedExpressionSb = new StringBuilder();
			if(softDeleteT1 != null) {
				Column softDeleteColumn = softDeleteT1.getAnnotation(Column.class);
				String columnName = getColumnName(softDeleteColumn);
				if(joinTable.joinType() == JoinTypeEnum.RIGHT_JOIN) {
					deletedExpressionSb.append("(").append(joinLeftTable.alias()).append(".").append(
						columnName + "=" + softDeleteColumn.softDelete()[0])
					   .append(" or ").append(joinLeftTable.alias()).append(".")
					   .append(columnName).append(" is null)");
				} else {
					deletedExpressionSb.append(joinLeftTable.alias()).append(".").append(
							columnName + "=" + softDeleteColumn.softDelete()[0]);
				}
			}
			
			if(softDeleteT2 != null) {
				if(softDeleteT1 != null) {
					deletedExpressionSb.append(" AND ");
				}
				Column softDeleteColumn = softDeleteT2.getAnnotation(Column.class);
				String columnName = getColumnName(softDeleteColumn);
				if(joinTable.joinType() == JoinTypeEnum.LEFT_JOIN) {
					deletedExpressionSb.append("(").append(joinRightTable.alias()).append(".").append(
							columnName + "=" + softDeleteColumn.softDelete()[0])
					    .append(" or ").append(joinRightTable.alias()).append(".")
					    .append(columnName).append(" is null)");
				} else {
					deletedExpressionSb.append(joinRightTable.alias()).append(".").append(
							columnName + "=" + softDeleteColumn.softDelete()[0]);
				}
			}
			
			deletedExpression = deletedExpressionSb.toString();		
		} else {
			Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz);
			if(softDelete == null) {
				try {
					return " " + insertWhereAndExpression(whereSql, extraWhere);
				} catch (JSQLParserException e) {
					LOGGER.error("Bad sql syntax,whereSql:{},deletedExpression:{}",
							whereSql, deletedExpression, e);
					throw new BadSQLSyntaxException(e);
				}
			}
			
			Column softDeleteColumn = softDelete.getAnnotation(Column.class);
			deletedExpression = getColumnName(softDeleteColumn) + "=" 
			                        + softDeleteColumn.softDelete()[0];
		}
		
		try {
			if(!extraWhere.isEmpty()) {
				deletedExpression = "(" + deletedExpression + " and " + extraWhere + ")";
			}
			return " " + SQLUtils.insertWhereAndExpression(whereSql, deletedExpression);
		} catch (JSQLParserException e) {
			LOGGER.error("Bad sql syntax,whereSql:{},deletedExpression:{}",
					whereSql, deletedExpression, e);
			throw new BadSQLSyntaxException(e);
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
	    return join(fields, sep, null);
    }
	
    /**
     * 拼凑select的field的语句
     * @param fields
     * @param sep
     * @param fieldPrefix
     * @return
     */
    private static String join(List<Field> fields, String sep, String fieldPrefix) {
    	return joinAndGetValueForSelect(fields, sep, fieldPrefix);
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
			Column column = fields.get(i).getAnnotation(Column.class);
			sb.append(getColumnName(column)).append("=?");
			if(i < fieldSize - 1) {
				sb.append(" ").append(logicOperate).append(" ");
			}
			Object val = DOInfoReader.getValue(fields.get(i), obj);
			if(val != null && column.isJSON()) {
				val = NimbleOrmJSON.toJson(val);
			}
			values.add(val);
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
			Column column = fields.get(i).getAnnotation(Column.class);
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
	    return joinAndGetValueForInsert(fields, sep, null, values, obj, isWithNullValue);
    }
    
    /**
     * 拼凑字段逗号,分隔子句（用于select）。会处理computed的@Column字段
     * @param fields
     * @param sep
     * @param fieldPrefix
     * @return
     */
	private static String joinAndGetValueForSelect(List<Field> fields, String sep, String fieldPrefix) {
        fieldPrefix = fieldPrefix == null ? "" : fieldPrefix.trim();

    	StringBuilder sb = new StringBuilder();
    	for(Field field : fields) {
    		Column column = field.getAnnotation(Column.class);
    		
    		String computed = column.computed().trim();
    		if(!computed.isEmpty()) {
    			sb.append("(").append(computed).append(") AS ");
    		} else {
    			sb.append(fieldPrefix); // 计算列不支持默认前缀，当join时，请自行区分计算字段的命名
    		}
        	sb.append(getColumnName(column)).append(sep);
    	}
    	int len = sb.length();
    	return len == 0 ? "" : sb.toString().substring(0, len - 1);
	}
    
    /**
     * 拼凑字段逗号,分隔子句（用于insert），并把参数obj的值放到values中。会排除掉computed的@Column字段
     * 
     * @param fields
     * @param sep
     * @param fieldPrefix
     * @param values 不应该为null
     * @param obj 不应该为null
     * @param isWithNullValue 是否把null值放到values中
     * @return
     */
	private static String joinAndGetValueForInsert(List<Field> fields, String sep, String fieldPrefix,
			List<Object> values, Object obj, boolean isWithNullValue) {
		if(values == null || obj == null) {
			throw new InvalidParameterException("joinAndGetValueForInsert require values and obj");
		}
		
        fieldPrefix = fieldPrefix == null ? "" : fieldPrefix.trim();

    	StringBuilder sb = new StringBuilder();
    	for(Field field : fields) {
    		Column column = field.getAnnotation(Column.class);
    		if(!(column.computed().trim().isEmpty())) {
    			continue; // insert不加入computed字段
    		}

			Object value = DOInfoReader.getValue(field, obj);
			if(value != null && column.isJSON()) {
				value = NimbleOrmJSON.toJson(value);
			}
			if(isWithNullValue) {
				values.add(value);
			} else {
				if(value == null) {
					continue; // 不加入该column
				} else {
					values.add(value);
				}
			}
    		
        	sb.append(fieldPrefix).append(getColumnName(column)).append(sep);
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
    
	/**
	 * 拼凑set子句，将会处理casVersion的字段自动+1
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
			Column column = fields.get(i).getAnnotation(Column.class);
			Object value = DOInfoReader.getValue(fields.get(i), obj);
			if(column.casVersion()) {
				if(value == null) {
					throw new CasVersionNotMatchException("casVersion column value is null");
				}
				Long _v = null;
				if(value instanceof Long) {
					_v = (Long) value;
				} else if (value instanceof Integer) {
					_v = ((Integer) value).longValue();
				} else {
					throw new CasVersionNotMatchException("casVersion column type must be Integer or Long");
				}
				sb.append(getColumnName(column)).append("=").append(_v + 1).append(",");
			} else {
				if(value != null && column.isJSON()) {
					value = NimbleOrmJSON.toJson(value);
				}
				if(withNull || value != null) {
					sb.append(getColumnName(column)).append("=?,");
					values.add(value);
				}
			}
		}
		return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
	}

	private static String getTableName(Class<?> clazz) {
		String tableName = DBHelperContext.getTableName(clazz);
		if(tableName != null) {
			return "`" + tableName + "`";
		}

		Table table = DOInfoReader.getTable(clazz);
		return "`" + table.value() + "`";
	}

	private static String getColumnName(Column column) {
		return "`" + column.value() + "`";
	}
	
	/**
	 * 输出类似：'2017-05-25 11:22:33'
	 * @param date
	 * @return
	 */
	private static String getDateString(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return "'" + df.format(date) + "'";
	}

}
