package br.com.gregoryfeijon.objectfactoryutil.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Time;
import java.text.Format;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.util.FieldUtils;
import org.springframework.util.SerializationUtils;

import com.google.gson.Gson;

import br.com.gregoryfeijon.objectfactoryutil.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutil.exception.ObjectFactoryUtilException;

/**
 * 
 * 09 de março de 2020
 * 
 * @author gregory.feijon
 * 
 * @see ObjectConstructor
 * 
 */

public final class ObjectFactoryUtil {

	private static final Gson GSON = GsonUtil.getGson();
	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

	private ObjectFactoryUtil() {}

	/**
	 * <strong>Método para retornar um novo objeto criado. Mesma lógica de cópia do
	 * {@linkplain #createFromObject(Object, Object) createFromObject}.</strong>
	 *
	 * @param <T>
	 * @param source - T
	 * @return {@linkplain Object}
	 */
	public static <T> Object createFromObject(T source) {
		Object dest = BeanUtils.instantiateClass(source.getClass());
		createFromObject(source, dest);
		return dest;
	}

	/**
	 * <strong>Método para criar um novo objeto (dest) a partir de um outro objeto
	 * do mesmo tipo (source).</strong>
	 * 
	 * <p>
	 * Primeiramente, obtém a lista dos campos do objeto de origem, dos quais os
	 * valores serão copiados, utilizando o método
	 * {@linkplain #getFieldsToCopy(Object, Object) getFieldsToCopy}, excluindo os
	 * campos definidos no parâmetro {@linkplain ObjectConstructor#exclude()
	 * exclude} Annotation {@linkplain ObjectConstructor}. Em seguida, utiliza o
	 * método {@linkplain ReflectionUtil#getFieldsAsCollection(Object)
	 * getFieldsAsCollection} para obter os campos do objeto destino e setar em cada
	 * um dos correspondentes os valores dos campos obtidos anteriormente,
	 * utilizando os métodos
	 * {@linkplain FieldUtils#setProtectedFieldValue(String, Object, Object)
	 * setProtectedFieldValue} e
	 * {@linkplain FieldUtils#getProtectedFieldValue(String, Object)
	 * getProtectedFieldValue}, do próprio Spring.
	 * <p>
	 * 
	 * @param <T>    - method type definer
	 * @param source - &ltT&gt
	 * @param dest   - &ltT&gt
	 */
	public static <T> void createFromObject(T source, T dest) {
		List<Field> sourceFields = getFieldsToCopy(source, dest);
		sourceFields.stream().forEach(sourceField -> {
			ReflectionUtil.getFieldsAsCollection(dest).stream()
					.filter(destField -> destField.getName().toLowerCase().equals(sourceField.getName().toLowerCase()))
					.findAny().ifPresent(destField -> {
						FieldUtils.setProtectedFieldValue(destField.getName(), dest, verifyValue(sourceField, source));
					});
		});
	}

	/**
	 * <strong>Método que obtém todos os campos que deverão ser copiados do objeto
	 * de origem (source).</strong>
	 * 
	 * <p>
	 * Primeiramente, utiliza-se o método
	 * {@linkplain ReflectionUtil#getFieldsAsCollection(Object)
	 * getFieldsAsCollection} para obter todos os campos do objeto de origem dos
	 * dados. A partir dessa lista, é criada uma outra lista dos campos a remover,
	 * verificando os campos final, que nãos erão trabalhados. Posteriormente, o
	 * objeto de destino é verificado e, caso existam campos definidos no
	 * {@linkplain ObjectConstructor#exclude() exclude} da annotation
	 * {@linkplain ObjectConstructor}, também serão adicionados à lista de exclusão.
	 * Os campos que foram separados são removidos, retornando a lista com os
	 * restantes.
	 * <p>
	 * 
	 * @param <T>    - method type definer
	 * @param source - &ltT&gt
	 * @param dest   - &ltT&gt
	 * @return {@linkplain List}&lt{@linkplain Field}&gt
	 */
	private static <T> List<Field> getFieldsToCopy(T source, T dest) {
		List<Field> sourceFields = new ArrayList<>(ReflectionUtil.getFieldsAsCollection(source));
		List<Field> fieldsToRemove = new LinkedList<>(sourceFields.stream()
				.filter(sourceField -> Modifier.isFinal(sourceField.getModifiers())).collect(Collectors.toList()));
		String[] exclude = getExcludeFromAnnotation(dest);
		if (ArrayUtils.isNotEmpty(exclude)) {
			Arrays.stream(exclude).forEach(excludeField -> {
				Optional<Field> opField = sourceFields.stream()
						.filter(sourceField -> sourceField.getName().toLowerCase().equals(excludeField.toLowerCase()))
						.findAny();
				if (opField.isPresent() && !fieldsToRemove.contains(opField.get())) {
					fieldsToRemove.add(opField.get());
				}
			});
		}
		if (ValidationHelpers.collectionNotEmpty(fieldsToRemove)) {
			sourceFields.removeAll(fieldsToRemove);
		}
		return sourceFields;
	}

