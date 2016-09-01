package com.pugwoo.dbhelper.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.NoColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoTableAnnotationException;

/**
 * 2015年1月12日 16:42:26 读取DO的注解信息:
 * 
 * 1. 继承的类的信息读取 XXX 待验证，去重的情况怎样处理
 */
public class DOInfoReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DOInfoReader.class);
	
	/**
	 * 获取DO的@Table信息
	 * 
	 * @param clazz
	 * @throws NoTableAnnotationException 当clazz没有@Table注解时抛出NoTableAnnotationException
	 * @return
	 */
	public static Table getTable(Class<?> clazz)
			throws NoTableAnnotationException {
		Table table = clazz.getAnnotation(Table.class);
		if (table == null) {
			throw new NoTableAnnotationException("class " + clazz.getName()
					+ " does not have @Table annotation.");
		}
		return table;
	}

	/**
	 * 获得所有有@Column注解的列，包括继承的父类中的，顺序父类先
	 * 
	 * @param clazz
	 * @throws NoColumnAnnotationException 当没有一个@Column注解时抛出
	 * @return 不会返回null
	 */
	public static List<Field> getColumns(Class<?> clazz)
			throws NoColumnAnnotationException {
		List<Class<?>> classLink = new ArrayList<Class<?>>();
		Class<?> curClass = clazz;
		while (curClass != null) {
			classLink.add(curClass);
			curClass = curClass.getSuperclass();
		}
		// 父类优先
		List<Field> result = new ArrayList<Field>();
		for (int i = classLink.size() - 1; i >= 0; i--) {
			Field[] fields = classLink.get(i).getDeclaredFields();
			for (Field field : fields) {
				if (field.getAnnotation(Column.class) != null) {
					result.add(field);
				}
			}
		}
		if (result.isEmpty()) {
			throw new NoColumnAnnotationException("class " + clazz.getName()
					+ " does not have any @Column annotation");
		}
		return result;
	}
	
	/**
	 * 获得字段里面的key字段
	 * @param fields
	 * @return
	 * @throws NoKeyColumnAnnotationException
	 */
	public static List<Field> getKeyColumns(List<Field> fields) 
	    throws NoKeyColumnAnnotationException {
		List<Field> keyFields = new ArrayList<Field>();
		for(Field field : fields) {
			Column column = DOInfoReader.getColumnInfo(field);
			if(column.isKey()) {
				keyFields.add(field);
			}
		}
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		return keyFields;
	}
	
	/**
	 * 获得字段里面的非key字段
	 * @param fields
	 * @return
	 * @throws NoKeyColumnAnnotationException
	 */
	public static List<Field> getNotKeyColumns(List<Field> fields) {
		List<Field> keyFields = new ArrayList<Field>();
		for(Field field : fields) {
			Column column = DOInfoReader.getColumnInfo(field);
			if(!column.isKey()) {
				keyFields.add(field);
			}
		}
		return keyFields;
	}

	public static Column getColumnInfo(Field field) {
		return field.getAnnotation(Column.class);
	}
	
	/**
	 * 优先通过getter获得值，如果没有getter，则直接获取
	 * 
	 * @param field
	 * @param object
	 * @return
	 */
	public static Object getValue(Field field, Object object) {
		String fieldName = field.getName();
		String setMethodName = "get" + firstLetterUpperCase(fieldName);
		Method method = null;
		try {
			method = object.getClass().getMethod(setMethodName);
		} catch (Exception e) {
		}
		
		if(method != null) {
			try {
				return method.invoke(object);
			} catch (Exception e) {
				LOGGER.error("method invoke", e);
			}
		}
		
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (Exception e) {
			LOGGER.error("method invoke", e);
			return null;
		}
	}
	
	/**
	 * 先按照setter的约定寻找setter方法(必须严格匹配参数类型或自动转换)<br>
	 * 如果有则按setter方法，如果没有则直接写入
	 * 
	 * @param field
	 * @param object
	 * @param value
	 */
	public static boolean setValue(Field field, Object object, Object value) {
		String fieldName = field.getName();
		String setMethodName = "set" + firstLetterUpperCase(fieldName);
		value = TypeAutoCast.cast(value, field.getType());
		
		Method method = null;
		try {
			method = object.getClass().getMethod(setMethodName, value.getClass());
		} catch (Exception e) {
		}
		
		if(method != null) {
			try {
				method.invoke(object, value);
			} catch (Exception e) {
				LOGGER.error("method invoke", e);
				return false;
			}
		} else {
			field.setAccessible(true);
			try {
				field.set(object, value);
			} catch (Exception e) {
				LOGGER.error("method invoke", e);
				return false;
			}
		}
		
		return true;
	}
	
	private static String firstLetterUpperCase(String str) {
		if (str == null || str.length() < 2) {
			return str;
		}
		String firstLetter = str.substring(0, 1).toUpperCase();
		return firstLetter + str.substring(1, str.length());
	}
}
