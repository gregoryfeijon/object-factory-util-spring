package br.com.gregoryfeijon.objectfactoryutil.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.dongliu.gson.GsonJava8TypeAdapterFactory;

/**
 * 
 * 26 de fev de 2020
 * 
 * 
 * 
 * @author gregory.feijon
 * 
 */
public final class GsonUtil {

	private GsonUtil() {}

	// QUANDO NECESSÁRIO ATRIBUTO COM O TIPO ABSTRATO! NECESSÁRIO BIBLIOTECA
	// GSON-EXTRAS
//    .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(Venda.class, "type")
//            .registerSubtype(Romaneio.class, Romaneio.class.getName())
//            .registerSubtype(Pedido.class, Pedido.class.getName())
//            .registerSubtype(Nfe.class, Nfe.class.getName())
//            .registerSubtype(Sat.class, Sat.class.getName()))

	private static final Locale BRASIL = new Locale("pt", "BR");
	private static final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping()
			.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
			.registerTypeAdapterFactory(new GsonJava8TypeAdapterFactory()
					.setLocalDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", BRASIL))
					.setLocalDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd", BRASIL))
					.setLocalTimeFormatter(DateTimeFormatter.ofPattern("HH:mm:ss.SSS", BRASIL)))
			.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).setPrettyPrinting();

	public static Gson getGson() {
		return gsonBuilder.create();
	}

	public static Type getType(Class<?> rawClass, Class<?> genClass) {
		return new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return new Type[] { genClass };
			}

			@Override
			public Type getRawType() {
				return rawClass;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};
	}

	public static Type getListType(Class<?> clazz) {
		return TypeToken.getParameterized(List.class, clazz).getType();
	}

	public static Type getSetType(Class<?> clazz) {
		return TypeToken.getParameterized(Set.class, clazz).getType();
	}

	public static Type getCollectionType(Class<?> clazz) {
		return TypeToken.getParameterized(Collection.class, clazz).getType();
	}
}
