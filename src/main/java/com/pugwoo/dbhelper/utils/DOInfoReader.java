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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public static Table getTable(Class<?> clazz) throws NoTableAnnotationException {
		Table table = getAnnotationClass(clazz, Table.class);
		if (table != null) {
			return table;
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
		return getAnnotationClass(clazz, JoinTable.class);
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
		return getJoinTableField(clazz, JoinLeftTable.class);
	}
	
	/**
	 * 获得注解了@JoinRightTable的字段，如果没有注解，抛出NoJoinTableMemberException
	 */
	public static Field getJoinRightTable(Class<?> clazz) {
		return getJoinTableField(clazz, JoinRightTable.class);
	}

	/**
	 * 用于获取@JoinLeftTable和@JoinRightTable注解的属性Field
	 */
	private static Field getJoinTableField(Class<?> clazz,
										   Class<? extends Annotation> annotationClass) {
		if (clazz == null) {
			throw new NoJoinTableMemberException("clazz is null");
		}

		List<Field> result = _getAnnotationColumns(clazz, annotationClass);
		if (result.isEmpty()) {
			throw new NoJoinTableMemberException("class " + clazz.getName()
					+ " does not have @" + annotationClass.getSimpleName() + " field");
		}
		if (result.size() > 1) {
			LOGGER.error("class {} has more than one @{} fields, use first one",
					clazz.getName(), annotationClass.getSimpleName());
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
		Method method = ClassInfoCache.getFieldSetMethod(field);
		
		if(method != null) {
			try {
				method.invoke(object, value);
			} catch (Exception e) {
				LOGGER.error("set method:{} invoke fail", method.getName(), e);
				return false;
			}
		} else {
			field.setAccessible(true);
			try {
				field.set(object, value);
			} catch (Exception e) {
				LOGGER.error("field:{} set fail", field.getName(), e);
				return false;
			}
		}
		
		return true;
	}
	
	private static List<Field> _getFieldsForSelect(Class<?> clazz, boolean selectOnlyKey) {
		List<Class<?>> classLink = getClassAndParentClasses(clazz, true);
		
		List<Field> fields = _getFields(classLink, Column.class);
		if(selectOnlyKey) {
			return InnerCommonUtils.filter(fields,
					o -> o.getAnnotation(Column.class).isKey());
		} else {
			return fields;
		}
	}

	/**
	 * 获得clazz类的有annotationClazz注解的字段field（包括clazz类及其父类，父类优先，不处理重名）。
	 */
	private static List<Field> _getAnnotationColumns(Class<?> clazz,
													 Class<? extends Annotation> annotationClazz) {

		List<Class<?>> classLink = getClassAndParentClasses(clazz, false);

		return _getFields(classLink, annotationClazz);
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
	 * 从指定类clazz再往其父类查找注解了annotationClass的类，如果已经找到了，就返回对应该注解，不再继续往上找
	 * @return 找不到返回null
	 */
	private static <T extends Annotation> T getAnnotationClass(Class<?> clazz,
															   Class<T> annotationClass) {
		Class<?> curClass = clazz;
		while (curClass != null) {
			T annotation = curClass.getAnnotation(annotationClass);
			if(annotation != null) {
				return annotation;
			}
			curClass = curClass.getSuperclass();
		}

		return null;
	}

	/**
	 * 获取classLink多个类中，注解了annotationClazz的Field字段。<br>
	 * 目前annotationClazz的取值有@Column @JoinLeftTable @JoinRightTable <br>
	 * 对于Column注解，如果子类和父类字段同名，那么子类将代替父类，父类的Field不会加进来；<br>
	 * 如果子类出现相同的Field，那么也只拿第一个；一旦出现字段同名，那么进行error log，正常是不建议覆盖和同名操作的，风险很大 <br>
	 */
	private static List<Field> _getFields(List<Class<?>> classLink,
			Class<? extends Annotation> annotationClazz) {
		List<Field> result = new ArrayList<>();
		if(classLink == null || classLink.isEmpty() || annotationClazz == null) {
			return result;
		}

		// 按父类优先的顺序来
		List<List<Field>> fieldList = new ArrayList<>();
		for (int i = classLink.size() - 1; i >= 0; i--) {
			Field[] fields = classLink.get(i).getDeclaredFields();
			fieldList.add(InnerCommonUtils.filter(fields, o -> o.getAnnotation(annotationClazz) != null));
		}

		// @Column注解的字段，需要按value进行去重
		if (annotationClazz == Column.class) {
			Set<String> columnValues = new HashSet<>();
			// 从子类开始拿
			List<List<Field>> fieldListTmp = new ArrayList<>();
			for (int i = fieldList.size() - 1; i >= 0; i--) {
				List<Field> fields = fieldList.get(i);
				List<Field> fieldsTmp = new ArrayList<>();
				for (Field field : fields) {
					Column column = field.getAnnotation(Column.class);
					if (columnValues.contains(column.value())) {
						LOGGER.error("found duplicate field:{} in class(and its parents):{}, this field is ignored",
								field.getName(), classLink.get(0).getName());
						continue;
					}
					columnValues.add(column.value());

					fieldsTmp.add(field);
				}
				fieldListTmp.add(0, fieldsTmp);
			}

			for (List<Field> fields : fieldListTmp) {
				result.addAll(fields);
			}
		} else {
			for (List<Field> fields : fieldList) {
				result.addAll(fields);
			}
		}

		return result;
	}

}
