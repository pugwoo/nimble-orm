package com.pugwoo.dbhelper.utils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.pugwoo.dbhelper.annotation.Column;

/**
 * 2015年1月13日 17:48:30<br>
 * 抽取出来的根据注解来生成bean的rowMapper
 * 
 * @param <T>
 */
public class AnnotationSupportRowMapper<T> implements RowMapper<T> {

	private Class<T> clazz;
	private boolean isUseGivenObj = false;
	private T t;

	public AnnotationSupportRowMapper(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public AnnotationSupportRowMapper(Class<T> clazz, T t) {
		this.clazz = clazz;
		this.t = t;
		this.isUseGivenObj = true;
	}

	public T mapRow(ResultSet rs, int index) throws SQLException {
		try {
			T obj = isUseGivenObj ? t : clazz.newInstance();
			List<Field> fields = DOInfoReader.getColumns(clazz);
			for (Field field : fields) {
				Column column = DOInfoReader.getColumnInfo(field);
				Object value = dbToJavaTypeAutoCast(field.getType(),
						rs.getObject(column.value()));
				DOInfoReader.setValue(field, obj, value);
			}
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 自动转换数据库到java的类型 TODO 待持续完善
	 * 
	 * @param targetClass
	 * @param value
	 * @return
	 */
	private static Object dbToJavaTypeAutoCast(Class<?> targetClass,
			Object value) {
		if (targetClass.isInstance(value)) {
			return value;
		}
		if (targetClass == Long.class || targetClass == long.class) {
			if (Integer.class.isInstance(value) || int.class.isInstance(value)) {
				return ((Integer) value).longValue();
			}
		}

		return value;
	}
}
