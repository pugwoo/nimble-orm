package com.pugwoo.dbhelper.utils;

/**
 * 2015年8月22日 16:58:48
 * 自动转换类型
 */
public class TypeAutoCast {

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj, Class<T> clazz) {
		if(obj == null) {
			return null;
		}
		if(clazz == String.class) {
			return (T) obj.toString();
		}
		if(clazz == Integer.class) {
			if(obj instanceof Integer) {
				return (T) obj;
			}
			return (T) new Integer(obj.toString());
		}
		if(clazz == Long.class) {
			if(obj instanceof Long) {
				return (T) obj;
			}
			return (T) new Long(obj.toString());
		}

		// TODO 还有更多的类型需要转换
		
		return (T) obj;
	}

}