	/**
	 * <strong>Método que verifica o Objeto de destino. Se houver a annotation
	 * {@linkplain ObjectConstructor} na classe, retorna o exclude. Caso contrário,
	 * retorna um array vazio.</strong>
	 * 
	 * @param <T>  - method type definer
	 * @param dest - &ltT&gt
	 * @return {@linkplain String}[]
	 */
	private static <T> String[] getExcludeFromAnnotation(T dest) {
		if (dest.getClass().isAnnotationPresent(ObjectConstructor.class)) {
			return dest.getClass().getAnnotation(ObjectConstructor.class).exclude();
		}
		return ArrayUtils.EMPTY_STRING_ARRAY;
	}

	/**
	 * <strong>Método para verificar o tipo do valor copiado, com o intuito de
	 * definir a melhor forma para copiá-lo.</strong>
	 * 
	 * <p>
	 * Verifica se o {@linkplain Field} é um tipo primitivo ou {@linkplain Enum} e,
	 * caso seja, apenas obtem o valor do campo de nome correspondente.
	 * Posteriormente, verifica se é um Wrapper, que será copiado apenas via
	 * serialização. Caso o valor seja uma {@linkplain Collection} ou um
	 * {@linkplain Map}, também possui um fluxo para validação dos tipos e devida
	 * cópia dos valores. Se não for nenhum desses tipos, é necessário utilizar o
	 * método {@linkplain ObjectFactoryUtil#objectCopy(Object) objectCopy}, que cria
	 * uma nova instância do objeto e faz a cópia via serialização, para garantir
	 * que seja feita a cópia por valor, não por referência.
	 * <p>
	 * 
	 * @param <T>
	 * @param sourceField - {@linkplain Field}
	 * @param source      - T
	 * @return {@linkplain Object}
	 */
	private static <T> Object verifyValue(Field sourceField, T source) {
		Object sourceValue = FieldUtils.getProtectedFieldValue(sourceField.getName(), source);
		if (isPrimitiveOrEnum(sourceField.getType())) {
			return sourceValue;
		}
		if (isWrapperType(sourceField.getType())) {
			return serializingClone(sourceValue, sourceField.getType());
		}
		if (isClassMapCollection(sourceField.getType())) {
			return serializingClone(sourceValue, sourceField.getGenericType());
		}
		try {
			return objectCopy(sourceValue, sourceField.getType());
		} catch (Exception ex) {
			throw new ObjectFactoryUtilException(ex.getMessage());
		}
	}

	/**
	 * <strong>Método para copiar o valor de objetos do tipo
	 * 
	 * <i>Wrappers</i>.</strong>
	 * 
	 * @param sourceValue - {@linkplain Object}
	 * @param clazz       - {@linkplain Class}&lt?&gt
	 * @return {@linkplain Object}
	 */
	private static Object serializingClone(Object sourceValue, Class<?> clazz) {
		Object clone = null;
		if (sourceValue != null) {
			clone = serializingClone(clone, sourceValue, clazz);
		}
		return clone;
	}

	/**
	 * <strong>Método que efetivamente faz a cópia dos valores via serialização, nos
	 * 
	 * casos de <i>Wrappers</i> e objetos.</strong>
	 * 
	 * @param clone       - {@linkplain Object}
	 * @param sourceValue - {@linkplain Object}
	 * @param clazz       - {@linkplain Class}&lt?&gt
	 * @return {@linkplain Object}
	 */
	private static Object serializingClone(Object clone, Object sourceValue, Class<?> clazz) {
		byte[] byteClone;
		if (clazz.isPrimitive() || isWrapperType(clazz)) {
			byteClone = SerializationUtils.serialize(sourceValue);
			clone = SerializationUtils.deserialize(byteClone);
		} else {
			byteClone = SerializationUtil.serializaJsonDeUmObjetoGetAsByte(sourceValue);
			clone = GSON.fromJson(SerializationUtil.getDesserealizedObjectAsString(byteClone), clazz);
		}
		return clone;
	}

