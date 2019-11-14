package com.pugwoo.dbhelper.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 封装起来的常用的json方法
 * @author NICK
 */
public class NimbleOrmJSON {

	private static ObjectMapper objectMapper = new MyObjectMapper();

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

}
