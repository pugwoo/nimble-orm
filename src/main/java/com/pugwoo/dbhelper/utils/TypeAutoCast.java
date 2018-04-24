package com.pugwoo.dbhelper.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.exception.RowMapperFailException;
import com.pugwoo.dbhelper.json.MyObjectMapper;

/**
 * 2015年8月22日 16:58:48
 * 自动转换类型
 * @author pugwoo
 */
public class TypeAutoCast {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeAutoCast.class);
	
	private static final ObjectMapper objectMapper = new MyObjectMapper();
	
	/**
	 * 从ResultSet中读出数据并转成成对应的类型，如果指定类型rs无法转换，则不转换。
	 * 
	 * 2018年4月24日 11:48:32 新增支持标记为isJSON的列的处理。
	 * @param rs
	 * @param columnName
	 * @param clazz
	 * @param isJSON 标记
	 * @return
	 */
	public static Object getFromRS(ResultSet rs, String columnName, Field field)
			throws SQLException{
		if(rs.getObject(columnName) == null) { // 保证null会返回null值
			return null;
		}
		
		Column column = field.getAnnotation(Column.class);
		if(column != null && column.isJSON()) { // 优先处理标记为json的列
			String valStr = rs.getString(columnName);
			String typeName = field.getGenericType().toString();
			try {
				if(!typeName.contains("<")) {
					return objectMapper.readValue(valStr, field.getType());
				} else { // 处理泛型
					JavaType type = parseGenericType(objectMapper.getTypeFactory(), typeName);
					return objectMapper.readValue(valStr, type);
				}
			} catch(Exception e) {
				LOGGER.error("parse column to JSON fail, json:{}, type:{}", valStr, typeName, e);
				throw new RowMapperFailException(e);
			}
		}
		
		Class<?> clazz = field.getType();
		
		if(clazz == Integer.class || clazz == int.class) {
			return rs.getInt(columnName);
		}
		if(clazz == Long.class || clazz == long.class) {
			return rs.getLong(columnName);
		}
		if(clazz == Byte.class || clazz == byte.class) {
			return rs.getByte(columnName);
		}
		if(clazz == byte[].class) {
			return rs.getBytes(columnName);
		}
		if(clazz == Short.class || clazz == short.class) {
			return rs.getShort(columnName);
		}
		if(clazz == Boolean.class || clazz == boolean.class) {
			return rs.getBoolean(columnName);
		}
		if(clazz == Float.class || clazz == float.class) {
			return rs.getFloat(columnName);
		}
		if(clazz == Double.class || clazz == double.class) {
			return rs.getDouble(columnName);
		}
		if(clazz == String.class) {
			return rs.getString(columnName);
		}
		if(clazz == BigDecimal.class) {
			return rs.getBigDecimal(columnName);
		}
		if (clazz == java.sql.Date.class) {
			return rs.getDate(columnName);
		}
		if (clazz == java.sql.Time.class) {
			return rs.getDate(columnName);
		}
		if (clazz == java.sql.Timestamp.class) {
			return rs.getTimestamp(columnName);
		}
		
		return rs.getObject(columnName);
	}
	
	/**
	 * 自动转换类型
	 * @param obj 要转换的对象
	 * @param clazz 要转换成的类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj, Class<T> clazz) {
		// 已经是同类型的就不用转
		if(obj != null && clazz.isInstance(obj)) {
			return (T) obj;
		}
		
		// 对于obj是null但是目标clazz是【基础类型】时，转成 0
		if(clazz == Integer.class || clazz == int.class) {
			if(obj == null) {
				return clazz == int.class ? (T) new Integer(0) : null;
			}
			if(obj instanceof Integer) {
				return (T) obj;
			}
			return (T) new Integer(obj.toString());
		}
		if(clazz == Long.class || clazz == long.class) {
			if(obj == null) {
				return clazz == long.class ? (T) new Long(0L) : null;
			}
			if(obj instanceof Long) {
				return (T) obj;
			}
			return (T) new Long(obj.toString());
		}
		if(clazz == Byte.class || clazz == byte.class) {
			if(obj == null) {
				return clazz == byte.class ? (T) new Byte((byte)0) : null;
			}
			if(obj instanceof Byte) {
				return (T) obj;
			}
			return (T) new Byte(obj.toString());
		}
		if(clazz == Character.class || clazz == char.class) {
			if(obj == null) {
				return clazz == char.class ? (T) new Character((char)0) : null;
			}
			if(obj instanceof Character) {
				return (T) obj;
			}
			return (T) new Character((char)0); // char没有办法从其它类型转
		}
		if(clazz == Short.class || clazz == short.class) {
			if(obj == null) {
				return clazz == short.class ? (T) new Short((short)0) : null;
			}
			if(obj instanceof Short) {
				return (T) obj;
			}
			return (T) new Short(obj.toString());
		}
		if(clazz == Boolean.class || clazz == boolean.class) {
			if(obj == null) {
				return clazz == boolean.class ? (T) new Boolean(false) : null;
			}
			if(obj instanceof Boolean) {
				return (T) obj;
			}
			if(obj instanceof Number) {
				return (T) new Boolean(((Number) obj).intValue() != 0);
			}
			return (T) new Boolean(obj.toString());
		}
		if(clazz == Float.class || clazz == float.class) {
			if(obj == null) {
				return clazz == float.class ? (T) new Float(0f) : null;
			}
			if(obj instanceof Float) {
				return (T) obj;
			}
			return (T) new Float(obj.toString());
		}
		if(clazz == Double.class || clazz == double.class) {
			if(obj == null) {
				return clazz == double.class ? (T) new Double(0d) : null;
			}
			if(obj instanceof Double) {
				return (T) obj;
			}
			return (T) new Double(obj.toString());
		}
		
		if(obj == null) {
			return null;
		}
		
		if(clazz == String.class) {
			return (T) obj.toString();
		}
		if(clazz == BigDecimal.class) {
			return (T) new BigDecimal(obj.toString());
		}
		if (clazz == java.sql.Date.class && obj instanceof java.util.Date) {
			return (T) new java.sql.Date(((java.util.Date)obj).getTime());
		}
		
		// TODO 可能还有更多的类型需要转换
		
		return (T) obj;
	}

	/**
	 * 解析泛型的类,只支持1个或2个的泛型类型，不支持3个及以上的
	 * @param className
	 * @return 如果没有泛型，则返回null
	 * @throws ClassNotFoundException 
	 */
	private static JavaType parseGenericType(TypeFactory typeFactory, String className)
			throws ClassNotFoundException {
		if(className == null) return null;
		int left = className.indexOf("<");
		if(left < 0) {
			return typeFactory.constructType(Class.forName(className.trim()));
		}
		int right = className.lastIndexOf(">");
		
		String baseClassName = className.substring(0, left);
		String genericAll = className.substring(left + 1, right);
		
		assertLessThan3Dot(genericAll);
		int dotIndex = getDotIndex(genericAll);
		if(dotIndex < 0) {
			return typeFactory.constructParametricType(Class.forName(baseClassName.trim()),
					parseGenericType(typeFactory, genericAll));
		} else {
			String leftClassName = genericAll.substring(0, dotIndex);
			String rightClassName = genericAll.substring(dotIndex + 1);
			return typeFactory.constructParametricType(Class.forName(baseClassName.trim()),
					parseGenericType(typeFactory, leftClassName),
					parseGenericType(typeFactory, rightClassName));
		}
	}
	
	private static int getDotIndex(String str) {
		if(str == null) return -1;
		int bracket = 0;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == ',' && bracket == 0) {
				return i;
			}
			if(c == '<') {
				bracket++;
			} else if(c == '>') {
				bracket--;
			}
		}
		return -1;
	}
	
	private static void assertLessThan3Dot(String str) {
		if(str == null) return;
		int counts = 0;
		int bracket = 0;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == ',' && bracket == 0) {
				counts++;
			}
			if(c == '<') {
				bracket++;
			} else if(c == '>') {
				bracket--;
			}
		}
		if(counts > 1) {
			throw new RuntimeException("spring-mvc-conf not support more than two generic type, found " + (counts+1)
				+ " for class:" +str);
		}
	}
}