	/**
	 * <strong>Método que efetivamente faz a cópia do valor via serialização para
	 * {@linkplain Collection} e {@linkplain Map}.</strong>
	 * 
	 * @param sourceValue - {@linkplain Object}
	 * @param genericType - {@linkplain Type}
	 * @return {@linkplain Object}
	 */
	private static Object serializingClone(Object sourceValue, Type genericType) {
		Object clone = null;
		if (sourceValue != null) {
			try {
				byte[] byteClone = SerializationUtil.serializaJsonDeUmObjetoGetAsByte(sourceValue);
				if (isCollection(sourceValue.getClass())) {
					clone = verifyList(sourceValue, genericType, byteClone);
				} else {
					clone = GSON.fromJson(SerializationUtil.getDesserealizedObjectAsString(byteClone), genericType);
				}
			} catch (IOException | ClassNotFoundException ex) {
				throw new ObjectFactoryUtilException("Erro ao deserializar collection na cópia de objeto.", ex);
			}
		}
		return clone;
	}

	/**
	 * <strong>Método que executa a verificação da lista, para poder definir os
	 * tipos corretamente para a cópia do valor via serialização usando o
	 * {@linkplain Gson}, configurado na classe {@linkplain GsonUtil}.</strong>
	 * 
	 * <p>
	 * Primeiramente passa por uma verificação do tipo e, caso passe, executa a
	 * desserialização usando o {@linkplain Type genericType} normalmente. Caso
	 * capture alguma exception no processo, significa que possui algum tipo
	 * genérico. Nesse caso, passará por um processo para identificar o tipo
	 * utilizado em <i>Runtime</i> através do valor, para que possa ser feita a
	 * desserialização da forma devida.
	 * <p>
	 * 
	 * @param sourceValue - {@linkplain Object}
	 * @param genericType - {@linkplain Type}
	 * @param byteClone   - byte[]
	 * @return {@linkplain Object}
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private static Object verifyList(Object sourceValue, Type genericType, byte[] byteClone)
			throws IOException, ClassNotFoundException {
		Object clone = null;
		try {
			verifyType(genericType);
			clone = desserializeCollection(byteClone, genericType);
		} catch (BeanInstantiationException | ClassNotFoundException ex) {
			List<Object> aux = Collections.checkedCollection((Collection<Object>) sourceValue, Object.class).stream()
					.collect(Collectors.toList());
			if (ValidationHelpers.collectionNotEmpty(aux)) {
				Class<?> objectType = aux.get(0).getClass();
				clone = desserializeCollection(byteClone, GsonUtil.getType(getRawType(genericType), objectType));
			}
		}
		return clone;
	}

	/**
	 * <strong>Método para verificar o tipo dos parâmetros da
	 * {@linkplain Collection}, de modo a conseguir definir os tipos utilizados na
	 * desserialização.</strong>
	 * <p>
	 * Primeiramente passa por uma verificação dos parâmetros da
	 * {@linkplain Collection}, que identifica se o valor é um tipo primitivo,
	 * {@linkplain Enum} ou <i>Wrapper</i>. Caso não seja, tenta instanciar através
	 * do seu tipo.
	 * <p>
	 * 
	 * @param genericType - {@linkplain Type}
	 * @throws ClassNotFoundException
	 * @throws BeanInstantiationException
	 */
	private static void verifyType(Type genericType) throws ClassNotFoundException, BeanInstantiationException {
		ParameterizedType typeTest = (ParameterizedType) genericType;
		for (Type type : Arrays.asList(typeTest.getActualTypeArguments())) {
			Class<?> clazz = Class.forName(type.getTypeName());
			if (!isPrimitiveOrEnum(clazz) && !isWrapperType(clazz)) {
				BeanUtils.instantiateClass(clazz);
			}
		}
	}

	/**
	 * <strong>Método para desserialização, usando as classes utils
	 * {@linkplain GsonUtil} e {@linkplain SerializationUtil}.</strong>
	 * 
	 * @param byteClone   - byte[]
	 * @param genericType - {@linkplain Type}
	 * @return {@linkplain Object}
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static Object desserializeCollection(byte[] byteClone, Type genericType)
			throws IOException, ClassNotFoundException {
		return GSON.fromJson(SerializationUtil.getDesserealizedObjectAsString(byteClone), genericType);
	}

	/**
	 * <strong>Método para obter o tipo de {@linkplain Collection} utilizado no
	 * atributo.</strong>
	 * 
	 * @param genericType - {@linkplain Type}
	 * @return {@linkplain Class}&lt?&gt
	 * @throws ClassNotFoundException
	 */
	private static Class<?> getRawType(Type genericType) throws ClassNotFoundException {
		return Class.forName(((ParameterizedType) genericType).getRawType().getTypeName());
	}

