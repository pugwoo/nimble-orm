package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.json.NimbleOrmDateUtils;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * 2015年8月22日 16:58:48
 * 自动转换类型
 * @author pugwoo
 */
public class TypeAutoCast {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeAutoCast.class);

	/**
	 * 从ResultSet中读出数据并转成成对应的类型，如果指定类型rs无法转换，则不转换。
	 * 
	 * 2018年4月24日 11:48:32 新增支持标记为isJSON的列的处理。
	 * @param rs
	 * @param columnName
	 * @return
	 */
	public static Object getFromRS(ResultSet rs, String columnName, Field field)
			throws SQLException {
		int columnIndex = rs.findColumn(columnName);
		Object result = rs.getObject(columnIndex);
		if(result == null) { // 保证null会返回null值
			return null;
		}
		
		Column column = field.getAnnotation(Column.class);
		if(column != null && column.isJSON()) { // 优先处理标记为json的列
			String valStr = (result instanceof String) ? (String) result : rs.getString(columnIndex);
			if(valStr == null || valStr.trim().isEmpty()) {
				return null;
			}
			String typeName = field.getGenericType().toString();
			try {
				if(!typeName.contains("<")) {
					return NimbleOrmJSON.parse(valStr, field.getType());
				} else { // 处理泛型
					return NimbleOrmJSON.parseGeneric(valStr, typeName);
				}
			} catch(Exception e) {
				LOGGER.error("parse column to JSON fail, json:{}, type:{}", valStr, typeName, e);
				return valStr; // 作为string返回，交由上一级处理
			}
		}
		
		Class<?> clazz = field.getType();

		if(clazz == String.class) {
			return result instanceof String ? result : rs.getString(columnIndex);
		}
		if(clazz == Integer.class || clazz == int.class) {
			return result instanceof Integer ? result : rs.getInt(columnIndex);
		}
		if(clazz == Long.class || clazz == long.class) {
			return result instanceof Long ? result : rs.getLong(columnIndex);
		}
		if(clazz == Boolean.class || clazz == boolean.class) {
			return result instanceof Boolean ? result : rs.getBoolean(columnIndex);
		}
		if(clazz == Byte.class || clazz == byte.class) {
			return result instanceof Byte ? result : rs.getByte(columnIndex);
		}
		if(clazz == byte[].class) {
			return result instanceof byte[] ? result : rs.getBytes(columnIndex);
		}
		if(clazz == Short.class || clazz == short.class) {
			return result instanceof Short ? result : rs.getShort(columnIndex);
		}
		if(clazz == Float.class || clazz == float.class) {
			return result instanceof Float ? result : rs.getFloat(columnIndex);
		}
		if(clazz == Double.class || clazz == double.class) {
			return result instanceof Double ? result : rs.getDouble(columnIndex);
		}
		if(clazz == BigDecimal.class) {
			return result instanceof BigDecimal ? result : rs.getBigDecimal(columnIndex);
		}
		if (clazz == java.sql.Date.class) {
			return result instanceof java.sql.Date ? result : rs.getDate(columnIndex);
		}
		if (clazz == java.sql.Time.class) {
			return result instanceof java.sql.Time ? result : rs.getDate(columnIndex);
		}
		if (clazz == java.sql.Timestamp.class) {
			return result instanceof java.sql.Timestamp ? result : rs.getTimestamp(columnIndex);
		}
		
		return result;
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
		if (clazz == java.sql.Time.class && obj instanceof java.util.Date) {
			return (T) new java.sql.Time(((java.util.Date)obj).getTime());
		}
		
		// TODO 可能还有更多的类型需要转换
		
		return (T) obj;
	}

	/**
	 * 转换成sql值的字符串形式，带上单引号。
	 * 例如传入hello，返回'hello'
	 * @param object 不应该为null，如果为null则返回空字符串''
	 * @return
	 */
	public static String toSqlValueStr(Object object) {
		if(object == null) {
			return "''";
		}

		if(object instanceof Date) {
			return "'" + NimbleOrmDateUtils.formatWithMs((Date) object) + "'";
		}

		return "'" + object.toString().replace("'", "\\'") + "'";
	}

}
