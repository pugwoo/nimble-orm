package com.pugwoo.dbhelper.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
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

	/**
	 * 将对象转换成json字符串，不要抛出异常，一般用于log场景
	 * @param obj 需要转出json的对象
	 * @return 转换后的json语句
	 */
	public static String toJsonNoException(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Throwable e) {
			return "JSON PARSE EXCEPTION:" + e.getClass() + " " + e.getMessage();
		}
	}

	public static <T> T parse(String json, Class<T> clazz) throws IOException {
		if (InnerCommonUtils.isBlank(json)) {
			return null;
		}
		return objectMapper.readValue(json, clazz);
	}

	public static Object parseGeneric(String json, ParameterizedType type) throws IOException {
		if (InnerCommonUtils.isBlank(json)) {
			return null;
		}
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

}
