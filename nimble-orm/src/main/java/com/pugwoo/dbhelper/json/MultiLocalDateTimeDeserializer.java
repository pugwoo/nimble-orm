package com.pugwoo.dbhelper.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class MultiLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

	private static final long serialVersionUID = 1L;

	public MultiLocalDateTimeDeserializer() {
		this(null);
	}
	public MultiLocalDateTimeDeserializer(Class<?> vc) {
		super(vc);
	}
	
	@Override
	public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		String date = node.asText();
		try {
			LocalDateTime localDateTime = NimbleOrmDateUtils.parseLocalDateTime(date);
			if (localDateTime == null) {
				throw new JsonParseException(jp,
						"Unparseable localDateTime: \"" + date + "\". Supported formats: "
								+ NimbleOrmDateUtils.DATE_FORMAT_REGEXPS.values());
			}
			return localDateTime;
		} catch (Exception e) {
			throw new JsonParseException(jp,
					"Unparseable localDateTime: \"" + date + "\". Supported formats: "
							+ NimbleOrmDateUtils.DATE_FORMAT_REGEXPS.values());
		}
	}
	
}