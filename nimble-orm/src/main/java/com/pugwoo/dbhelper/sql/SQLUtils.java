package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.annotation.*;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.exception.*;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.utils.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
	 * @param clazz 注解了Table的类
	 * @param selectOnlyKey 是否只查询key
	 * @param isSelect1 是否只select 1，不查询实际字段；当该值为true时，selectOnlyKey无效。
	 * @param features 将dbHelper的特性开关传入，用于处理生成的SQL
	 * @param postSql 将postSql传入，目前仅用于确定select 1字段的附加computed字段是否加入
	 * @return 返回拼凑返回的SQL
	 */
	public static String getSelectSQL(Class<?> clazz, boolean selectOnlyKey, boolean isSelect1,
									  Map<FeatureEnum, Boolean> features, String postSql,
									  DatabaseTypeEnum databaseType) {
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
				String computedColumnsForCountSelect = getComputedColumnsForCountSelect(
						leftTableField.getType(), joinLeftTable.alias() + ".", features, postSql);
				if (InnerCommonUtils.isNotBlank(computedColumnsForCountSelect)) {
					sql.append(",").append(computedColumnsForCountSelect);
				}
				computedColumnsForCountSelect = getComputedColumnsForCountSelect(
						rightTableField.getType(), joinRightTable.alias() + ".", features, postSql);
				if (InnerCommonUtils.isNotBlank(computedColumnsForCountSelect)) {
					sql.append(",").append(computedColumnsForCountSelect);
				}
            } else {
                List<Field> fields1 = DOInfoReader.getColumnsForSelect(leftTableField.getType(), selectOnlyKey);
                List<Field> fields2 = DOInfoReader.getColumnsForSelect(rightTableField.getType(), selectOnlyKey);
                sql.append(joinColumnForSelect(fields1,  joinLeftTable.alias() + ".", features));
                sql.append(",");
                sql.append(joinColumnForSelect(fields2,  joinRightTable.alias() + ".", features));
            }

	        sql.append(" FROM ").append(getTableName(leftTableField.getType()))
	           .append(" ").append(joinLeftTable.alias()).append(" ");
			if (databaseType == DatabaseTypeEnum.MYSQL && InnerCommonUtils.isNotBlank(joinLeftTable.forceIndex())) {
				sql.append(" FORCE INDEX(").append(joinLeftTable.forceIndex()).append(") ");
			}
	        sql.append(joinTable.joinType().getCode()).append(" ");
	        sql.append(getTableName(rightTableField.getType())).append(" ").append(joinRightTable.alias());
			if (databaseType == DatabaseTypeEnum.MYSQL && InnerCommonUtils.isNotBlank(joinRightTable.forceIndex())) {
				sql.append(" FORCE INDEX(").append(joinRightTable.forceIndex()).append(") ");
			}
	        if(InnerCommonUtils.isBlank(joinTable.on())) {
	        	throw new OnConditionIsNeedException("join table :" + clazz.getName());
	        }
	        sql.append(" on ").append(joinTable.on());
	        
		} else {
			Table table = DOInfoReader.getTable(clazz);
			if (InnerCommonUtils.isNotBlank(table.virtualTableSQL())) {
				if (InnerCommonUtils.isNotBlank(table.value())) {
					LOGGER.warn("table DO class:{} table name:{} is ignored because virtualTableSQL has value:{}",
							clazz, table.value(), table.virtualTableSQL());
				}
				return table.virtualTableSQL();
			}
			if (InnerCommonUtils.isNotBlank(table.virtualTablePath())) {
				if (InnerCommonUtils.isNotBlank(table.value())) {
					LOGGER.warn("table DO class:{} table name:{} is ignored because virtualTablePath has value:{}",
							clazz, table.value(), table.virtualTableSQL());
				}
				return InnerCommonUtils.readClasspathResourceAsString(table.virtualTablePath());
			}

			if(isSelect1) {
			    sql.append("1");
				String computedColumnsForCountSelect = getComputedColumnsForCountSelect(
						clazz, null, features, postSql);
				if (InnerCommonUtils.isNotBlank(computedColumnsForCountSelect)) {
					sql.append(",").append(computedColumnsForCountSelect);
				}
			} else {
                List<Field> fields = DOInfoReader.getColumnsForSelect(clazz, selectOnlyKey);
                sql.append(joinColumnForSelect(fields, null, features));
            }

			sql.append(" FROM ").append(getTableName(clazz)).append(" ").append(table.alias());
		}
		
		return sql.toString();
	}

	/**
	 * 获得计算列同时也是postSql中出现的列的Column的集合
	 */
	private static String getComputedColumnsForCountSelect(Class<?> clazz, String fieldPrefix,
													Map<FeatureEnum, Boolean> features, String postSql) {
		List<Field> fields = DOInfoReader.getColumnsForSelect(clazz, false);

		List<Field> field2 = new ArrayList<>();
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (column != null && InnerCommonUtils.isNotBlank(column.computed())) {
				// 这里用简单的postSql是否出现计算列的字符串来判断计算列是否要加入，属于放宽松的做法，程序不会有bug，但优化有空间
				if (postSql != null && postSql.contains(column.value())) {
					field2.add(field);
				}
			}
		}

		if (field2.isEmpty()) {
			return "";
		} else {
			return joinColumnForSelect(field2,  fieldPrefix, features);
		}
	}
	
	/**
	 * select count(1) from t_table, 不包含where子句及以后的语句
	 * @param clazz 注解了Table的表
	 * @return 生成的SQL
	 */
	public static String getSelectCountSQL(Class<?> clazz, DatabaseTypeEnum databaseType) {
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
			if (databaseType == DatabaseTypeEnum.MYSQL && InnerCommonUtils.isNotBlank(joinLeftTable.forceIndex())) {
				sql.append(" FORCE INDEX(").append(joinLeftTable.forceIndex()).append(") ");
			}
	        sql.append(joinTable.joinType().getCode()).append(" ");
	        sql.append(getTableName(rightTableField.getType())).append(" ").append(joinRightTable.alias());
			if (databaseType == DatabaseTypeEnum.MYSQL && InnerCommonUtils.isNotBlank(joinRightTable.forceIndex())) {
				sql.append(" FORCE INDEX(").append(joinRightTable.forceIndex()).append(") ");
			}
	        if(InnerCommonUtils.isBlank(joinTable.on())) {
	        	throw new OnConditionIsNeedException("join table VO:" + clazz.getName());
	        }
	        sql.append(" on ").append(joinTable.on());
	        
		} else {
			Table table = DOInfoReader.getTable(clazz);
			if (InnerCommonUtils.isNotBlank(table.virtualTableSQL())) {
				if (InnerCommonUtils.isNotBlank(table.value())) {
					LOGGER.warn("table DO class:{} table name:{} is ignored because virtualTable has value:{}",
							clazz, table.value(), table.virtualTableSQL());
				}
				sql.append(" FROM ( ").append(table.virtualTableSQL()).append(" )");
				return sql.toString();
			}
			if (InnerCommonUtils.isNotBlank(table.virtualTablePath())) {
				if (InnerCommonUtils.isNotBlank(table.value())) {
					LOGGER.warn("table DO class:{} table name:{} is ignored because virtualTablePath has value:{}",
							clazz, table.value(), table.virtualTablePath());
				}
				String vSQL = InnerCommonUtils.readClasspathResourceAsString(table.virtualTablePath());
				sql.append(" FROM ( ").append(vSQL).append(" )");
				return sql.toString();
			}

			sql.append(" FROM ").append(getTableName(clazz)).append(" ").append(table.alias());
		}
		
		return sql.toString();
	}
	
	/**
	 * 获得主键where子句，包含where关键字。会自动处理软删除条件
	 * 
	 * @param t 注解了Table的类的对象
	 * @param keyValues 返回传入sql的参数，如果提供list则写入
	 * @return 返回值前面会带空格，以确保安全。
	 * @throws NoKeyColumnAnnotationException 当t的类没有注解任何isKey=true的列时抛出
	 * @throws NullKeyValueException 当t中的主键都是null时抛出
	 */
	public static <T> String getKeysWhereSQL(T t, List<Object> keyValues) 
	    throws NoKeyColumnAnnotationException, NullKeyValueException {
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		
		List<Object> _keyValues = new ArrayList<>();
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
	 * 获得主键where子句，包含where关键字
	 * 
	 * @param clazz 注解了Table的类
	 * @throws NoKeyColumnAnnotationException 当没有注解isKey=1的列时抛出
	 */
	public static String getKeysWhereSQLWithoutSoftDelete(Class<?> clazz)
			throws NoKeyColumnAnnotationException {
		List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);
		String where = joinWhere(keyFields, "AND");
		return "WHERE " + where;
	}

	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * @param t 注解了Table的对象
	 * @param values 必须，要插入的参数值
	 * @param isWithNullValue 标记是否将null字段放到insert语句中
	 * @return 生成的SQL
	 */
	public static <T> String getInsertSQL(T t, List<Object> values, boolean isWithNullValue) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");

        List<Field> fields = DOInfoReader.getColumns(t.getClass());

        sql.append(getTableName(t.getClass())).append(" (");
        List<Object> _values = new ArrayList<>(); // 之所以增加一个临时变量，是避免values初始不是空的易错情况
		String insertSql = joinAndGetValueForInsert(fields, ",", _values, t, isWithNullValue);

		sql.append(insertSql);
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
	public static <T> InsertSQLForBatchDTO getInsertSQLForBatch(Collection<T> list, List<Object> values,
																DatabaseTypeEnum databaseType) {
		StringBuilder sql = new StringBuilder("INSERT INTO ");

		// 获得元素的class，list非空，因此clazz和t肯定有值
		Class<?> clazz = list.iterator().next().getClass();
		List<Field> fields = DOInfoReader.getColumns(clazz);

		// 根据list的值，只留下有值的field和非computed的列
		fields = filterFieldWithValue(fields, list);

		appendTableName(sql, clazz);
		appendInsertColumnSql(sql, fields);

		int sqlLogEndIndex = 0;
		int paramLogEndIndex = 0;
		boolean isFirst = true;
		for (T t : list) {
			sql.append(isFirst ? "VALUES" : ",");
			appendValueForBatchInsert(sql, fields, values, t, databaseType);
			if (isFirst) {
				sqlLogEndIndex = sql.length();
				paramLogEndIndex = values.size();
			}
			isFirst = false;
		}

		return new InsertSQLForBatchDTO(sql.toString(), sqlLogEndIndex, paramLogEndIndex);
	}

	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * @param list 要插入的数据，非空
	 * @param values 返回的参数列表
	 * @return 插入的SQL
	 */
	public static InsertSQLForBatchDTO getInsertSQLForBatch(String tableName, Collection<Map<String, Object>> list,
			List<Object> values, DatabaseTypeEnum databaseType) {
		StringBuilder sql = new StringBuilder("INSERT INTO `");
		sql.append(tableName.trim());
		sql.append("` (");

		boolean isFirst = true;
		int sqlLogEndIndex = 0;
		int paramLogEndIndex = 0;

		// 先从map获得所有的列
		Set<String> colSet = new HashSet<>();
		for (Map<String, Object> map : list) {
			colSet.addAll(map.keySet());
		}
		List<String> cols = new ArrayList<>(colSet);

		for (Map<String, Object> map : list) {
			StringBuilder sb = new StringBuilder("(");
			for (String col : cols) {
				Object value = map.get(col);
				if (value == null) {
					sb.append(SQLDialect.getInsertDefaultValue(databaseType));
				} else {
					sb.append("?");
					values.add(value);
				}
				sb.append(",");
			}
			if (isFirst) {
				for (int i = 0; i < cols.size(); i++) {
					if (i != 0) {
						sql.append(",");
					}
					sql.append("`").append(cols.get(i)).append("`");
				}
				sql.append(") VALUES ");
			} else {
				sql.append(",");
			}
			String dotSql = sb.substring(0, sb.length() - 1) + ")";
			sql.append(dotSql);

			if (isFirst) {
				sqlLogEndIndex = sql.length();
				paramLogEndIndex = values.size();
				isFirst = false;
			}
		}

		return new InsertSQLForBatchDTO(sql.toString(), sqlLogEndIndex, paramLogEndIndex);
	}

	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * @param values 返回的参数列表
	 * @return 插入的SQL
	 */
	public static InsertSQLForBatchDTO getInsertSQLForBatch(String tableName, List<String> cols,
															Collection<Object[]> list, DatabaseTypeEnum databaseType,
															List<Object> values) {
		StringBuilder sql = new StringBuilder("INSERT INTO `");
		sql.append(tableName.trim());
		sql.append("` (");

		boolean isFirst = true;
		int sqlLogEndIndex = 0;
		int paramLogEndIndex = 0;

		for (Object[] valueArray : list) {
			StringBuilder sb = new StringBuilder("(");
			for (Object value : valueArray) {
				if (value == null) {
					sb.append(SQLDialect.getInsertDefaultValue(databaseType));
				} else {
					sb.append("?");
					values.add(value);
				}
				sb.append(",");
			}
			if (isFirst) {
				for (int i = 0; i < cols.size(); i++) {
					if (i != 0) {
						sql.append(",");
					}
					sql.append("`").append(cols.get(i)).append("`");
				}
				sql.append(") VALUES ");
			} else {
				sql.append(",");
			}
			String dotSql = sb.substring(0, sb.length() - 1) + ")";
			sql.append(dotSql);

			if (isFirst) {
				sqlLogEndIndex = sql.length();
				paramLogEndIndex = values.size();
				isFirst = false;
			}
		}

		return new InsertSQLForBatchDTO(sql.toString(), sqlLogEndIndex, paramLogEndIndex);
	}

	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * 说明：这种方式是交给jdbc驱动来处理批量插入。
	 *
	 * @param list 要插入的数据，非空
	 * @param values 返回的参数列表
	 * @return 插入的SQL
	 */
	public static <T> String getInsertSQLForBatchForJDBCTemplate(Collection<T> list, List<Object[]> values) {
		StringBuilder sql = new StringBuilder("INSERT INTO ");

		// 获得元素的class，list非空，因此clazz和t肯定有值
		Class<?> clazz = list.iterator().next().getClass();
		List<Field> fields = DOInfoReader.getColumns(clazz);

		// 根据list的值，只留下有值的field和非computed的列
		fields = filterFieldWithValue(fields, list);

		appendTableName(sql, clazz);
		sql.append(" (");

		boolean isFirst = true;
		for (T t : list) {
			List<Object> _values = new ArrayList<>();
			String insertSql = joinAndGetValueForInsert(fields, ",", _values, t, true);
			if (isFirst) {
				sql.append(insertSql);
				sql.append(") VALUES ");
				String dotSql = "(" + join("?", _values.size(), ",") + ")";
				sql.append(dotSql);
			}
			isFirst = false;
			values.add(_values.toArray());
		}

		return sql.toString();
	}

	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * 说明：这种方式是交给jdbc驱动来处理批量插入。
	 *
	 * @param tableName 要插入的表名
	 * @param list 列和数据的集合
	 * @param values 返回的参数列表
	 * @return 插入的SQL
	 */
	public static String getInsertSQLForBatchForJDBCTemplate(String tableName,
			Collection<Map<String, Object>> list, List<Object[]> values) {
		StringBuilder sql = new StringBuilder("INSERT INTO `");
		sql.append(tableName.trim());
		sql.append("` (");

		// 先从map获得所有的列
		Set<String> colSet = new HashSet<>();
		for (Map<String, Object> map : list) {
			colSet.addAll(map.keySet());
		}

		List<String> cols = new ArrayList<>(colSet);
		boolean isFirst = true;
		for (Map<String, Object> map : list) {
			List<Object> _values = new ArrayList<>();
			for (String col : cols) {
				_values.add(map.get(col));
			}
			if (isFirst) {
				boolean isColFirst = true;
				for (String col : cols) {
					if (!isColFirst) {
						sql.append(",");
					} else {
						isColFirst = false;
					}
					sql.append("`").append(col.trim()).append("`");
				}

				sql.append(") VALUES ");
				String dotSql = "(" + join("?", _values.size(), ",") + ")";
				sql.append(dotSql);
				isFirst = false;
			}
			values.add(_values.toArray());
		}

		return sql.toString();
	}

	/**
	 * 生成insert语句insert into (...) values (?,?,?)，将值放到values中。
	 * 说明：这种方式是交给jdbc驱动来处理批量插入。
	 *
	 * @param tableName 要插入的表名
	 * @param cols 列和列表
	 * @return 插入的SQL
	 */
	public static String getInsertSQLForBatchForJDBCTemplate(String tableName, List<String> cols) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO `").append(tableName.trim())
				.append("` (");
		boolean isFirst = true;
		for(String col : cols) {
			if (!isFirst) {
				sql.append(",");
			} else {
				isFirst = false;
			}
			sql.append("`").append(col.trim()).append("`");
		}
		sql.append(") VALUES (").append(join("?", cols.size(), ",")).append(")");
		return sql.toString();
	}

	private static <T> List<Field> filterFieldWithValue(List<Field> fields, Collection<T> list) {
		fields = InnerCommonUtils.filter(fields, o -> {
			Column column = o.getAnnotation(Column.class);
			return column != null && InnerCommonUtils.isBlank(column.computed());
		});

		List<Field> result = new ArrayList<>();
		for (Field field : fields) {
			for (T t : list) {
				if (DOInfoReader.getValue(field, t) != null) {
					result.add(field);
					break;
				}
			}
		}
		return result;
	}

	private static void appendValueForBatchInsert(StringBuilder sb, List<Field> fields, List<Object> values,
												  Object obj, DatabaseTypeEnum databaseType) {
		if(values == null || obj == null) {
			throw new InvalidParameterException("joinAndGetValueForInsert require values and obj");
		}

		sb.append("(");
		for(int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			Field field = fields.get(i);
			Column column = field.getAnnotation(Column.class);
			if(InnerCommonUtils.isNotBlank(column.computed())) {
				continue; // insert不加入computed字段
			}

			Object value = DOInfoReader.getValue(field, obj);
			if(value != null && column.isJSON()) {
				value = NimbleOrmJSON.toJson(value);
			}
			if (value == null) {
				sb.append(SQLDialect.getInsertDefaultValue(databaseType));
			} else {
				sb.append("?");
				values.add(value);
			}
		}
		sb.append(")");
	}

	public static class BatchUpdateResultDTO {
		private String sql;
		private String logSql;
		private List<Object> logParams;

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public String getLogSql() {
			return logSql;
		}

		public void setLogSql(String logSql) {
			this.logSql = logSql;
		}

		public List<Object> getLogParams() {
			return logParams;
		}

		public void setLogParams(List<Object> logParams) {
			this.logParams = logParams;
		}
	}

	/**
	 * 生成批量update的sql
	 * @param list 要更新的对象
	 * @param values sql中对应的参数
	 * @param casVersionColumn cas版本号列，如果为null则表示没有
	 * @param keyColumn 主键列，目前的主键列只有一列，在外层调用时限制了
	 * @param notKeyColumns 非主键列
	 * @return 批量update的sql；如果返回空字符串表示不需要更新，且应该当成功处理
	 */
	public static <T> BatchUpdateResultDTO getBatchUpdateSQL(
			Collection<T> list, List<Object> values, Field casVersionColumn,
			Field keyColumn, List<Field> notKeyColumns, Class<?> clazz) {

		// 1. 找出所有的主键的值，如果值为null，这抛出异常
		List<Object> keys = new ArrayList<>(list.size());
		for (T t : list) {
			Object key = DOInfoReader.getValue(keyColumn, t);
			if (key == null) {
				throw new NullKeyValueException("class:" + t.getClass().getName() + ",values:" + NimbleOrmJSON.toJson(t));
			}
			keys.add(key);
		}

		// 2. 找出所有非null的字段，对于list中已经全是null的字段，不参与update
		List<Field> notKeyNotNullFields = new ArrayList<>();
		for (Field field : notKeyColumns) {
			for (T t : list) {
				if (DOInfoReader.getValue(field, t) != null) {
					notKeyNotNullFields.add(field);
					break;
				}
			}
		}
		if (casVersionColumn == null && notKeyNotNullFields.isEmpty()) {
			BatchUpdateResultDTO dto = new BatchUpdateResultDTO();
			dto.setSql("");
			return dto; // 没有需要更新的列
		}

		StringBuilder sql = new StringBuilder();
		StringBuilder logSql = new StringBuilder();
		List<Object> logParams = new ArrayList<>();

		// 3. 生成update语句
		sql.append("UPDATE ").append(getTableName(clazz)).append(" SET ");
		logSql.append(sql);

		boolean isFirst = true;
		for (Field field : notKeyNotNullFields) {
			if (isFirst) {
				isFirst = false;
			} else {
				sql.append(",");
				logSql.append(",");
			}

			sql.append(getColumnName(field)).append("=(CASE");
			logSql.append(getColumnName(field)).append("=(CASE");

			boolean isFirstT = true;
			for (T t : list) {
				StringBuilder sqlOld = null;
				List<Object> valuesOld = null;
				if (isFirstT) {
					sqlOld = sql;
					valuesOld = values;
					sql = new StringBuilder();
					values = new ArrayList<>();
				}

				if (casVersionColumn == null) { // 没有CAS的场景
					sql.append(" WHEN ").append(getColumnName(keyColumn)).append("=? THEN ");
					values.add(DOInfoReader.getValue(keyColumn, t));

					Object value = DOInfoReader.getValue(field, t);
					if (value == null) {
						sql.append(getColumnName(field));
					} else {
						if (field.getAnnotation(Column.class).isJSON()) {
							value = NimbleOrmJSON.toJson(value);
						}
						sql.append("?");
						values.add(value);
					}
				} else {
					sql.append(" WHEN ").append(getColumnName(keyColumn)).append("=? AND ")
							.append(getColumnName(casVersionColumn)).append("=? THEN ");
					values.add(DOInfoReader.getValue(keyColumn, t));
					values.add(DOInfoReader.getValue(casVersionColumn, t));

					Object value = DOInfoReader.getValue(field, t);
					if (value == null) {
						sql.append(getColumnName(field));
					} else {
						if (field.getAnnotation(Column.class).isJSON()) {
							value = NimbleOrmJSON.toJson(value);
						}
						sql.append("?");
						values.add(value);
					}

					sql.append(" WHEN ").append(getColumnName(keyColumn)).append("=? AND ")
							.append(getColumnName(casVersionColumn)).append("!=? THEN ");
					values.add(DOInfoReader.getValue(keyColumn, t));
					values.add(DOInfoReader.getValue(casVersionColumn, t));
					sql.append(getColumnName(field));
				}

				if (isFirstT) {
					logSql.append(sql);
					sqlOld.append(sql);
					valuesOld.addAll(values);
					logParams.addAll(values);
					sql = sqlOld;
					values = valuesOld;
					isFirstT = false;
				}
			}

			sql.append(" END)");
			logSql.append(" END)");
		}

		// 最后再追加CAS列的更新
		if (casVersionColumn != null) {
			if (!isFirst) {
				sql.append(",");
				logSql.append(",");
			}
			sql.append(getColumnName(casVersionColumn)).append("=(CASE");
			logSql.append(getColumnName(casVersionColumn)).append("=(CASE");

			boolean isFirstT = true;
			for (T t : list) {
				StringBuilder sqlOld = null;
				List<Object> valuesOld = null;
				if (isFirstT) {
					sqlOld = sql;
					valuesOld = values;
					sql = new StringBuilder();
					values = new ArrayList<>();
				}

				sql.append(" WHEN ").append(getColumnName(keyColumn)).append("=? AND ")
						.append(getColumnName(casVersionColumn)).append("=? THEN ")
						.append(getColumnName(casVersionColumn)).append("+1");
				values.add(DOInfoReader.getValue(keyColumn, t));
				values.add(DOInfoReader.getValue(casVersionColumn, t));
				sql.append(" WHEN ").append(getColumnName(keyColumn)).append("=? AND ")
						.append(getColumnName(casVersionColumn)).append("!=? THEN ")
						.append(getColumnName(casVersionColumn));
				values.add(DOInfoReader.getValue(keyColumn, t));
				values.add(DOInfoReader.getValue(casVersionColumn, t));

				if (isFirstT) {
					logSql.append(sql);
					sqlOld.append(sql);
					valuesOld.addAll(values);
					logParams.addAll(values);
					sql = sqlOld;
					values = valuesOld;
					isFirstT = false;
				}
			}

			sql.append(" END)");
			logSql.append(" END)");
		}

		String where = "WHERE " + getColumnName(keyColumn) + " IN (?)";
		values.add(keys);

		// 对于casVersion，要将cas版本加入到where子句中，因为返回的affected rows应该是where match到的行数，而不是实际修改的行数
		if (casVersionColumn != null) {
			where += " AND (" + getColumnName(keyColumn) + "," + getColumnName(casVersionColumn) + ") IN (?)";
			List<Object[]> idAndCas = new ArrayList<>();
			for (T t : list) {
				idAndCas.add(new Object[]{
						DOInfoReader.getValue(keyColumn, t),
						DOInfoReader.getValue(casVersionColumn, t)
				});
			}
			values.add(idAndCas);
		}

		where = autoSetSoftDeleted(where, clazz);
		sql.append(where);
		logSql.append(where);

		BatchUpdateResultDTO dto = new BatchUpdateResultDTO();
		dto.setSql(sql.toString());
		dto.setLogSql(logSql.toString());
		dto.setLogParams(logParams);

		return dto;
	}

	/**
	 * 生成update语句
	 * @param t 注解了Table的对象
	 * @param values 要更新的值
	 * @param withNull 是否更新null值
	 * @param postSql 附带的where子句
	 * @return 返回值为null表示不需要更新操作，这个是这个方法特别之处
	 */
	public static <T> String getUpdateSQL(T t, List<Object> values, boolean withNull, String postSql) {
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(t.getClass());
		
		sql.append(getTableName(t.getClass())).append(" SET ");
		
		List<Object> setValues = new ArrayList<>();
		String setSql = joinSetAndGetValue(notKeyFields, setValues, t, withNull);
		if(setValues.isEmpty()) {
			return null; // all field is empty, not need to update
		}
		sql.append(setSql);
		values.addAll(setValues);
		
		List<Object> whereValues = new ArrayList<>();
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
			List<Field> casVersionFields = new ArrayList<>();
			casVersionFields.add(casVersionField);
			List<Object> casValues = new ArrayList<>();
			String casWhere = joinWhereAndGetValue(casVersionFields, "AND", casValues, t);
			if(casValues.size() != 1 || casValues.get(0) == null) {
				throw new CasVersionNotMatchException("casVersion column value is null");
			}
			values.add(casValues.get(0));
			where = where + " AND " + casWhere;
		}
		
		// 带上postSql
		if(postSql != null) {
			postSql = postSql.trim(); // 这里必须要trim,因为下面靠startsWith判断是否从where开头
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
	 * @param clazz 注解了Table的类
	 * @param setSql update的set子句
	 * @param whereSql 附带的where子句
	 * @param extraWhereSql 会放在最后，以满足update子select语句的要求
	 * @return 生成的SQL
	 */
	public static <T> String getUpdateAllSQL(Class<T> clazz, String setSql, String whereSql,
			String extraWhereSql) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		
		List<Field> fields = DOInfoReader.getColumns(clazz);
		
		sql.append(getTableName(clazz)).append(" ");
		
		if(setSql.trim().toLowerCase().startsWith("set ")) { // 这里必须有trim()
			sql.append(setSql);
		} else {
			sql.append("SET ").append(setSql);
		}
		
		// 加上更新时间和updateValueScript
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);

			if(column.setTimeWhenUpdate()) {
				String nowDateTime = PreHandleObject.getNowDateTime(field.getType());
				if (nowDateTime != null) {
					sql.append(",").append(getColumnName(column))
						.append("='").append(nowDateTime).append("'");
				}
			}

			if(InnerCommonUtils.isNotBlank(column.updateValueScript())) {
				Object value = ScriptUtils.getValueFromScript(column.ignoreScriptError(), column.updateValueScript());
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
	 * @param t 注解了Table的对象
	 * @param values 要update的参数值
	 * @param setSql set子句SQL
	 * @return 生成的SQL
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

			if(column.setTimeWhenUpdate()) {
				String nowDateTime = PreHandleObject.getNowDateTime(field.getType());
				if (nowDateTime != null) {
					sql.append(",").append(getColumnName(column))
							.append("='").append(nowDateTime).append("'");
				}
			}

			if(column.casVersion()) {
				Object value = DOInfoReader.getValue(field, t);
				if(value == null) {
					throw new CasVersionNotMatchException("casVersion column value is null");
				}
				long _v;
				if(value instanceof Long) {
					_v = (Long) value;
				} else if (value instanceof Integer) {
					_v = ((Integer) value).longValue();
				} else {
					throw new CasVersionNotMatchException("casVersion column value type must be Integer or Long");
				}
				sql.append(",").append(getColumnName(column)).append("=").append(_v + 1);
			}

			if(InnerCommonUtils.isNotBlank(column.updateValueScript())) {
				Object value = ScriptUtils.getValueFromScript(t, column.ignoreScriptError(), column.updateValueScript());
				if(value != null) {
					sql.append(",").append(getColumnName(column)).append("=")
							.append(TypeAutoCast.toSqlValueStr(value));
				}
			}
		}
		
		List<Object> whereValues = new ArrayList<>();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", whereValues, t);
		
		for(Object value : whereValues) {
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		values.addAll(whereValues);

		Field casVersionField = DOInfoReader.getCasVersionColumn(t.getClass());
		if(casVersionField != null) {
			List<Field> casVersionFields = new ArrayList<>();
			casVersionFields.add(casVersionField);
			List<Object> casValues = new ArrayList<>();
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
	 * @param t 注解了Table的对象
	 * @param values 要传回给调用方的更新值
	 * @return 生成的SQL
	 */
	public static <T> String getSoftDeleteSQL(T t, Column softDeleteColumn, List<Object> values) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		sql.append(getTableName(t.getClass())).append(" SET ");
		sql.append(getColumnName(softDeleteColumn)).append("=").append(softDeleteColumn.softDelete()[1]);

		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			// 加上删除时间
			if(column.setTimeWhenDelete()) {
				String nowDateTime = PreHandleObject.getNowDateTime(field.getType());
				if (nowDateTime != null) {
					sql.append(",").append(getColumnName(column))
							.append("='").append(nowDateTime).append("'");
				}
			}
			// 处理deleteValueScript
			if(InnerCommonUtils.isNotBlank(column.deleteValueScript())) {
				// 这里不需要再执行deleteValueScript脚本了 ，因为前面preHandleDelete已经执行了
				Object value = DOInfoReader.getValue(field, t);
				if(value != null) {
					sql.append(",").append(getColumnName(column))
							.append("=").append(TypeAutoCast.toSqlValueStr(value));
				}
			}
		}

		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		List<Object> whereValues = new ArrayList<>();
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
	 * 获得自定义删除SQL，给物理删除的
	 */
	public static <T> String getCustomDeleteSQL(Class<T> clazz, String postSql) {
		return "DELETE FROM " + getTableName(clazz) + " " + postSql;
	}

	public static <T> String getCustomSoftDeleteSQL(Class<T> clazz, String postSql, Field softDelete) {
		
		List<Field> fields = DOInfoReader.getColumns(clazz);
		Column softDeleteColumn = softDelete.getAnnotation(Column.class);
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("UPDATE ").append(getTableName(clazz));
		sql.append(" SET ").append(getColumnName(softDeleteColumn));
		sql.append("=").append(softDeleteColumn.softDelete()[1]);
		
		// 特殊处理@Column setTimeWhenDelete时间
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.setTimeWhenDelete()) {
				String nowDateTime = PreHandleObject.getNowDateTime(field.getType());
				if (nowDateTime != null) {
					sql.append(",").append(getColumnName(column)).append("='");
					sql.append(nowDateTime).append("'");
				}
			}
		}

		sql.append(autoSetSoftDeleted(postSql, clazz));
		
		return sql.toString();
	}

	/**
	 * 获得硬删除SQL
	 */
	public static <T> String getDeleteSQL(T t, List<Object> values) {
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("DELETE FROM ");
		sql.append(getTableName(t.getClass()));
		
		List<Object> _values = new ArrayList<>();
		String where = "WHERE " + joinWhereAndGetValue(keyFields, "AND", _values, t);
		for(Object value : _values) { // 检查key的值是不是null
			if(value == null) {
				throw new NullKeyValueException();
			}
		}
		values.addAll(_values);

		sql.append(where);
		return sql.toString();
	}

	/**
	 * 往where sql里面插入AND关系的表达式。
	 * 例如：whereSql为 where a!=3 or a!=2 limit 1
	 *      condExpress为 deleted=0
	 * 那么返回：where (deleted=0 and (a!=3 or a!=2)) limit 1
	 * 
	 * @param whereSql 从where起的sql子句，如果有where必须带上where关键字。
	 * @param condExpression 例如a=?  不带where或and关键字。
	 * @return 注意返回字符串前面没有空格
	 * @throws JSQLParserException SQL解析错误时抛出
	 */
	public static String insertWhereAndExpression(String whereSql, String condExpression) 
			throws JSQLParserException {
		
		if(InnerCommonUtils.isBlank(condExpression)) {
			return whereSql == null ? "" : whereSql;
		}
		if(InnerCommonUtils.isBlank(whereSql)) {
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

	public static String autoSetSoftDeleted(String whereSql, Class<?> clazz) {
		return autoSetSoftDeleted(whereSql, clazz, "");
	}

	private static String _getDefaultOrderBy(Class<?> clazz, String prefix) {
		List<Field> orderColumn = DOInfoReader.getKeyColumnsNoThrowsException(clazz);
		if (orderColumn.isEmpty()) { // 如果没有主键，那么全字段排序
			orderColumn = DOInfoReader.getColumns(clazz);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < orderColumn.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(prefix).append(getColumnName(orderColumn.get(i)));
		}
		return sb.toString();
	}

	private static String getDefaultOrderBy(Class<?> clazz) {
		JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
		if (joinTable == null) {
			return "ORDER BY " + _getDefaultOrderBy(clazz, "");
		} else {
			Field leftTableField = DOInfoReader.getJoinLeftTable(clazz);
			Field rightTableField = DOInfoReader.getJoinRightTable(clazz);
			JoinLeftTable joinLeftTable = leftTableField.getAnnotation(JoinLeftTable.class);
			JoinRightTable joinRightTable = rightTableField.getAnnotation(JoinRightTable.class);
			String orderBy1 = _getDefaultOrderBy(leftTableField.getType(), joinLeftTable.alias() + ".");
			String orderBy2 = _getDefaultOrderBy(rightTableField.getType(), joinRightTable.alias() + ".");
			return "ORDER BY " + orderBy1 + "," + orderBy2;
		}
	}

	private static List<OrderByElement> _getDefaultOrderByElement(Class<?> clazz, String prefix) {
		List<Field> orderColumn = DOInfoReader.getKeyColumnsNoThrowsException(clazz);
		if (orderColumn.isEmpty()) { // 如果没有主键，那么全字段排序
			orderColumn = DOInfoReader.getColumns(clazz);
		}
		List<OrderByElement> list = new ArrayList<>();
		for (Field field : orderColumn) {
			OrderByElement ele = new OrderByElement();
			Column column = field.getAnnotation(Column.class);
			if (InnerCommonUtils.isBlank(column.computed())) {
				ele.setExpression(new net.sf.jsqlparser.schema.Column(prefix + getColumnName(field)));
			} else {
				ele.setExpression(new net.sf.jsqlparser.schema.Column(getColumnName(field, prefix)));
			}
			list.add(ele);
		}
		return list;
	}

	private static List<OrderByElement> getDefaultOrderByElement(Class<?> clazz) {
		JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
		if (joinTable == null) {
			return _getDefaultOrderByElement(clazz, "");
		} else {
			Field leftTableField = DOInfoReader.getJoinLeftTable(clazz);
			Field rightTableField = DOInfoReader.getJoinRightTable(clazz);
			JoinLeftTable joinLeftTable = leftTableField.getAnnotation(JoinLeftTable.class);
			JoinRightTable joinRightTable = rightTableField.getAnnotation(JoinRightTable.class);
			List<OrderByElement> list = new ArrayList<>();
			list.addAll(_getDefaultOrderByElement(leftTableField.getType(), joinLeftTable.alias() + "."));
			list.addAll(_getDefaultOrderByElement(rightTableField.getType(), joinRightTable.alias() + "."));
			return list;
		}
	}

	private static List<OrderByElement> getDefaultOrderByGroup(List<Expression> groupByList) {
		List<OrderByElement> list = new ArrayList<>();
		for (Expression expression : groupByList) {
			OrderByElement ele = new OrderByElement();
			ele.setExpression(new net.sf.jsqlparser.schema.Column(expression.getASTNode().jjtGetValue().toString()));
			list.add(ele);
		}
		return list;
	}

	/**
	 * 移除whereSql中的limit子句；检查并加上order by子句
	 */
	public static String removeLimitAndAddOrder(String whereSql, boolean autoAddOrderForPagination, Class<?> clazz) {
		// 当查询条件是空字符串时，默认带上order by主键
		if (InnerCommonUtils.isBlank(whereSql) && autoAddOrderForPagination) {
			return getDefaultOrderBy(clazz);
		}

		String selectSql = "SELECT * FROM dual "; // 辅助where sql解析用，这个大小写不能改动！
		Statement statement;
		try {
			statement = CCJSqlParserUtil.parse(selectSql + whereSql);
		} catch (JSQLParserException e) {
			LOGGER.error("fail to parse sql:{}", whereSql, e);
			return whereSql;
		}
		boolean isChange = false;
		Select selectStatement = (Select) statement;
		PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();

		// 移除limit
		Limit limit = plainSelect.getLimit();
		if (limit != null) {
			plainSelect.setLimit(null);
			isChange = true;
		}

		// 自动加order by
		if (autoAddOrderForPagination) {
			List<OrderByElement> orderBy = plainSelect.getOrderByElements();
			GroupByElement groupBy = plainSelect.getGroupBy();
			ExpressionList groupBys = groupBy == null ? null : groupBy.getGroupByExpressionList();
			List<Expression> groupByList = groupBys == null ? null : groupBys.getExpressions();
			if (orderBy == null || orderBy.isEmpty()) {
				if (groupByList == null || groupByList.isEmpty()) {
					plainSelect.setOrderByElements(getDefaultOrderByElement(clazz));
				} else {
					plainSelect.setOrderByElements(getDefaultOrderByGroup(groupByList));
				}
				isChange = true;
			} else { // 如果用户自己指定了order by，那么不处理
				if (groupByList != null) {
					// 对于有group的情况，检查一下order by字段是否完成包含了group by字段
					for (Expression groupName : groupByList) {
						String name = groupName.getASTNode().jjtGetValue().toString();
						boolean isFound = false;
						for (OrderByElement order : orderBy) {
							if (order.getExpression().getASTNode().getClass().toString().equals(name)) {
								isFound = true;
								break;
							}
						}
						if (!isFound) {
							LOGGER.warn("class:{} postSql:[{}], group by field:{} not in order by list,"
											+ " it may cause unstable pagination result.",
									clazz, whereSql, name);
						}
					}
				}
			}
		}

		if (isChange) {
			String sql = plainSelect.toString();
			if (sql.startsWith(selectSql)) {
				return sql.substring(selectSql.length());
			} else {
				LOGGER.error("fail to remove limit and handle order by for sql:{}", whereSql);
				return whereSql;
			}
		} else {
			return whereSql; // 没变化
		}
	}

	/**
	 * 自动为【最后】where sql字句加上软删除查询字段。
	 * 说明：不支持virtualTable虚拟表。
	 *
	 * @param whereSql 如果有where条件的，【必须】带上where关键字；如果是group by或空的字符串或null都可以
	 * @param clazz 要操作的DO类
	 * @param extraWhere 附带的where语句，会加进去，不能带where关键字，仅能是where的条件字句，该子句会放到最后
	 * @return 无论如何前面会加空格，更安全
	 */
	public static String autoSetSoftDeleted(String whereSql, Class<?> clazz, String extraWhere) {
		if (whereSql == null) {
			whereSql = "";
		}
		extraWhere = extraWhere == null ? "" : extraWhere;
		if(InnerCommonUtils.isNotBlank(extraWhere)) {
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
					deletedExpressionSb.append("(").append(joinLeftTable.alias()).append(".")
							.append(columnName).append("=").append(softDeleteColumn.softDelete()[0])
					   .append(" or ").append(joinLeftTable.alias()).append(".")
					   .append(columnName).append(" is null)");
				} else {
					deletedExpressionSb.append(joinLeftTable.alias()).append(".")
							.append(columnName).append("=").append(softDeleteColumn.softDelete()[0]);
				}
			}
			
			if(softDeleteT2 != null) {
				if(softDeleteT1 != null) {
					deletedExpressionSb.append(" AND ");
				}
				Column softDeleteColumn = softDeleteT2.getAnnotation(Column.class);
				String columnName = getColumnName(softDeleteColumn);
				if(joinTable.joinType() == JoinTypeEnum.LEFT_JOIN) {
					deletedExpressionSb.append("(").append(joinRightTable.alias()).append(".")
							.append(columnName).append("=").append(softDeleteColumn.softDelete()[0])
					    .append(" or ").append(joinRightTable.alias()).append(".")
					    .append(columnName).append(" is null)");
				} else {
					deletedExpressionSb.append(joinRightTable.alias()).append(".")
							.append(columnName).append("=").append(softDeleteColumn.softDelete()[0]);
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
	 * @return 生成的SQL
	 */
	public static String genLimitSQL(Integer offset, Integer limit) {
		StringBuilder sb = new StringBuilder();
		if (limit != null) {
			sb.append(" LIMIT ");
			sb.append(limit);
			if(offset != null) {
				sb.append(" OFFSET ").append(offset);
			}
		}
		return sb.toString();
	}

	/**
	 * 拿到computed SQL在特性开关的情况下的返回值。说明：调用此方法请确保计算列是非空的。
	 * @param column 列注解
	 * @param features 特性开关map
	 * @return 返回计算列的结果SQL
	 */
	public static String getComputedColumn(Column column, Map<FeatureEnum, Boolean> features) {
		String computed = column.computed();

		Boolean autoSumNullToZero = features.get(FeatureEnum.AUTO_SUM_NULL_TO_ZERO);
		if (autoSumNullToZero != null && autoSumNullToZero) {
			String computedLower = computed.toLowerCase().trim();
			if (computedLower.startsWith("sum(") && computedLower.endsWith(")")) {
				computed = "COALESCE(" + computed + ",0)";
			}
		}

		return computed;
	}

	/**
	 * 判断postSql是否包含了limit子句
	 * @param postSql 从where开始的子句
	 * @return 是返回true，否返回false；如果解析异常返回false
	 */
	public static boolean isContainsLimit(String postSql) throws JSQLParserException {
		Boolean result = containsLimitCache.get(postSql);
		if (result != null) {
			return result;
		}

		String selectSql = "select * from dual "; // 辅助where sql解析用

		Statement statement = CCJSqlParserUtil.parse(selectSql + postSql);
		Select selectStatement = (Select) statement;
		PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
		Limit limit = plainSelect.getLimit();
		boolean isContainsLimit = limit != null;
		containsLimitCache.put(postSql, isContainsLimit); // 这里能用缓存是因为该postSql来自于注解，数量固定
		return isContainsLimit;
	}

	private static final Map<String, Boolean> containsLimitCache = new ConcurrentHashMap<>();

	/**
	 * 拼凑字段逗号,分隔子句（用于select）。会处理computed的@Column字段
	 */
    private static String joinColumnForSelect(List<Field> fields, String fieldPrefix, Map<FeatureEnum, Boolean> features) {
		String sep = ",";
		fieldPrefix = fieldPrefix == null ? "" : fieldPrefix.trim();

		StringBuilder sb = new StringBuilder();
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);

			if(InnerCommonUtils.isNotBlank(column.computed())) {
				// 计算列不支持默认前缀，当join时，请自行区分计算字段的命名
				sb.append("(").append(SQLUtils.getComputedColumn(column, features)).append(") AS ")
						.append(getColumnName(column, fieldPrefix)).append(sep);
			} else {
				// 非计算列的话，表的别名要放在`外边
				sb.append(fieldPrefix).append(getColumnName(column))
						.append(" AS \"").append(fieldPrefix).append(column.value()).append("\"") // as里的列名不需要`
						.append(sep);
			}
		}
		int len = sb.length();
		return len == 0 ? "" : sb.substring(0, len - 1);
    }
	
	/**
	 * 拼凑where子句，并把需要的参数写入到values中。返回sql【不】包含where关键字
	 * 
	 * @param fields 注解了Column的field
	 * @param logicOperate 操作符，例如AND
	 * @param values where条件的参数值
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
	 * @param logicOperate 操作符，例如AND
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
	 * 获得指定字段拼凑而成的插入列，例如(name,age)。
	 * @param fields 由调用方保证有Column注解且没有computed值
	 */
	private static void appendInsertColumnSql(StringBuilder sb, List<Field> fields) {
		sb.append("(");
		if (fields != null) {
			for(int i = 0; i < fields.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				Field field = fields.get(i);
				Column column = field.getAnnotation(Column.class);
				appendColumnName(sb, column.value());
			}
		}
		sb.append(")");
	}
    
    /**
     * 拼凑字段逗号,分隔子句（用于insert），并把参数obj的值放到values中。会排除掉computed的@Column字段
     *
     * @param values 不应该为null
     * @param obj 不应该为null
     * @param isWithNullValue 是否把null值放到values中
     */
	private static String joinAndGetValueForInsert(List<Field> fields, String sep,
			List<Object> values, Object obj, boolean isWithNullValue) {
		if(values == null || obj == null) {
			throw new InvalidParameterException("joinAndGetValueForInsert require values and obj");
		}

    	StringBuilder sb = new StringBuilder();
    	for(Field field : fields) {
    		Column column = field.getAnnotation(Column.class);
    		if(InnerCommonUtils.isNotBlank(column.computed())) {
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
    		
        	sb.append(getColumnName(column)).append(sep);
    	}
    	int len = sb.length();
    	return len == 0 ? "" : sb.substring(0, len - 1);
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
	 * @param withNull 当为true时，如果field的值为null，也加入
	 */
	private static String joinSetAndGetValue(List<Field> fields,
			List<Object> values, Object obj, boolean withNull) {
		StringBuilder sb = new StringBuilder();
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			Object value = DOInfoReader.getValue(field, obj);
			if (column.casVersion()) {
				if (value == null) {
					throw new CasVersionNotMatchException("casVersion column value is null");
				}
				long _v;
				if (value instanceof Long) {
					_v = (Long) value;
				} else if (value instanceof Integer) {
					_v = ((Integer) value).longValue();
				} else {
					throw new CasVersionNotMatchException("casVersion column type must be Integer or Long");
				}
				sb.append(getColumnName(column)).append("=").append(_v + 1).append(",");
			} else {
				if (value != null && column.isJSON()) {
					value = NimbleOrmJSON.toJson(value);
				}
				if (withNull || value != null) {
					sb.append(getColumnName(column)).append("=?,");
					values.add(value);
				}
			}
		}
		return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
	}

	private static String getTableName(Class<?> clazz) {
		String tableName = DBHelperContext.getTableName(clazz);
		if (InnerCommonUtils.isBlank(tableName)) {
			tableName = DOInfoReader.getTable(clazz).value();
		}
		return "`" + tableName + "`";
	}

	private static void appendTableName(StringBuilder sb, Class<?> clazz) {
		String tableName = DBHelperContext.getTableName(clazz);
		if (InnerCommonUtils.isBlank(tableName)) {
			tableName = DOInfoReader.getTable(clazz).value();
		}
		sb.append("`").append(tableName).append("`");
	}

	private static String getColumnName(Column column, String prefix) {
		return getColumnName(column.value(), prefix);
	}

	private static String getColumnName(Column column) {
		return getColumnName(column.value());
	}

	/**返回字段名称，重要: 请自行确保field上有注解了@Column*/
	public static String getColumnName(Field field) {
		return getColumnName(field.getAnnotation(Column.class));
	}

	public static String getColumnName(Field field, String prefix) {
		return getColumnName(field.getAnnotation(Column.class), prefix);
	}

	public static String getColumnName(String columnName) {
		return "`" + columnName + "`";
	}

	private static String getColumnName(String columnName, String prefix) {
		return "`" + prefix + columnName + "`";
	}

	private static void appendColumnName(StringBuilder sb, String columnName) {
		sb.append("`").append(columnName).append("`");
	}

}
