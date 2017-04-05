package com.pugwoo.dbhelper.utils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.model.Pair;

/**
 * 2015年1月13日 17:48:30<br>
 * 抽取出来的根据注解来生成bean的rowMapper
 * 
 * @param <T>
 */
public class AnnotationSupportRowMapper4Pair<T1, T2> implements RowMapper<Pair<T1, T2>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationSupportRowMapper4Pair.class);

	private Class<T1> clazzT1;
	private Class<T2> clazzT2;

	public AnnotationSupportRowMapper4Pair(Class<T1> clazzT1, Class<T2> clazzT2) {
		this.clazzT1 = clazzT1;
		this.clazzT2 = clazzT2;
	}
	
	@Override
	public Pair<T1, T2> mapRow(ResultSet rs, int index) throws SQLException {
		try {
			T1 t1 = clazzT1.newInstance();
			T2 t2 = clazzT2.newInstance();
			
			List<Field> fieldsT1 = DOInfoReader.getColumns(clazzT1);
			for (Field field : fieldsT1) {
				Column column = field.getAnnotation(Column.class);
				Object value = TypeAutoCast.cast(
						TypeAutoCast.cast(rs, "t1." + column.value(), field.getType()), 
						field.getType());
				DOInfoReader.setValue(field, t1, value);
			}
			
			List<Field> fieldsT2 = DOInfoReader.getColumns(clazzT2);
			for (Field field : fieldsT2) {
				Column column = field.getAnnotation(Column.class);
				Object value = TypeAutoCast.cast(
						TypeAutoCast.cast(rs, "t2." + column.value(), field.getType()), 
						field.getType());
				DOInfoReader.setValue(field, t2, value);
			}
			
			return new Pair<T1, T2>(t1, t2);
		} catch (Exception e) {
			LOGGER.error("mapRow exception", e);
			return null;
		}
	}

}
