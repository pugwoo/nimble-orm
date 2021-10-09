package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.ExcludeInheritedColumn;
import com.pugwoo.dbhelper.annotation.JoinLeftTable;
import com.pugwoo.dbhelper.annotation.JoinRightTable;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.cache.ClassInfoCache;
import com.pugwoo.dbhelper.exception.CasVersionNotMatchException;
import com.pugwoo.dbhelper.exception.NoColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoJoinTableMemberException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoTableAnnotationException;
import com.pugwoo.dbhelper.exception.NotOnlyOneKeyColumnException;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

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
	 * @throws NoTableAnnotationException 当clazz没有@Table注解时抛出NoTableAnnotationException
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
		
		throw new NoTableAnnotationException("class: "
				+ (clazz == null ? "null" : clazz.getName())
				+ " does not have @Table annotation.");
	}
	
	/**
	 * 获得clazz上注解的JoinTable，如果没有则返回null
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
	 * @param clazz DO类
	 * @param dbFieldName 数据库字段名称，多个用逗号隔开
	 * @return 如果不存在返回空数组，返回的Field的顺序和dbFieldName保持一致；只要有一个dbFieldName找不到，则返回空数组
	 */
	public static List<Field> getFieldByDBField(Class<?> clazz, String dbFieldName) {
		List<String> dbFieldNameList = InnerCommonUtils.split(dbFieldName, ",");
		List<Field> fields = getColumns(clazz);
		List<Field> result = new ArrayList<>();

		for (String dbField : dbFieldNameList) {
			boolean isFound = false;
			for(Field field : fields) {
				Column column = field.getAnnotation(Column.class);
				if(column.value().equals(dbField)) {
					result.add(field);
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				LOGGER.error("cannot found db field:{} in class:{}", dbField, clazz.getName());
				return new ArrayList<>();
			}
		}

		return result;
	}
	
	/**
	 * 获得泛型的class
	 */
	public static Class<?> getGenericFieldType(Field field) {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
		return (Class<?>) stringListType.getActualTypeArguments()[0];
	}
	
	/**
	 * 获得所有有@Column注解的列，包括继承的父类中的，顺序父类先
	 *
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
	 * @param selectOnlyKey 是否只select主键
	 * @throws NoColumnAnnotationException 当没有一个@Column注解时抛出
	 * @return 不会返回null
	 */
	public static List<Field> getColumnsForSelect(Class<?> clazz, boolean selectOnlyKey) {
		if(clazz == null) {
			throw new NoColumnAnnotationException("class is null");
		}
		
		List<Field> result = _getFieldsForSelect(clazz, selectOnlyKey);
		if (result.isEmpty()) {
			throw new NoColumnAnnotationException("class " + clazz.getName()
					+ " does not have any @Column fields");
		}
		
		return result;
	}
	
	/**
	 * 获得注解了@JoinLeftTable的字段，如果没有注解，抛出NoJoinTableMemberException
	 */
	public static Field getJoinLeftTable(Class<?> clazz) {
		if(clazz == null) {
			throw new NoJoinTableMemberException("clazz is null");
		}
		List<Field> result = _getAnnotationColumns(clazz, JoinLeftTable.class);
		if(result.isEmpty()) {
			throw new NoJoinTableMemberException("class " + clazz.getName()
			    + " does not have @JoinLeftTable field");
		}
		return result.get(0);
	}
	
	/**
	 * 获得注解了@JoinRightTable的字段，如果没有注解，抛出NoJoinTableMemberException
	 */
	public static Field getJoinRightTable(Class<?> clazz) {
		if(clazz == null) {
			throw new NoJoinTableMemberException("clazz is null");
		}
		List<Field> result = _getAnnotationColumns(clazz, JoinRightTable.class);
		if(result.isEmpty()) {
			throw new NoJoinTableMemberException("class " + clazz.getName()
			    + " does not have @JoinRightTable field");
		}
		return result.get(0);
	}
	
	/**
	 * 获得字段里面的key字段
	 * @throws NoKeyColumnAnnotationException 如果没有key Column，抛出该异常。
	 */
	public static List<Field> getKeyColumns(Class<?> clazz) 
	    throws NoKeyColumnAnnotationException {
		List<Field> fields = getColumns(clazz);
		List<Field> keyFields = new ArrayList<>();
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

	/**
	 * 获得一个DO类注解的casVersion字段
	 * @return 当没有column字段时返回null
	 * @throws CasVersionNotMatchException 当有2个及2个以上的casVersion字段时抛出该异常
	 */
	public static Field getCasVersionColumn(Class<?> clazz) throws CasVersionNotMatchException {
		List<Field> fields = getColumns(clazz);
		Field casVersionField = null;
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.casVersion()) {
				if(casVersionField != null) {
					throw new CasVersionNotMatchException("there are more than one casVersion column in a DO class");
				}
				casVersionField = field;
			}
		}
		return casVersionField;
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
	 * @return 如果没有则返回null
	 */
	public static Field getSoftDeleteColumn(Class<?> clazz) {

		// 处理turnoff软删除
		if (DBHelperContext.isTurnOffSoftDelete(clazz)) {
			return null;
		}

		List<Field> fields = getColumns(clazz);
		
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column.softDelete().length == 2 && !column.softDelete()[0].trim().isEmpty()
					&& !column.softDelete()[1].trim().isEmpty()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * 获得字段里面的非key字段
	 */
	public static List<Field> getNotKeyColumns(Class<?> clazz) {
		List<Field> fields = getColumns(clazz);
		
		List<Field> keyFields = new ArrayList<>();
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
	 * @return 不会返回null
	 */
	public static List<Field> getRelatedColumns(Class<?> clazz) {
		List<Class<?>> classLink = getClassAndParentClasses(clazz, true);

		// 父类优先
		List<Field> result = new ArrayList<>();
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
	 */
	public static Object getValue(Field field, Object object) {
		String fieldName = field.getName();
		String setMethodName = "get" + InnerCommonUtils.firstLetterUpperCase(fieldName);
		Method method = null;
		try {
			method = object.getClass().getMethod(setMethodName);
		} catch (Exception e) {
			// ignore
		}
		
		if(method != null) {
			try {
				method.setAccessible(true);
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
	 * 为relatedColumn获得字段的值。这里有特别的逻辑。
	 * 当fields只有一个时，返回的是对象本身；否则是一个List，里面是按顺序的fields的多个值
	 * @return 当fields为空，返回null
	 */
	public static Object getValueForRelatedColumn(List<Field> fields, Object object) {
		if (fields == null || fields.isEmpty()) {
			return null;
		}

		if (fields.size() == 1) {
			return getValue(fields.get(0), object);
		}
		List<Object> result = new ArrayList<>();
		for (Field field : fields) {
			result.add(getValue(field, object));
		}
		return result;
	}
	
	/**
	 * 先按照setter的约定寻找setter方法(必须严格匹配参数类型或自动转换)<br>
	 * 如果有则按setter方法，如果没有则直接写入
	 */
	public static boolean setValue(Field field, Object object, Object value) {
		value = TypeAutoCast.cast(value, field.getType());
		Method method = ClassInfoCache.getFieldMethod(field);
		
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
	
	private static List<Field> _getFieldsForSelect(Class<?> clazz, boolean selectOnlyKey) {
		List<Class<?>> classLink = getClassAndParentClasses(clazz, true);
		
		List<Field> fields = _getFields(classLink, Column.class);
		if(selectOnlyKey) {
			List<Field> keyFields = new ArrayList<>();
			for(Field field : fields) {
				if(field.getAnnotation(Column.class).isKey()) {
					keyFields.add(field);
				}
			}
			return keyFields;
		} else {
			return fields;
		}
	}

	/**
	 * 获得指定类及其父类的列表，子类在前，父类在后
	 * @param clazz 要查询的类
	 * @param enableExcludeInheritedColumn 是否受到@ExcludeInheritedColumn注解的约束
	 */
	private static List<Class<?>> getClassAndParentClasses(Class<?> clazz, boolean enableExcludeInheritedColumn) {
		if (clazz == null) {
			return new ArrayList<>();
		}

		List<Class<?>> classLink = new ArrayList<>();
		Class<?> curClass = clazz;
		while (curClass != null) {
			classLink.add(curClass);

			if (enableExcludeInheritedColumn) {
				ExcludeInheritedColumn eic = curClass.getAnnotation(ExcludeInheritedColumn.class);
				if(eic != null) {
					break;
				}
			}

			curClass = curClass.getSuperclass();
		}

		return classLink;
	}
	
	/**
	 * 获得clazz类的有annotationClazz注解的字段field（包括clazz类及其父类，父类优先，不处理重名）。
	 */
	private static List<Field> _getAnnotationColumns(Class<?> clazz, 
			Class<? extends Annotation> annotationClazz) {

		List<Class<?>> classLink = getClassAndParentClasses(clazz, false);
		
		return _getFields(classLink, annotationClazz);
	}
	
	private static List<Field> _getFields(List<Class<?>> classLink,
			Class<? extends Annotation> annotationClazz) {
		List<Field> result = new ArrayList<>();
		if(classLink == null || classLink.isEmpty()) {
			return result;
		}
		// 父类先拿，不处理重名情况
		for (int i = classLink.size() - 1; i >= 0; i--) {
			Field[] fields = classLink.get(i).getDeclaredFields();
			for (Field field : fields) {
				if (annotationClazz != null && field.getAnnotation(annotationClazz) != null) {
					result.add(field);
				}
			}
		}
		return result;
	}

}
