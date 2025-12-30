package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
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
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 2015年8月22日 16:58:48
 * 自动转换类型
 * 
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
	 * 2018年4月24日 11:48:32 新增支持标记为isJSON的列的处理。
	 */
	public static Object getFromRS(ResultSet rs, String columnName, Field field, DatabaseTypeEnum databaseType,
			Column column) throws Exception {
		if (column != null && column.isJSON()) { // 优先处理标记为json的列
			String valStr = rs.getString(columnName);
			if (InnerCommonUtils.isBlank(valStr)) {
				return null;
			}

			Type genericType = field.getGenericType();
			try {
				if (genericType instanceof Class) {
					return NimbleOrmJSON.parse(valStr, field.getType());
				} else { // 处理泛型
					return NimbleOrmJSON.parseGeneric(valStr, (ParameterizedType) genericType);
				}
			} catch (Exception e) {
				LOGGER.error("parse column to JSON fail, json:{}, type:{}", valStr, genericType, e);
				return valStr; // 作为string返回，交由上一级处理
			}
		}

		Class<?> clazz = field.getType();

		if (clazz == String.class) {
			return rs.getString(columnName);
		}
		if (clazz == Integer.class) {
			int value = rs.getInt(columnName);
			return rs.wasNull() ? null : value;
		}
		if (clazz == int.class) {
			return rs.getInt(columnName);
		}
		if (clazz == Long.class) {
			long value = rs.getLong(columnName);
			return rs.wasNull() ? null : value;
		}
		if (clazz == long.class) {
			return rs.getLong(columnName);
		}
		if (clazz == Boolean.class) {
			boolean value = rs.getBoolean(columnName);
			return rs.wasNull() ? null : value;
		}
		if (clazz == boolean.class) {
			return rs.getBoolean(columnName);
		}
		if (clazz == Byte.class) {
			byte value = rs.getByte(columnName);
			return rs.wasNull() ? null : value;
		}
		if (clazz == byte.class) {
			return rs.getByte(columnName);
		}
		if (clazz == byte[].class) {
			if (databaseType == DatabaseTypeEnum.CLICKHOUSE) {
				return InnerCommonUtils.decodeBase64(rs.getString(columnName));
			}
			return rs.getBytes(columnName);
		}
		if (clazz == Short.class) {
			short value = rs.getShort(columnName);
			return rs.wasNull() ? null : value;
		}
		if (clazz == short.class) {
			return rs.getShort(columnName);
		}
		if (clazz == Float.class) {
			float value = rs.getFloat(columnName);
			return rs.wasNull() ? null : value;
		}
		if (clazz == float.class) {
			return rs.getFloat(columnName);
		}
		if (clazz == Double.class) {
			double value = rs.getDouble(columnName);
			return rs.wasNull() ? null : value;
		}
		if (clazz == double.class) {
			return rs.getDouble(columnName);
		}
		if (clazz == BigDecimal.class) {
			return rs.getBigDecimal(columnName);
		}
		if (clazz == java.util.Date.class) {
			// 说明：这里要严格匹配，虽然java.sql.Date是java.util.Date的子类，但行为不同
			return getDate(rs, columnName);
		}
		if (clazz == LocalDateTime.class) {
			return getLocalDateTime(rs, columnName);
		}
		if (clazz == LocalDate.class) {
			return getLocalDate(rs, columnName);
		}
		if (clazz == LocalTime.class) {
			return getLocalTime(rs, columnName);
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

	private static Date getDate(ResultSet rs, String columnName) throws Exception {
		try {
			Timestamp timestamp = rs.getTimestamp(columnName);
			if (timestamp == null) {
				return null;
			}
			// 对于java.util.Date类型，一般是java.sql.Timestamp，所以特意做了转换
			return new Date(timestamp.getTime());
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(columnName);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseThrowException(str.trim());
		}
	}

	private static Date getDate(ResultSet rs) throws Exception {
		try {
			Timestamp timestamp = rs.getTimestamp(1);
			if (timestamp == null) {
				return null;
			}
			// 对于java.util.Date类型，一般是java.sql.Timestamp，所以特意做了转换
			return new Date(timestamp.getTime());
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(1);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseThrowException(str.trim());
		}
	}

	private static LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws Exception {
		try {
			Timestamp timestamp = rs.getTimestamp(columnName);
			return timestamp == null ? null : timestamp.toLocalDateTime();
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(columnName);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseLocalDateTime(str.trim());
		}
	}

	private static LocalDateTime getLocalDateTime(ResultSet rs) throws Exception {
		try {
			Timestamp timestamp = rs.getTimestamp(1);
			return timestamp == null ? null : timestamp.toLocalDateTime();
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(1);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseLocalDateTime(str.trim());
		}
	}

	private static LocalDate getLocalDate(ResultSet rs, String columnName) throws Exception {
		try {
			// 特别说明：在mysql的ResultSetImpl实现中，rs.getDate实现复杂，效率很低，所以之类不要用rs.getDate
			Timestamp timestamp = rs.getTimestamp(columnName);
			return timestamp == null ? null : timestamp.toLocalDateTime().toLocalDate();
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(columnName);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseLocalDate(str.trim());
		}
	}

	private static LocalDate getLocalDate(ResultSet rs) throws Exception {
		try {
			// 特别说明：在mysql的ResultSetImpl实现中，rs.getDate实现复杂，效率很低，所以之类不要用rs.getDate
			Timestamp timestamp = rs.getTimestamp(1);
			return timestamp == null ? null : timestamp.toLocalDateTime().toLocalDate();
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(1);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseLocalDate(str.trim());
		}
	}

	private static LocalTime getLocalTime(ResultSet rs, String columnName) throws Exception {
		try {
			java.sql.Time time = rs.getTime(columnName);
			return time == null ? null : time.toLocalTime();
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(columnName);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseLocalTime(str.trim());
		}
	}

	private static LocalTime getLocalTime(ResultSet rs) throws Exception {
		try {
			java.sql.Time time = rs.getTime(1);
			return time == null ? null : time.toLocalTime();
		} catch (Exception e) {
			// 尝试通过获取字符串自行进行解析，有些jdbc driver不支持getTimestamp
			String str = rs.getString(1);
			if (InnerCommonUtils.isBlank(str)) {
				return null;
			}
			return NimbleOrmDateUtils.parseLocalTime(str.trim());
		}
	}

	/**
	 * 转换基本类型
	 */
	public static BasicTypeResult transBasicType(Class<?> clazz, ResultSet rs) throws Exception {
		BasicTypeResult result = new BasicTypeResult();

		if (clazz == String.class) {
			result.setBasicType(true);
			result.setValue(rs.getString(1));
		} else if (clazz == Integer.class || clazz == int.class) {
			result.setBasicType(true);
			result.setValue(rs.getInt(1));
		} else if (clazz == Long.class || clazz == long.class) {
			result.setBasicType(true);
			result.setValue(rs.getLong(1));
		} else if (clazz == Boolean.class || clazz == boolean.class) {
			result.setBasicType(true);
			result.setValue(rs.getBoolean(1));
		} else if (clazz == Byte.class || clazz == byte.class) {
			result.setBasicType(true);
			result.setValue(rs.getByte(1));
		} else if (clazz == byte[].class) {
			result.setBasicType(true);
			result.setValue(rs.getBytes(1));
		} else if (clazz == Short.class || clazz == short.class) {
			result.setBasicType(true);
			result.setValue(rs.getShort(1));
		} else if (clazz == Float.class || clazz == float.class) {
			result.setBasicType(true);
			result.setValue(rs.getFloat(1));
		} else if (clazz == Double.class || clazz == double.class) {
			result.setBasicType(true);
			result.setValue(rs.getDouble(1));
		} else if (clazz == BigDecimal.class) {
			result.setBasicType(true);
			result.setValue(rs.getBigDecimal(1));
		} else if (clazz == java.util.Date.class) {
			result.setBasicType(true);
			Date date = getDate(rs);
			result.setValue(date);
		} else if (clazz == LocalDateTime.class) {
			result.setBasicType(true);
			result.setValue(getLocalDateTime(rs));
		} else if (clazz == LocalDate.class) {
			result.setBasicType(true);
			result.setValue(getLocalDate(rs));
		} else if (clazz == LocalTime.class) {
			result.setBasicType(true);
			result.setValue(getLocalTime(rs));
		} else if (clazz == java.sql.Date.class) {
			result.setBasicType(true);
			result.setValue(rs.getDate(1));
		} else if (clazz == java.sql.Time.class) {
			result.setBasicType(true);
			result.setValue(rs.getDate(1));
		} else if (clazz == java.sql.Timestamp.class) {
			result.setBasicType(true);
			result.setValue(rs.getTimestamp(1));
		} else if (clazz == Map.class) {
			result.setBasicType(true);
			ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();
			Map<String, Object> map = new HashMap<>(columns);
			for (int i = 1; i <= columns; i++) {
				map.put(md.getColumnLabel(i), rs.getObject(i));
			}
			result.setValue(map);
		}

		return result;
	}

	/**
	 * 自动转换类型
	 * 
	 * @param obj   要转换的对象
	 * @param clazz 要转换成的类型
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj, Class<T> clazz) {
		// 已经是同类型的就不用转
		if (clazz.isInstance(obj)) {
			return (T) obj;
		}

		// 对于obj是null但是目标clazz是【基础类型】时，转成 0
		if (clazz == Integer.class || clazz == int.class) {
			if (obj == null) {
				return clazz == int.class ? (T) Integer.valueOf(0) : null;
			}
			if (obj instanceof Integer) {
				return (T) obj;
			}
			String str = obj.toString();
			if (str.isEmpty()) {
				return clazz == int.class ? (T) Integer.valueOf(0) : null;
			}
			return (T) Integer.valueOf(str);
		}
		if (clazz == Long.class || clazz == long.class) {
			if (obj == null) {
				return clazz == long.class ? (T) Long.valueOf(0L) : null;
			}
			if (obj instanceof Long) {
				return (T) obj;
			}
			String str = obj.toString();
			if (str.isEmpty()) {
				return clazz == long.class ? (T) Long.valueOf(0L) : null;
			}
			return (T) Long.valueOf(str);
		}
		if (clazz == Byte.class || clazz == byte.class) {
			if (obj == null) {
				return clazz == byte.class ? (T) Byte.valueOf((byte) 0) : null;
			}
			if (obj instanceof Byte) {
				return (T) obj;
			}
			String str = obj.toString();
			if (str.isEmpty()) {
				return clazz == byte.class ? (T) Byte.valueOf((byte) 0) : null;
			}
			return (T) Byte.valueOf(str);
		}
		if (clazz == Character.class || clazz == char.class) {
			if (obj == null) {
				return clazz == char.class ? (T) Character.valueOf((char) 0) : null;
			}
			if (obj instanceof Character) {
				return (T) obj;
			}
			return (T) Character.valueOf((char) 0); // char没有办法从其它类型转
		}
		if (clazz == Short.class || clazz == short.class) {
			if (obj == null) {
				return clazz == short.class ? (T) Short.valueOf((short) 0) : null;
			}
			if (obj instanceof Short) {
				return (T) obj;
			}
			String str = obj.toString();
			if (str.isEmpty()) {
				return clazz == short.class ? (T) Short.valueOf((short) 0) : null;
			}
			return (T) Short.valueOf(str);
		}
		if (clazz == Boolean.class || clazz == boolean.class) {
			if (obj == null) {
				return clazz == boolean.class ? (T) Boolean.FALSE : null;
			}
			if (obj instanceof Boolean) {
				return (T) obj;
			}
			if (obj instanceof Number) {
				return (T) Boolean.valueOf(((Number) obj).intValue() != 0);
			}
			String str = obj.toString();
			if (str.isEmpty()) {
				return clazz == boolean.class ? (T) Boolean.FALSE : null;
			}
			return (T) Boolean.valueOf(str);
		}
		if (clazz == Float.class || clazz == float.class) {
			if (obj == null) {
				return clazz == float.class ? (T) Float.valueOf(0f) : null;
			}
			if (obj instanceof Float) {
				return (T) obj;
			}
			String str = obj.toString();
			if (str.isEmpty()) {
				return clazz == float.class ? (T) Float.valueOf(0f) : null;
			}
			return (T) Float.valueOf(str);
		}
		if (clazz == Double.class || clazz == double.class) {
			if (obj == null) {
				return clazz == double.class ? (T) Double.valueOf(0d) : null;
			}
			if (obj instanceof Double) {
				return (T) obj;
			}
			String str = obj.toString();
			if (str.isEmpty()) {
				return clazz == double.class ? (T) Double.valueOf(0d) : null;
			}
			return (T) Double.valueOf(str);
		}

		if (obj == null) {
			return null;
		}

		if (clazz == byte[].class) {
			return (T) ((String) obj).getBytes();
		}
		if (clazz == String.class) {
			return (T) obj.toString();
		}
		if (clazz == BigDecimal.class) {
			String str = obj.toString();
			if (str.isEmpty()) {
				return null;
			}
			return (T) new BigDecimal(str);
		}
		if (clazz == java.util.Date.class) {
			try {
				return (T) NimbleOrmDateUtils.parseThrowException(obj.toString());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if (clazz == java.sql.Date.class) {
			try {
				Date date = obj instanceof Date ? (Date) obj : NimbleOrmDateUtils.parseThrowException(obj.toString());
				return date == null ? null : (T) new java.sql.Date(date.getTime());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if (clazz == java.sql.Time.class) {
			try {
				Date date = obj instanceof Date ? (Date) obj : NimbleOrmDateUtils.parseThrowException(obj.toString());
				return date == null ? null : (T) new java.sql.Time(date.getTime());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if (clazz == java.sql.Timestamp.class) {
			try {
				LocalDateTime date = obj instanceof Date ? NimbleOrmDateUtils.toLocalDateTime((Date) obj)
						: NimbleOrmDateUtils.parseLocalDateTimeThrowException(obj.toString());
				return date == null ? null : (T) Timestamp.valueOf(date);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if (clazz == LocalDate.class) {
			try {
				LocalDate date = obj instanceof Date ? NimbleOrmDateUtils.toLocalDate((Date) obj)
						: NimbleOrmDateUtils.parseLocalDateThrowException(obj.toString());
				return (T) date;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if (clazz == LocalDateTime.class) {
			try {
				LocalDateTime date = obj instanceof Date ? NimbleOrmDateUtils.toLocalDateTime((Date) obj)
						: NimbleOrmDateUtils.parseLocalDateTimeThrowException(obj.toString());
				return (T) date;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if (clazz == LocalTime.class) {
			try {
				LocalTime date = obj instanceof Date ? NimbleOrmDateUtils.toLocalTime((Date) obj)
						: NimbleOrmDateUtils.parseLocalTimeThrowException(obj.toString());
				return (T) date;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}

		return (T) obj;
	}

	/**
	 * 转换成sql值的字符串形式，带上单引号。
	 * 例如传入hello, 返回 'hello'
	 * 
	 * @param object 不应该为null，如果为null则返回空字符串''
	 */
	public static String toSqlValueStr(Object object) {
		if (object == null) {
			return "''";
		}

		if (object instanceof Number || object instanceof Boolean) {
			return object.toString();
		}
		if (object instanceof Date) {
			return "'" + NimbleOrmDateUtils.formatWithMs((Date) object) + "'"; // 这里保留毫秒是为了让数据库自行处理时间的四舍五入
		}

		return "'" + object.toString().replace("'", "''") + "'";
	}

}
