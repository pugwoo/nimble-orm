package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.json.NimbleOrmDateUtils;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 2015年8月22日 16:58:48
 * 自动转换类型
 * @author pugwoo
 */
public class TypeAutoCast {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeAutoCast.class);

	public static class BasicTypeResult {
		// 转换之后的值
		private Object value;
		// 是否基本类型
		private boolean isBasicType;

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public boolean isBasicType() {
			return isBasicType;
		}

		public void setBasicType(boolean basicType) {
			isBasicType = basicType;
		}
	}

	/**
	 * 从ResultSet中读出数据并转成成对应的类型，如果指定类型rs无法转换，则不转换。
	 * 
	 * 2018年4月24日 11:48:32 新增支持标记为isJSON的列的处理。
	 */
	public static Object getFromRS(ResultSet rs, String columnName, Field field) throws Exception {
		int columnIndex = rs.findColumn(columnName);
		Object result = rs.getObject(columnIndex);
		if(result == null) { // 保证null会返回null值
			return null;
		}
		
		Column column = field.getAnnotation(Column.class);
		if(column != null && column.isJSON()) { // 优先处理标记为json的列
			String valStr = (result instanceof String) ? (String) result : rs.getString(columnIndex);
			if(InnerCommonUtils.isBlank(valStr)) {
				return null;
			}

			Type genericType = field.getGenericType();
			try {
				if (genericType instanceof Class) {
					return NimbleOrmJSON.parse(valStr, field.getType());
				} else { // 处理泛型
				    return NimbleOrmJSON.parseGeneric(valStr, (ParameterizedType) genericType);
				}
			} catch(Exception e) {
				LOGGER.error("parse column to JSON fail, json:{}, type:{}", valStr, genericType, e);
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
		if (clazz == java.util.Date.class) {
			if (result instanceof java.util.Date) {
				return result;
			}
			return getDate(rs, columnIndex);
		}
		if (clazz == LocalDateTime.class) {
			if (result instanceof LocalDateTime) {
				return result;
			}
			Date date = getDate(rs, columnIndex);
			if (date == null) {
				return null;
			}
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		}
		if (clazz == LocalDate.class) {
			if (result instanceof LocalDate) {
				return result;
			}
			Date date = getDate(rs, columnIndex);
			if (date == null) {
				return null;
			}
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}
		if (clazz == LocalTime.class) {
			if (result instanceof LocalTime) {
				return result;
			}
			Date date = getDate(rs, columnIndex);
			if (date == null) {
				return null;
			}
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
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

	private static Date getDate(ResultSet rs, int columnIndex) throws Exception {
		try {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			if (timestamp == null) {
				return null;
			}
			// 对于java.util.Date类型，一般是java.sql.Timestamp，所以特意做了转换
			return new Date(timestamp.getTime());
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(columnIndex);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseThrowException(str.trim());
		}
	}

	/**
	 * 转换基本类型
	 */
	public static BasicTypeResult transBasicType(Class<?> clazz, ResultSet rs) throws SQLException {
		BasicTypeResult result = new BasicTypeResult();

		if(clazz == String.class) {
			result.setBasicType(true);
			result.setValue(rs.getString(1));
		}
		if(clazz == Integer.class || clazz == int.class) {
			result.setBasicType(true);
			result.setValue(rs.getInt(1));
		}
		if(clazz == Long.class || clazz == long.class) {
			result.setBasicType(true);
			result.setValue(rs.getLong(1));
		}
		if(clazz == Boolean.class || clazz == boolean.class) {
			result.setBasicType(true);
			result.setValue(rs.getBoolean(1));
		}
		if(clazz == Byte.class || clazz == byte.class) {
			result.setBasicType(true);
			result.setValue(rs.getByte(1));
		}
		if(clazz == byte[].class) {
			result.setBasicType(true);
			result.setValue(rs.getBytes(1));
		}
		if(clazz == Short.class || clazz == short.class) {
			result.setBasicType(true);
			result.setValue(rs.getShort(1));
		}
		if(clazz == Float.class || clazz == float.class) {
			result.setBasicType(true);
			result.setValue(rs.getFloat(1));
		}
		if(clazz == Double.class || clazz == double.class) {
			result.setBasicType(true);
			result.setValue(rs.getDouble(1));
		}
		if(clazz == BigDecimal.class) {
			result.setBasicType(true);
			result.setValue(rs.getBigDecimal(1));
		}
		if (clazz == java.util.Date.class) {
			result.setBasicType(true);
			Timestamp timestamp = rs.getTimestamp(1);
			if (timestamp == null) {
				result.setValue(null);
			} else {
				// 对于java.util.Date类型，一般是java.sql.Timestamp，所以特意做了转换
				result.setValue(new Date(timestamp.getTime()));
			}
		}
		if (clazz == LocalDateTime.class) {
			result.setBasicType(true);
			Timestamp timestamp = rs.getTimestamp(1);
			if (timestamp == null) {
				result.setValue(null);
			} else {
				result.setValue(timestamp.toLocalDateTime());
			}
		}
		if (clazz == LocalDate.class) {
			result.setBasicType(true);
			Timestamp timestamp = rs.getTimestamp(1);
			if (timestamp == null) {
				result.setValue(null);
			} else {
				result.setValue(timestamp.toLocalDateTime().toLocalDate());
			}
		}
		if (clazz == LocalTime.class) {
			result.setBasicType(true);
			Timestamp timestamp = rs.getTimestamp(1);
			if (timestamp == null) {
				result.setValue(null);
			} else {
				result.setValue(timestamp.toLocalDateTime().toLocalTime());
			}
		}
		if (clazz == java.sql.Date.class) {
			result.setBasicType(true);
			result.setValue(rs.getDate(1));
		}
		if (clazz == java.sql.Time.class) {
			result.setBasicType(true);
			result.setValue(rs.getDate(1));
		}
		if (clazz == java.sql.Timestamp.class) {
			result.setBasicType(true);
			result.setValue(rs.getTimestamp(1));
		}

		if (clazz == Map.class) {
			result.setBasicType(true);
			ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();
			Map<String, Object> map = new HashMap<>(columns);
			for (int i = 1; i <= columns; i++) {
				map.put(md.getColumnName(i), rs.getObject(i));
			}
			result.setValue(map);
		}

		return result;
	}
	
	/**
	 * 自动转换类型
	 * @param obj 要转换的对象
	 * @param clazz 要转换成的类型
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj, Class<T> clazz) {
		// 已经是同类型的就不用转
		if(clazz.isInstance(obj)) {
			return (T) obj;
		}
		
		// 对于obj是null但是目标clazz是【基础类型】时，转成 0
		if(clazz == Integer.class || clazz == int.class) {
			if(obj == null) {
				return clazz == int.class ? (T) Integer.valueOf(0) : null;
			}
			if(obj instanceof Integer) {
				return (T) obj;
			}
			return (T) Integer.valueOf(obj.toString());
		}
		if(clazz == Long.class || clazz == long.class) {
			if(obj == null) {
				return clazz == long.class ? (T) Long.valueOf(0L) : null;
			}
			if(obj instanceof Long) {
				return (T) obj;
			}
			return (T) Long.valueOf(obj.toString());
		}
		if(clazz == Byte.class || clazz == byte.class) {
			if(obj == null) {
				return clazz == byte.class ? (T) Byte.valueOf((byte) 0) : null;
			}
			if(obj instanceof Byte) {
				return (T) obj;
			}
			return (T) Byte.valueOf(obj.toString());
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
				return clazz == short.class ? (T) Short.valueOf((short) 0) : null;
			}
			if(obj instanceof Short) {
				return (T) obj;
			}
			return (T) Short.valueOf(obj.toString());
		}
		if(clazz == Boolean.class || clazz == boolean.class) {
			if(obj == null) {
				return clazz == boolean.class ? (T) Boolean.FALSE : null;
			}
			if(obj instanceof Boolean) {
				return (T) obj;
			}
			if(obj instanceof Number) {
				return (T) Boolean.valueOf(((Number) obj).intValue() != 0);
			}
			return (T) Boolean.valueOf(obj.toString());
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
	 * 例如传入hello, 返回 'hello'
	 * @param object 不应该为null，如果为null则返回空字符串''
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
