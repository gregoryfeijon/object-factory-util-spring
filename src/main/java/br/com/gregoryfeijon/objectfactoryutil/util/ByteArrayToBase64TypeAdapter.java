package br.com.gregoryfeijon.objectfactoryutil.util;

import java.lang.reflect.Type;
import java.util.Base64;

import org.springframework.boot.json.JsonParseException;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * 23/09/2020
 *
 * 
 * 
 * @author gregory.feijon
 *
 * 
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
