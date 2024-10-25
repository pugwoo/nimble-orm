package com.pugwoo.dbhelper.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;

public class MultiLocalDateDeserializer extends StdDeserializer<LocalDate> {

	private static final long serialVersionUID = 1L;

	public MultiLocalDateDeserializer() {
		this(null);
	}
	public MultiLocalDateDeserializer(Class<?> vc) {
		super(vc);
	}
	
	@Override
	public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		String date = node.asText();

		try {
			return NimbleOrmDateUtils.parseLocalDateThrowException(date);
		} catch (Exception e) {
			throw new JsonParseException(jp,
					"Unparseable localDate: \"" + date + "\". Supported formats: "
							+ NimbleOrmDateUtils.DATE_FORMAT_REGEXPS.values());
		}
	}
	
}