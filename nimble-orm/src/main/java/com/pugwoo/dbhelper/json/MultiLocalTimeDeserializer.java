package com.pugwoo.dbhelper.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalTime;

public class MultiLocalTimeDeserializer extends StdDeserializer<LocalTime> {

	private static final long serialVersionUID = 1L;

	public MultiLocalTimeDeserializer() {
		this(null);
	}
	public MultiLocalTimeDeserializer(Class<?> vc) {
		super(vc);
	}
	
	@Override
	public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		String date = node.asText();

		try {
            return NimbleOrmDateUtils.parseLocalTimeThrowException(date);
		} catch (Exception e) {
			throw new JsonParseException(jp,
					"Unparseable localTime: \"" + date + "\". Supported formats: "
							+ NimbleOrmDateUtils.DATE_FORMAT_REGEXPS.values());
		}
	}
	
}