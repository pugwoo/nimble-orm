package com.pugwoo.dbhelper.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.ExcludeInheritedColumn;
import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.exception.NoColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoJoinTableMemberException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoTableAnnotationException;
import com.pugwoo.dbhelper.exception.NotOnlyOneKeyColumnException;

/**
 * 2015年1月12日 16:42:26 读取DO的注解信息:
 * 
 * 1. 继承的类的信息读取，父类先读取，请保证@Column注解没有重复的字段。
 */
public class DOInfoReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DOInfoReader.class);
	
	/**
	 * 获取DO的@Table信息，如果子类没有，会往父类查找
	 * 
	 * @param clazz
	 * @throws NoTableAnnotationException 当clazz没有@Table注解时抛出NoTableAnnotationException
	 * @return
	 */
	public static Table getTable(Class<?> clazz)
			throws NoTableAnnotationException {
		Class<?> curClass = clazz;
		while (curClass != null) {
			Table table = curClass.getAnnotation(Table.class);
			if(table != null) {
				return table;
			}
			curClass = curClass.getSuperclass();
		}
		
		throw new NoTableAnnotationException("class " + clazz.getName()
					+ " does not have @Table annotation.");
	}
	
	/**
	 * 获得clazz上注解的JoinTable，如果没有则返回null
	 * @param clazz
	 * @return 如果没有则返回null
	 */
	public static JoinTable getJoinTable(Class<?> clazz) {
		Class<?> curClass = clazz;
		while (curClass != null) {
			JoinTable joinTable = curClass.getAnnotation(JoinTable.class);
			if(joinTable != null) {
				return joinTable;
			}
			curClass = curClass.getSuperclass();
		}
		
		return null;
	}
	
	/**
	 * 从db字段名拿字段对象
	 * @param clazz
	 * @param dbFieldName
	 * @return 如果不存在返回null
	 */
	public static Field getFieldByDBField(Class<?> clazz, String dbFieldName) {
		List<Field> fields = getColumns(clazz);
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.value().equals(dbFieldName)) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * 获得泛型的class
	 * @param field
	 * @return
	 */
	public static Class<?> getGenericFieldType(Field field) {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        Class<?> clazz = (Class<?>) stringListType.getActualTypeArguments()[0];
        return clazz;
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
		if(clazz == null) {
			throw new NoColumnAnnotationException("class is null");
		}
			
		List<Field> result = _getAnnotationColumns(clazz, Column.class);
		if (result.isEmpty()) {
			throw new NoColumnAnnotationException("class " + clazz.getName()
					+ " does not have any @Column fields");
		}
		
		return result;
	}
	
	/**
	 * 获得所有有@Column注解的列，包括继承的父类中的，顺序父类先。
	 * 该方法只用于select读操作。
	 * @param clazz
	 * @throws NoColumnAnnotationException 当没有一个@Column注解时抛出
	 * @return 不会返回null
	 */
	public static List<Field> getColumnsForSelect(Class<?> clazz) {
		if(clazz == null) {
			throw new NoColumnAnnotationException("class is null");
		}
		
		List<Field> result = _getFieldsForSelect(clazz);
		if (result.isEmpty()) {
			throw new NoColumnAnnotationException("class " + clazz.getName()
					+ " does not have any @Column fields");
		}
		
		return result;
	}
	
	/**
	 * 获得注解了@JoinLeftTable的字段，如果没有注解，抛出NoJoinTableMemberException
	 * @param clazz
	 * @return
	 */
	public static Field getJoinLeftTable(Class<?> clazz) {
		if(clazz == null) {
			throw new NoJoinTableMemberException("clazz is null");
		}
		List<Field> result = _getAnnotationColumns(clazz, JoinLeftTable.class);
		if(result == null || result.isEmpty()) {
			throw new NoJoinTableMemberException("class " + clazz.getName()
			    + " does not have @JoinLeftTable field");
		}
		return result.get(0);
	}
	
	/**
	 * 获得注解了@JoinRightTable的字段，如果没有注解，抛出NoJoinTableMemberException
	 * @param clazz
	 * @return
	 */
	public static Field getJoinRightTable(Class<?> clazz) {
		if(clazz == null) {
			throw new NoJoinTableMemberException("clazz is null");
		}
		List<Field> result = _getAnnotationColumns(clazz, JoinRightTable.class);
		if(result == null || result.isEmpty()) {
			throw new NoJoinTableMemberException("class " + clazz.getName()
			    + " does not have @JoinRightTable field");
		}
		return result.get(0);
	}
	
	/**
	 * 获得字段里面的key字段
	 * @param clazz
	 * @return
	 * @throws NoKeyColumnAnnotationException 如果没有key Column，抛出该异常。
	 */
	public static List<Field> getKeyColumns(Class<?> clazz) 
	    throws NoKeyColumnAnnotationException {
		List<Field> fields = getColumns(clazz);
		List<Field> keyFields = new ArrayList<Field>();
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.isKey()) {
				keyFields.add(field);
			}
		}
		if(keyFields.isEmpty()) {
			throw new NoKeyColumnAnnotationException();
		}
		return keyFields;
	}
	
	public static Field getOneKeyColumn(Class<?> clazz) throws NotOnlyOneKeyColumnException {
		List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);

		if (keyFields.size() != 1) {
			throw new NotOnlyOneKeyColumnException(
					"must have only one key column, actually has "
							+ keyFields.size() + " key columns");
		}
		
		return keyFields.get(0);
	}
	
	public static Field getAutoIncrementField(Class<?> clazz) {
		
		List<Field> fields = getColumns(clazz);
		
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.isAutoIncrement()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * 获得软删除标记字段，最多只能返回1个。
	 * @param clazz
	 * @return 如果没有则返回null
	 */
	public static Field getSoftDeleteColumn(Class<?> clazz) {
		List<Field> fields = getColumns(clazz);
		
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.softDelete() != null && column.softDelete().length == 2
					&& !column.softDelete()[0].trim().isEmpty()
					&& !column.softDelete()[1].trim().isEmpty()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * 获得字段里面的非key字段
	 * @param clazz
	 * @return
	 */
	public static List<Field> getNotKeyColumns(Class<?> clazz) {
		List<Field> fields = getColumns(clazz);
		
		List<Field> keyFields = new ArrayList<Field>();
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(!column.isKey()) {
				keyFields.add(field);
			}
		}
		return keyFields;
	}
	
	/**
	 * 获得所有有@RelatedColumn注解的列，包括继承的父类中的，顺序父类先
	 * 
	 * @param clazz
	 * @return 不会返回null
	 */
	public static List<Field> getRelatedColumns(Class<?> clazz) {
		if(clazz == null) {
			return new ArrayList<Field>();
		}
		
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
				if (field.getAnnotation(RelatedColumn.class) != null) {
					result.add(field);
				}
			}
		}

		return result;
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
	
	private static List<Field> _getFieldsForSelect(Class<?> clazz) {
		if(clazz == null) {
			return new ArrayList<Field>();
		}
		
		List<Class<?>> classLink = new ArrayList<Class<?>>();
		Class<?> curClass = clazz;
		while (curClass != null) {
			classLink.add(curClass);
			ExcludeInheritedColumn eic = curClass.getAnnotation(ExcludeInheritedColumn.class);
			if(eic != null) {
				break;
			}
			curClass = curClass.getSuperclass();
		}
		
		return _getFields(classLink, Column.class);
	}
	
	/**
	 * 获得clazz类的有annotationClazz注解的字段field（包括clazz类及其父类，父类优先，不处理重名）。
	 * @param clazz
	 * @param annoClazz
	 * @return
	 */
	private static List<Field> _getAnnotationColumns(Class<?> clazz, 
			Class<? extends Annotation> annoClazz) {
		if(clazz == null) {
			return new ArrayList<Field>();
		}
		
		List<Class<?>> classLink = new ArrayList<Class<?>>();
		Class<?> curClass = clazz;
		while (curClass != null) {
			classLink.add(curClass);
			curClass = curClass.getSuperclass();
		}
		
		return _getFields(classLink, annoClazz);
	}
	
	private static List<Field> _getFields(List<Class<?>> classLink,
			Class<? extends Annotation> annoClazz) {
		List<Field> result = new ArrayList<Field>();
		if(classLink == null || classLink.isEmpty()) {
			return result;
		}
		// 父类先拿，不处理重名情况
		for (int i = classLink.size() - 1; i >= 0; i--) {
			Field[] fields = classLink.get(i).getDeclaredFields();
			for (Field field : fields) {
				if (annoClazz != null && field.getAnnotation(annoClazz) != null) {
					result.add(field);
				}
			}
		}
		return result;
	}
	
	private static String firstLetterUpperCase(String str) {
		if (str == null || str.length() < 2) {
			return str;
		}
		String firstLetter = str.substring(0, 1).toUpperCase();
		return firstLetter + str.substring(1, str.length());
	}
}