	/**
	 * <strong>Método para a cópia do valor de um objeto que não seja <i>Wrapper</i>
	 * nem {@linkplain Collection}.</strong>
	 * 
	 * @param sourceValue - {@linkplain Object}
	 * @param clazz       - {@linkplain Class}&lt?&gt
	 * @return {@linkplain Object}
	 */
	private static Object objectCopy(Object sourceValue, Class<?> clazz) {
		Object clone = null;
		if (sourceValue != null) {
			if (!isClassMapCollection(clazz)) {
				clone = BeanUtils.instantiateClass(clazz);
			}
			clone = serializingClone(clone, sourceValue, clazz);
		}
		return clone;
	}

	/**
	 * <strong>Método para verificar se é um tipo primitivo ou
	 * {@linkplain Enum}.</strong>
	 * 
	 * @param type - {@linkplain Class}&lt?&gt
	 * @return boolean
	 */
	private static boolean isPrimitiveOrEnum(Class<?> type) {
		return type.isPrimitive() || type.isEnum();
	}

	/**
	 * <strong>Método para verificar se é uma {@linkplain Collection} ou um
	 * {@linkplain Map}.</strong>
	 * 
	 * @param clazz - {@linkplain Class}&lt?&gt
	 * @return boolean
	 */
	private static boolean isClassMapCollection(Class<?> clazz) {
		return isCollection(clazz) || isMap(clazz);
	}

	/**
	 * <strong>Método para verificar se é uma {@linkplain Collection}.</strong>
	 * 
	 * @param clazz - {@linkplain Class}&lt?&gt
	 * @return boolean
	 */
	private static boolean isCollection(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz);
	}

	/**
	 * <strong>Método para verificar se é um {@linkplain Map}.</strong>
	 * 
	 * @param clazz - {@linkplain Class}&lt?&gt
	 * @return boolean
	 */
	private static boolean isMap(Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}

	/**
	 * <strong>Método responsável por verificar se o tipo do valor sendo copiado é
	 * um wrapper.</strong>
	 * 
	 * @param clazz - {@linkplain Class}&lt?&gt
	 * @return {@linkplain Boolean}
	 */
	private static boolean isWrapperType(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz)
				|| WRAPPER_TYPES.stream().anyMatch(wrapper -> wrapper.isAssignableFrom(clazz));
	}

	/**
	 * <strong>Método responsável por criar o {@linkplain Set} com os <i>wrapper
	 * types</i></strong>
	 * 
	 * @return {@linkplain Set}&lt{@linkplain Class}&lt?&gt&gt
	 */
	private static Set<Class<?>> getWrapperTypes() {
		Set<Class<?>> wrappers = new HashSet<>();
		wrappers.add(Boolean.class);
		wrappers.add(Byte.class);
		wrappers.add(UUID.class);
		wrappers.addAll(numberTypes());
		wrappers.addAll(dateTypes());
		wrappers.addAll(textTypes());
		return wrappers;
	}

	/**
	 * <strong>Método responsável por criar o {@linkplain Set} com os <i>wrapper
	 * types</i> de textos.</strong>
	 * 
	 * @return {@linkplain Set}&lt{@linkplain Class}&lt?&gt&gt
	 */
	private static Set<Class<?>> textTypes() {
		Set<Class<?>> aux = new HashSet<>();
		aux.add(String.class);
		aux.add(Character.class);
		aux.add(Format.class);
		return aux;
	}

	/**
	 * <strong>Método responsável por criar o {@linkplain Set} com os <i>wrapper
	 * types</i> de datas/horas.</strong>
	 * 
	 * @return {@linkplain Set}&lt{@linkplain Class}&lt?&gt&gt
	 */
	private static Set<Class<?>> dateTypes() {
		Set<Class<?>> aux = new HashSet<>();
		aux.add(Date.class);
		aux.add(Time.class);
		aux.add(LocalDateTime.class);
		aux.add(LocalDate.class);
		aux.add(LocalTime.class);
		aux.add(Temporal.class);
		aux.add(Instant.class);
		return aux;
	}

	/**
	 * <strong>Método responsável por criar o {@linkplain Set} com os <i>wrapper
	 * types</i> de números.</strong>
	 * 
	 * @return {@linkplain Set}&lt{@linkplain Class}&lt?&gt&gt
	 */
	private static Set<Class<?>> numberTypes() {
		Set<Class<?>> aux = new HashSet<>();
		aux.add(Integer.class);
		aux.add(Double.class);
		aux.add(Float.class);
		aux.add(Long.class);
		aux.add(Number.class);
		return aux;
	}
}