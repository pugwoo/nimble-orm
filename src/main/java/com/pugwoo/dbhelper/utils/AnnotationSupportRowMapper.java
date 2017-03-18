package com.pugwoo.dbhelper.utils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.pugwoo.dbhelper.annotation.Column;

/**
 * 2015年1月13日 17:48:30<br>
 * 抽取出来的根据注解来生成bean的rowMapper
 * 
 * @param <T>
 */
public class AnnotationSupportRowMapper<T> implements RowMapper<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationSupportRowMapper.class);

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
				Column column = field.getAnnotation(Column.class);
				Object value = TypeAutoCast.cast(
						TypeAutoCast.cast(rs, column.value(), field.getType()), 
						field.getType());
				DOInfoReader.setValue(field, obj, value);
			}
			return obj;
		} catch (Exception e) {
			LOGGER.error("mapRow exception", e);
			return null;
		}
	}

}
