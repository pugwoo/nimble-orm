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
	private DBHelper dbHelper;

	private Class<T> clazz;
	private boolean isUseGivenObj = false;
	private T t;
	
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
	
	public AnnotationSupportRowMapper(DBHelper dbHelper, Class<T> clazz, T t) {
		this.dbHelper = dbHelper;
		handleClazz(clazz);
		this.t = t;
		this.isUseGivenObj = true;
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
		try {
			// 支持基本的类型
			TypeAutoCast.BasicTypeResult basicTypeResult = TypeAutoCast.transBasicType(clazz, rs);
			if (basicTypeResult.isBasicType()) {
				return (T) basicTypeResult.getValue();
			}

			T obj = isUseGivenObj ? t : clazz.newInstance();
			
			if(isJoinVO) {
				Object t1 = leftJoinField.getType().newInstance();
				Object t2 = rightJoinField.getType().newInstance();
				
				JoinLeftTable joinLeftTable = leftJoinField.getAnnotation(JoinLeftTable.class);
				JoinRightTable joinRightTable = rightJoinField.getAnnotation(JoinRightTable.class);
				
				// 如果关联对象的所有字段都是null值，那么该对象设置为null值

				List<Field> fieldsT1 = DOInfoReader.getColumnsForSelect(leftJoinField.getType(), selectOnlyKey);
				boolean isT1AllNull = handleFieldAndIsAllFieldNull(fieldsT1, joinLeftTable.alias(), t1, rs);

				List<Field> fieldsT2 = DOInfoReader.getColumnsForSelect(rightJoinField.getType(), selectOnlyKey);
				boolean isT2AllNull = handleFieldAndIsAllFieldNull(fieldsT2, joinRightTable.alias(), t2, rs);
				
				DOInfoReader.setValue(leftJoinField, obj, isT1AllNull ? null : t1);
				DOInfoReader.setValue(rightJoinField, obj, isT2AllNull ? null : t2);
				
			} else {
				List<Field> fields = DOInfoReader.getColumnsForSelect(clazz, selectOnlyKey);
				for (Field field : fields) {
					Column column = field.getAnnotation(Column.class);
					Object value = TypeAutoCast.cast(
							getFromRS(rs, column.value(), field),
							field.getType());
					DOInfoReader.setValue(field, obj, value);
				}
			}
			
			return obj;
		} catch (Exception e) {
			LOGGER.error("mapRow exception, class:{}", clazz, e);
			throw new RowMapperFailException(e);
		}
	}

	/**当列不存在时，默认warn log出来，支持配置为抛出异常*/
	private Object getFromRS(ResultSet rs,
							 String columnName,
							 Field field) throws SQLException {
		if (dbHelper instanceof P0_JdbcTemplateOp) {
			Boolean throwErrorIfColumnNotExist =
					((P0_JdbcTemplateOp) dbHelper).getFeature(FeatureEnum.THROW_EXCEPTION_IF_COLUMN_NOT_EXIST);
			if (throwErrorIfColumnNotExist == null || !throwErrorIfColumnNotExist) {
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

	private boolean handleFieldAndIsAllFieldNull(List<Field> fields, String tableAlias, Object t, ResultSet rs) throws SQLException{
		boolean isAllNull = true;
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			String columnName;
			if(!column.computed().trim().isEmpty()) {
				columnName = column.value(); // 计算列用用户自行制定别名
			} else {
				columnName = tableAlias + "." + column.value();
			}
			Object value = TypeAutoCast.cast(
					getFromRS(rs, columnName, field), field.getType());
			if(value != null) {
				isAllNull = false;
			}
			DOInfoReader.setValue(field, t, value);
		}
		return isAllNull;
	}
}