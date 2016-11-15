package com.pugwoo.dbhelper.utils;

import java.math.BigDecimal;

/**
 * 2015年8月22日 16:58:48
 * 自动转换类型
 * @author pugwoo
 */
public class TypeAutoCast {

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

}
