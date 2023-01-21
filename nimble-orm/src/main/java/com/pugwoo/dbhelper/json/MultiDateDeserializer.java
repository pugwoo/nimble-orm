package com.pugwoo.dbhelper.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class MultiDateDeserializer extends StdDeserializer<Date> {
	
	private static final long serialVersionUID = 1L;

	public MultiDateDeserializer() {
		this(null);
	}
	public MultiDateDeserializer(Class<?> vc) {
		super(vc);
	}
	
	@Override
	public Date deserialize(JsonParser jp, DeserializationContext context) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		String date = node.asText();
		try {
			return NimbleOrmDateUtils.parseThrowException(date);
		} catch (ParseException e) {
			throw new JsonParseException(jp,
			    "cannot parse date: \"" + date + "\". Supported formats: "
			    + NimbleOrmDateUtils.DATE_FORMAT_REGEXPS.values());
		}
	}
	
}