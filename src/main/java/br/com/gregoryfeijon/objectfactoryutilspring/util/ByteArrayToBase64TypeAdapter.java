package br.com.gregoryfeijon.objectfactoryutilspring.util;

import com.google.gson.*;
import org.springframework.boot.json.JsonParseException;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * 
 * 23/09/2020
 *
 * @author gregory.feijon
 *
 */

public class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

	@Override
	public JsonElement serialize(byte[] src, Type type, JsonSerializationContext context) {
		return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
	}

	@Override
	public byte[] deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		return Base64.getDecoder().decode(json.getAsString());
	}
}
