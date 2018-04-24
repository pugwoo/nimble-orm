package com.pugwoo.dbhelper.json;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 封装起来的常用的json方法
 * @author NICK
 */
public class JSON {

	private static ObjectMapper objectMapper = new MyObjectMapper();
	
	public static Object parse(String str) {
		try {
			return objectMapper.readValue(str, Object.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T parse(String str, Class<T> clazz) {
		try {
			return objectMapper.readValue(str, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T parse(String str, Class<T> clazz, Class<?> genericClass) {
		try {
			JavaType type =  objectMapper.getTypeFactory()
					.constructParametricType(clazz, genericClass);
			return objectMapper.readValue(str, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T parse(String str, Class<T> clazz, JavaType genericClass) {
		try {
			JavaType type =  objectMapper.getTypeFactory()
					.constructParametricType(clazz, genericClass);
			return objectMapper.readValue(str, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T parse(String str, Class<T> clazz, Class<?> genericClass1, Class<?> genericClass2) {
		try {
			JavaType type =  objectMapper.getTypeFactory()
					.constructParametricType(clazz, genericClass1, genericClass2);
			return objectMapper.readValue(str, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T parse(String str, Class<T> clazz, JavaType genericClass1, JavaType genericClass2) {
		try {
			JavaType type =  objectMapper.getTypeFactory()
					.constructParametricType(clazz, genericClass1, genericClass2);
			return objectMapper.readValue(str, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 解析字符串为Map
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseToMap(String str) {
		return parse(str, Map.class);
	}
	
	/**
	 * 解析对象，可以通过jackson的ObjectNode读取各种类型值
	 * @param str
	 * @return
	 */
	public static ObjectNode parseObject(String str) {
		return parse(str, ObjectNode.class);
	}
	
	/**
	 * 解析数组
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<JsonNode> parseArray(String str) {
		return parse(str, List.class, JsonNode.class);
	}

	/**
	 * 将对象转换成json字符串
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 转换对象为map
	 * @param obj java bean对象，主要不要传入单个值如string Date等
	 * @return
	 */
	public static Map<String, Object> toMap(Object obj) {
		return parseToMap(toJson(obj));
	}
	
	/**
	 * 使用json的方式克隆对象
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T clone(T t) {
		if(t == null) {
			return null;
		}
		return (T) parse(toJson(t), t.getClass());
	}
	
}
