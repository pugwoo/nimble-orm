package com.pugwoo.dbhelper.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 封装起来的常用的json方法
 * @author NICK
 */
public class NimbleOrmJSON {

	private static final Logger LOGGER = LoggerFactory.getLogger(NimbleOrmJSON.class);

	private static final ObjectMapper objectMapper = new MyObjectMapper();

	/**
	 * 将对象转换成json字符串
	 * @param obj 需要转出json的对象
	 * @return 转换后的json语句
	 */
	public static String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parse(String json, Class<T> clazz) throws IOException {
		return objectMapper.readValue(json, clazz);
	}

	public static Object parseGeneric(String json, String typeName) throws ClassNotFoundException, IOException {
		JavaType type = parseGenericType(typeName);
		return objectMapper.readValue(json, type);
	}

	public static Object parseGeneric(String json, ParameterizedType type) throws ClassNotFoundException, IOException {
		JavaType javaType = toJavaType(type);
		return objectMapper.readValue(json, javaType);
	}

	private static JavaType toJavaType(ParameterizedType type) {
		TypeFactory typeFactory = objectMapper.getTypeFactory();

		Type rawType = type.getRawType();
		Type[] actualTypeArguments = type.getActualTypeArguments();

		JavaType[] javaTypes = new JavaType[actualTypeArguments.length];
		for (int i = 0; i < actualTypeArguments.length; i++) {
			if (actualTypeArguments[i] instanceof Class) {
				javaTypes[i] = typeFactory.constructType(actualTypeArguments[i]);
			} else if (actualTypeArguments[i] instanceof ParameterizedType) {
				javaTypes[i] = toJavaType((ParameterizedType) actualTypeArguments[i]);
			} else {
				LOGGER.error("unknown actualTypeArguments type:{} in type:{}",
						actualTypeArguments[i], type);
			}
		}

		return typeFactory.constructParametricType((Class<?>) rawType, javaTypes);
	}

	/**
	 * 解析泛型的类,只支持1个或2个的泛型类型，不支持3个及以上的
	 * @param className 要解析出泛型的类名
	 * @return 如果没有泛型，则返回null
	 */
	private static JavaType parseGenericType(String className)
			throws ClassNotFoundException {

		TypeFactory typeFactory = objectMapper.getTypeFactory();

		if(className == null) {
			return null;
		}
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
					parseGenericType(genericAll));
		} else {
			String leftClassName = genericAll.substring(0, dotIndex);
			String rightClassName = genericAll.substring(dotIndex + 1);
			return typeFactory.constructParametricType(Class.forName(baseClassName.trim()),
					parseGenericType(leftClassName),
					parseGenericType(rightClassName));
		}
	}

	private static int getDotIndex(String str) {
		if(str == null) {
			return -1;
		}
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
		if(str == null) {
			return;
		}
		int counts = 0;
		int bracket = 0;
		for(char c : str.toCharArray()/*int i = 0; i < str.length(); i++*/) {
			//char c = str.charAt(i);
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
			throw new RuntimeException("nimble-orm not support more than two generic type, found " + (counts+1)
					+ " for class:" +str);
		}
	}

}
