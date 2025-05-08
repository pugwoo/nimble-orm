package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.exception.RowMapperFailException;
import com.pugwoo.dbhelper.impl.part.P0_JdbcTemplateOp;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.model.RowData;
import com.pugwoo.dbhelper.sql.SQLAssemblyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 2015年1月13日 17:48:30<br>
 * 抽取出来的根据注解来生成bean的rowMapper
 * 
 * @param <T>
 */
public class AnnotationSupportRowMapper<T> implements RowMapper<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationSupportRowMapper.class);

	/**传入对应的dbHelper对象*/
	private final DBHelper dbHelper;

	private Class<T> clazz;

	private boolean isJoinVO = false;
	private Field leftJoinField;
	private Field rightJoinField;

	private final String sql;
	private final List<Object> args;
	private volatile String assembledSql;

	public AnnotationSupportRowMapper(DBHelper dbHelper, Class<T> clazz, String sql, List<Object> args) {
		this.dbHelper = dbHelper;
		this.sql = sql;
		this.args = args;
		handleClazz(clazz);
	}

	private void handleClazz(Class<T> clazz) {
		this.clazz = clazz;
		JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
		if(joinTable != null) {
			isJoinVO = true;
			leftJoinField = DOInfoReader.getJoinLeftTable(clazz);
			rightJoinField = DOInfoReader.getJoinRightTable(clazz);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T mapRow(ResultSet rs, int index) {
		// 保存当前正在处理的field，存null表示没有正在处理的field，这个记录是为了log打印出来，方便问题排查
		AtomicReference<Field> currentField = new AtomicReference<>(null);

		try {
			// 内置的数据类型RowData
			if (clazz == RowData.class) {
				RowData rowData = new RowData();
				ResultSetMetaData md = rs.getMetaData();
				int columns = md.getColumnCount();
				for (int i = 1; i <= columns; i++) {
					rowData.put(md.getColumnLabel(i), rs.getObject(i));
				}
				return (T) rowData;
			}

			// 支持基本的类型
			TypeAutoCast.BasicTypeResult basicTypeResult = TypeAutoCast.transBasicType(clazz, rs);
			if (basicTypeResult.isBasicType()) {
				return (T) basicTypeResult.getValue();
			}

			T obj = clazz.newInstance();

			if(isJoinVO) {
				currentField.set(leftJoinField);
				Object t1 = leftJoinField.getType().newInstance();
				JoinLeftTable joinLeftTable = leftJoinField.getAnnotation(JoinLeftTable.class);

				List<Field> fieldsT1 = DOInfoReader.getColumns(leftJoinField.getType());
				boolean isT1AllNull = handleFieldAndIsAllFieldNull(fieldsT1, joinLeftTable.alias(), t1, rs, currentField);
				currentField.set(leftJoinField); // 因为handleFieldAndIsAllFieldNull中会修改currentField，所以重新设置
				// 如果关联对象的所有字段都是null值，那么该对象设置为null值
				DOInfoReader.setValue(leftJoinField, obj, isT1AllNull ? null : t1);
				currentField.set(null);

				currentField.set(rightJoinField);
				Object t2 = rightJoinField.getType().newInstance();
				JoinRightTable joinRightTable = rightJoinField.getAnnotation(JoinRightTable.class);

				List<Field> fieldsT2 = DOInfoReader.getColumns(rightJoinField.getType());
				boolean isT2AllNull = handleFieldAndIsAllFieldNull(fieldsT2, joinRightTable.alias(), t2, rs, currentField);
				currentField.set(rightJoinField);
				DOInfoReader.setValue(rightJoinField, obj, isT2AllNull ? null : t2);
				currentField.set(null);

			} else {
				List<Field> fields = DOInfoReader.getColumns(clazz);
				for (Field field : fields) {
					currentField.set(field);

					Column column = field.getAnnotation(Column.class);
					Object value = getFromRS(rs, column.value(), field);
					if (value == null && InnerCommonUtils.isNotBlank(column.readIfNullScript())) {
						value = ScriptUtils.getValueFromScript(column.ignoreScriptError(), column.readIfNullScript());
					}

					DOInfoReader.setValue(field, obj, value);
					currentField.set(null);
				}

				List<Field> sqlColumnForSelect = DOInfoReader.getSqlColumns(clazz);
				for (Field field : sqlColumnForSelect) {
					String assembledSql = getAssembleSql();
					DOInfoReader.setValue(field, obj, assembledSql);
				}
			}

			return obj;
		} catch (Exception e) {
			boolean isHandleField = currentField.get() != null;
			if (isHandleField) {
				LOGGER.error("mapRow exception, class:{}, field:{}", clazz, currentField.get(), e);
				throw new RowMapperFailException(e, currentField.get());
			} else {
				LOGGER.error("mapRow exception, class:{}", clazz, e);
				throw new RowMapperFailException(e);
			}
		}
	}

	/**当列不存在时，默认warn log出来，支持配置为抛出异常*/
	private Object getFromRS(ResultSet rs,
							 String columnName,
							 Field field) throws Exception {
		if (dbHelper instanceof P0_JdbcTemplateOp) {
			boolean throwErrorIfColumnNotExist =
					((P0_JdbcTemplateOp) dbHelper).getFeature(FeatureEnum.THROW_EXCEPTION_IF_COLUMN_NOT_EXIST);
			if (!throwErrorIfColumnNotExist) {
				try {
					return TypeAutoCast.getFromRS(rs, columnName, field, dbHelper.getDatabaseType());
				} catch (SQLException e) {
					String message = e.getMessage();
					if (!(message.contains("not found") /*mysql/pg*/ || message.contains("does not exist") /*clickhouse*/
					      || message.contains("找不到") /*pg*/)) {
						throw e;
					}
					LOGGER.warn("column:[{}] not found in ResultSet, class:{}, field:{}", columnName, clazz, field);
					return null;
				}
			} else {
				return TypeAutoCast.getFromRS(rs, columnName, field, dbHelper.getDatabaseType());
			}
		} else {
			return TypeAutoCast.getFromRS(rs, columnName, field, dbHelper.getDatabaseType());
		}
	}

	private boolean handleFieldAndIsAllFieldNull(List<Field> fields, String tableAlias, Object t, ResultSet rs,
												 AtomicReference<Field> currentField) throws Exception {
		boolean isAllNull = true;
		for (Field field : fields) {
			currentField.set(field);

			Column column = field.getAnnotation(Column.class);
			String columnName = tableAlias + "." + column.value();

			Object value = getFromRS(rs, columnName, field);
			if(value != null) { // 这个值是否为null直接来自于数据库，不受是否设置了column.readIfNullScript()的影响
				isAllNull = false;
			}
			if (value == null && InnerCommonUtils.isNotBlank(column.readIfNullScript())) {
				value = ScriptUtils.getValueFromScript(column.ignoreScriptError(), column.readIfNullScript());
			}
			DOInfoReader.setValue(field, t, value);

			currentField.set(null);
		}
		return isAllNull;
	}

	private String getAssembleSql() {
		if (assembledSql == null) {
			synchronized (this) {
				if (assembledSql == null) {
					try {
						if (args == null) {
							assembledSql = sql;
						} else {
							assembledSql = SQLAssemblyUtils.assembleSql(sql, args.toArray());
						}
					} catch (Exception e) {
						LOGGER.error("fail to assemble sql, sql:{}, params:{}", sql, NimbleOrmJSON.toJsonNoException(args), e);
						assembledSql = "";
					}
				}
			}
        }
        return assembledSql;
    }
}