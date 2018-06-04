package com.pugwoo.dbhelper.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class MyObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 7802045661502663726L;

	public MyObjectMapper() {
		super();

		setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // 设置日期格式
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //属性不存在的兼容处理
		configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 对于没有任何getter的bean序列化不抛异常
		configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true); // 对枚举中不存在的值，设置为null
		configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true); // 自动将空字符串转成null值传入Object
		
		getSerializerProvider().setNullKeySerializer(new NullKeySerializer()); // 当map含有null key时，转成空字符串
		
		configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true); //属性key可以不用括号
		configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true); //属性key使用单引号
		configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true); //允许数字以0开头
		configure(JsonParser.Feature.ALLOW_MISSING_VALUES, true); //允许[]中有多个,,,
		configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true); //允许[]最后带多一个,
	
		MultiDateDeserializer deserializer = new MultiDateDeserializer();
		SimpleModule module = new SimpleModule("DateDeserializerModule",
		      new Version(1, 0, 0, "", "", ""));
		module.addDeserializer(Date.class, deserializer);
		
		registerModule(module);
	}
	
}
