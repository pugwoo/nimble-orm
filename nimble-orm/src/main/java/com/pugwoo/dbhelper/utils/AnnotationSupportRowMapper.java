package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.exception.RowMapperFailException;
import com.pugwoo.dbhelper.impl.part.P0_JdbcTemplateOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

	private boolean selectOnlyKey = false; // 是否只选择主键列，默认false
	
	public AnnotationSupportRowMapper(DBHelper dbHelper, Class<T> clazz) {
		this.dbHelper = dbHelper;
		handleClazz(clazz);
	}
	
	public AnnotationSupportRowMapper(DBHelper dbHelper, Class<T> clazz, boolean selectOnlyKey) {
		this.dbHelper = dbHelper;
		this.selectOnlyKey = selectOnlyKey;
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
	public T mapRow(ResultSet rs, int index) {
		// 保存当前正在处理的field，存null表示没有正在处理的field，这个记录是为了log打印出来，方便问题排查
		List<Field> currentField = new ArrayList<>(1);
		currentField.add(null);

		try {
			// 支持基本的类型
			TypeAutoCast.BasicTypeResult basicTypeResult = TypeAutoCast.transBasicType(clazz, rs);
			if (basicTypeResult.isBasicType()) {
				return (T) basicTypeResult.getValue();
			}

			T obj = clazz.newInstance();
			
			if(isJoinVO) {
				currentField.set(0, leftJoinField);
				Object t1 = leftJoinField.getType().newInstance();
				JoinLeftTable joinLeftTable = leftJoinField.getAnnotation(JoinLeftTable.class);

				List<Field> fieldsT1 = DOInfoReader.getColumnsForSelect(leftJoinField.getType(), selectOnlyKey);
				boolean isT1AllNull = handleFieldAndIsAllFieldNull(fieldsT1, joinLeftTable.alias(), t1, rs, currentField);
				currentField.set(0, leftJoinField); // 因为handleFieldAndIsAllFieldNull中会修改currentField，所以重新设置
				// 如果关联对象的所有字段都是null值，那么该对象设置为null值
				DOInfoReader.setValue(leftJoinField, obj, isT1AllNull ? null : t1);
				currentField.set(0, null);

				currentField.set(0, rightJoinField);
				Object t2 = rightJoinField.getType().newInstance();
				JoinRightTable joinRightTable = rightJoinField.getAnnotation(JoinRightTable.class);

				List<Field> fieldsT2 = DOInfoReader.getColumnsForSelect(rightJoinField.getType(), selectOnlyKey);
				boolean isT2AllNull = handleFieldAndIsAllFieldNull(fieldsT2, joinRightTable.alias(), t2, rs, currentField);
				currentField.set(0, rightJoinField);
				DOInfoReader.setValue(rightJoinField, obj, isT2AllNull ? null : t2);
				currentField.set(0, null);
				
			} else {
				List<Field> fields = DOInfoReader.getColumnsForSelect(clazz, selectOnlyKey);
				for (Field field : fields) {
					currentField.set(0, field);

					Column column = field.getAnnotation(Column.class);
					Object value = getFromRS(rs, column.value(), field);
					if (value == null && InnerCommonUtils.isNotBlank(column.readIfNullScript())) {
						value = ScriptUtils.getValueFromScript(column.ignoreScriptError(), column.readIfNullScript());
					}

					DOInfoReader.setValue(field, obj, value);
					currentField.set(0, null);
				}
			}
			
			return obj;
		} catch (Exception e) {
			boolean isHandleField = !currentField.isEmpty() && currentField.get(0) != null;
			if (isHandleField) {
				LOGGER.error("mapRow exception, class:{}, field:{}", clazz, currentField.get(0), e);
				throw new RowMapperFailException(e, currentField.get(0));
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
					return TypeAutoCast.getFromRS(rs, columnName, field);
				} catch (SQLException e) {
					String message = e.getMessage();
					if (!(message.startsWith("Column ") && message.endsWith(" not found."))) {
						throw e;
					}
					return null;
				}
			} else {
				return TypeAutoCast.getFromRS(rs, columnName, field);
			}
		} else {
			return TypeAutoCast.getFromRS(rs, columnName, field);
		}
	}

	private boolean handleFieldAndIsAllFieldNull(List<Field> fields, String tableAlias, Object t, ResultSet rs,
												 List<Field> currentField) throws Exception {
		boolean isAllNull = true;
		for (Field field : fields) {
			currentField.set(0, field);

			Column column = field.getAnnotation(Column.class);
			String columnName = tableAlias + "." + column.value();;

			Object value = getFromRS(rs, columnName, field);
			if(value != null) { // 这个值是否为null直接来自于数据库，不受是否设置了column.readIfNullScript()的影响
				isAllNull = false;
			}
			if (value == null && InnerCommonUtils.isNotBlank(column.readIfNullScript())) {
				value = ScriptUtils.getValueFromScript(column.ignoreScriptError(), column.readIfNullScript());
			}
			DOInfoReader.setValue(field, t, value);

			currentField.set(0, null);
		}
		return isAllNull;
	}
}