package br.com.gregoryfeijon.objectfactoryutilspring.util;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutilspring.exception.ObjectFactoryUtilException;
import com.google.gson.Gson;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.util.FieldUtils;
import org.springframework.util.SerializationUtils;

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
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

/**
 * 09 de março de 2020
 *
 * @author gregory.feijon
 * @see ObjectConstructor
 */

public final class ObjectFactoryUtil {
    private static final Gson GSON;
    private static final Set<Class<?>> WRAPPER_TYPES;
    private static final Predicate<Field> PREDICATE_MODIFIERS;
    private static final Map<Class<?>, Object> DEFAULT_VALUES = new HashMap<>();

    private ObjectFactoryUtil() {}

    static {
        GSON = GsonUtil.getGson();
        WRAPPER_TYPES = getWrapperTypes();
        PREDICATE_MODIFIERS = predicateModifiers();
        createMapDefaultValues();
    }

    /**
     * <strong>Método que retorna todos os objetos de uma {@linkplain Collection
     * coleção} copiados.</strong>
     *
     * @param <T>            - define o tipo da collection copiada e da resultante
     * @param entitiesToCopy - {@linkplain Collection}&ltT&gt
     * @return {@linkplain List}&ltT&gt
     */
    public static <T> List<T> copyAllObjectsFromCollection(Collection<T> entitiesToCopy) {
        verifyCollection(entitiesToCopy);
        return entitiesToCopy.stream().map(createCopy()).collect(Collectors.toList());
    }

    /**
     * <strong>Método que retorna uma cópia de uma lista de objetos.</strong>
     *
     * <p>
     * O Tipo da coleção retornada não precisa ser igual ao tipo da coleção copiada,
     * basta que os objetos possuam atributos com o mesmo nome, que os valores
     * desses atributos serão copiados.
     * <p>
     *
     * @param <T>            - define o tipo da collection resultante
     * @param entitiesToCopy - {@linkplain Collection}&lt?&gt
     * @param returnType     - {@linkplain Class}&ltT&gt
     * @return {@linkplain List}&ltT&gt
     */
    public static <T> List<T> copyAllObjectsFromCollection(Collection<?> entitiesToCopy, Class<T> returnType) {
        verifyCollection(entitiesToCopy);
        return entitiesToCopy.stream().map(createCopy(returnType)).collect(Collectors.toList());
    }

    /**
     * <strong> Método para copiar todos os elementos de uma {@linkplain Collection
     * coleção} e retornar em um tipo escolhido de {@linkplain Collection
     * coleção}.</strong>
     *
     * @param <T>            - define o tipo da collection copiada
     * @param <U>            - define o tipo da collection resultante, gerada através do supplier
     * @param entitiesToCopy - {@linkplain Collection}&ltT&gt
     * @param supplier       - {@linkplain Supplier}&ltU&gt
     * @return U
     */
    public static <T, U extends Collection<T>> U copyAllObjectsFromCollection(Collection<T> entitiesToCopy,
                                                                              Supplier<U> supplier) {
        verifyCollectionAndSupplier(entitiesToCopy, supplier);
        return entitiesToCopy.stream().map(createCopy()).collect(Collectors.toCollection(supplier));
    }

    /**
     * <strong> Método para copiar todos os elementos de uma {@linkplain Collection
     * coleção} e retornar em um tipo escolhido de {@linkplain Collection
     * coleção}.</strong>
     *
     * <p>
     * O Tipo da coleção retornada não precisa ser igual ao tipo da coleção copiada,
     * basta que os objetos possuam atributos com o mesmo nome, que os valores
     * desses atributos serão copiados.
     * <p>
     *
     * @param <T>            - define o tipo dos objetos da collection resultante
     * @param <U>            - define o tipo da collection resultante, gerada através do supplier
     * @param entitiesToCopy - {@linkplain Collection}&lt?&gt
     * @param supplier       - {@linkplain Supplier}&ltU&gt
     * @param returnType     - {@linkplain Class}&ltT&gt
     * @return U
     */
    public static <T, U extends Collection<T>> U copyAllObjectsFromCollection(Collection<?> entitiesToCopy,
                                                                              Supplier<U> supplier, Class<T> returnType) {
        verifyCollectionAndSupplier(entitiesToCopy, supplier);
        return entitiesToCopy.stream().map(createCopy(returnType)).collect(Collectors.toCollection(supplier));
    }

    /**
     * <strong> Método para verificar se a {@linkplain Collection collection}
     * copiada ou o {@linkplain Supplier supplier} estão null. </strong>
     *
     * @param <T>            - define o tipo da collection copiada
     * @param <U>            - define o tipo da collection resultante, gerada através do supplier
     * @param entitiesToCopy - {@linkplain Collection}&ltT&gt
     * @param supplier       - {@linkplain Supplier}&ltU&gt
     */
    private static <T, U> void verifyCollectionAndSupplier(Collection<T> entitiesToCopy, Supplier<U> supplier) {
        verifyCollection(entitiesToCopy);
        if (supplier == null) {
            throw new ObjectFactoryUtilException("O tipo de coleção especificada para retorno é nulo.");
        }
    }

    /**
     * <strong>Método para verificar se a {@linkplain Collection collection} a ser
     * copiada está vazia.</strong>
     *
     * @param <T>            define o tipo da collection verificada
     * @param entitiesToCopy - {@linkplain Collection}&ltT&gt
     */
    private static <T> void verifyCollection(Collection<T> entitiesToCopy) {
        if (ValidationHelpers.collectionEmpty(entitiesToCopy)) {
            throw new ObjectFactoryUtilException("A lista a ser copiada não possui elementos.");
        }
    }

    /**
     * <strong>Function executada para criar a cópia dos objetos da lista
     * passada.</strong>
     *
     * @param <T> define o tipo da function testada e do resultado.
     * @return {@linkplain Function}&ltT, T&gt
     */
    private static <T> Function<T, T> createCopy() {
        return ObjectFactoryUtil::createFromObject;
    }

    /**
     * <strong>Function executada para criar a cópia dos objetos da lista
     * passada, considerando o tipo de retorno especificado.</strong>
     *
     * @param <T> - define o tipo do objecto copiado
     * @param <S> - define o tipo do objeto retornado
     * @return {@linkplain Function}&ltT, S&gt
     */
    private static <T, S> Function<S, T> createCopy(Class<T> returnType) {
        return i -> createFromObject(i, returnType);
    }

    /**
     * <strong>Método que retorna um objeto copiado à partir de outro de tipo
     * DIFERENTE. Utiliza a lógica do {@linkplain #createFromObject(Object, Object)
     * createFromObject} para copiar </strong>
     *
     * @param <T>        - define o tipo do objeto retornado
     * @param <S>        - define o tipo do objetro copiado
     * @param source     S
     * @param returnType {@linkplain Class}&ltT&gt
     * @return T
     */
    public static <T, S> T createFromObject(S source, Class<T> returnType) {
        verifySourceObject(source);
        T dest = BeanUtils.instantiateClass(returnType);
        createFromObject(source, dest);
        return dest;
    }

    /**
     * <strong>Método para retornar um novo objeto criado do mesmo tipo do objeto
     * utilizado para a cópia. Mesma lógica de cópia do
     * {@linkplain #createFromObject(Object, Object) createFromObject}.</strong>
     *
     * @param <T>    - define o tipo dos objetos copiado e retornado
     * @param source - T
     * @return {@linkplain Object}
     */
    @SuppressWarnings("unchecked")
    public static <T> T createFromObject(T source) {
        verifySourceObject(source);
        Object dest = BeanUtils.instantiateClass(source.getClass());
        createFromObject(source, dest);
        return (T) dest;
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
     * @param <T>    - define o tipo do objeto retornado
     * @param <S>    - define o tipo do objeto copiado
     * @param source - S
     * @param dest   - T
     */
    public static <T, S> void createFromObject(S source, T dest) {
        verifySourceAndDestObjects(source, dest);
        List<Field> sourceFields = getFieldsToCopy(source, dest);
        sourceFields.parallelStream().forEach(sourceField -> ReflectionUtil.getFieldsAsCollection(dest).stream()
                .filter(destField -> destField.getName().equalsIgnoreCase(sourceField.getName()))
                .findAny().ifPresent(destField -> {
                    Object sourceValue = verifyValue(sourceField, destField, source);
                    FieldUtils.setProtectedFieldValue(destField.getName(), dest, sourceValue);
                }));
    }

    /**
     * <strong>Método para verificar se os objetos de origem e destino são
     * válidos.</strong>
     *
     * @param <T>    - define o tipo do objeto retornado
     * @param <S>    - define o tipo do objeto copiado
     * @param source S
     * @param dest   T
     */
    private static <T, S> void verifySourceAndDestObjects(S source, T dest) {
        verifySourceObject(source);
        if (dest == null) {
            throw new ObjectFactoryUtilException("O objeto de destino é nulo.");
        }
    }

    /**
     * <strong> Método para verificar se o objeto a ser copiado é válido.</strong>
     *
     * @param <S>    - define o tipo do objeto copiado
     * @param source S
     */
    private static <S> void verifySourceObject(S source) {
        if (source == null) {
            throw new ObjectFactoryUtilException("O objeto a ser copiado é nulo.");
        }
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
     * @param <T>    - define o tipo do objeto retornado
     * @param <S>    - define o tipo do objeto copiado
     * @param source - S
     * @param dest   - T
     * @return {@linkplain List}&lt{@linkplain Field}&gt
     */
    private static <T, S> List<Field> getFieldsToCopy(S source, T dest) {
        List<Field> sourceFields = new ArrayList<>(ReflectionUtil.getFieldsAsCollection(source));
        List<Field> fieldsToRemove = sourceFields.stream().filter(PREDICATE_MODIFIERS)
                .collect(Collectors.toCollection(LinkedList::new));
        String[] exclude = getExcludeFromAnnotation(dest);
        if (ArrayUtils.isNotEmpty(exclude)) {
            getFieldsAnnotatedToExclude(fieldsToRemove, sourceFields, exclude);
        }
        if (ValidationHelpers.collectionNotEmpty(fieldsToRemove)) {
            sourceFields.removeAll(fieldsToRemove);
        }
        return sourceFields;
    }

    /**
     * <strong>Método que adiciona os campos especificados na annotation especificada no tipo da classe de destino dos
     * dados copiados.</strong>
     *
     * @param fieldsToRemove {@linkplain List}&lt{@linkplain Field}&gt
     * @param sourceFields   {@linkplain List}&lt{@linkplain Field}&gt
     * @param exclude        {@linkplain String}[]
     */
    private static void getFieldsAnnotatedToExclude(List<Field> fieldsToRemove, List<Field> sourceFields, String[] exclude) {
        stream(exclude).forEach(excludeField -> {
            Optional<Field> opField = sourceFields.stream()
                    .filter(sourceField -> sourceField.getName().equalsIgnoreCase(excludeField))
                    .findAny();
            if (opField.isPresent() && !fieldsToRemove.contains(opField.get())) {
                fieldsToRemove.add(opField.get());
            }
        });
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
     * <strong>Método para verificar os casos especiais em que os tipos do objeto de
     * origem e destino são diferentes e é necessário um tratamento específico para
     * retornar o valor correto.</strong>
     *
     * <p>
     * Faz tratamento específicos entre Wrappers e tipos primitivos, tanto do
     * atributo copiado, quanto do destino. Também possui um tratamento específico
     * no caso do atributo do objeto copiado ser uma {@linkplain String} e o
     * atributo do destino ser um {@linkplain Enum}. No caso de
     * {@linkplain Collection} ou {@linkplain Map}, apenas retorna null, pois é um
     * tratamento mais específico de implementação.
     * <p>
     *
     * @param <S>         - define o tipo do objeto copiado
     * @param sourceField - {@linkplain Field}
     * @param destField   - {@linkplain Field}
     * @param source      - S
     * @return {@linkplain Object}
     */
    private static <S> Object  verifyValue(Field sourceField, Field destField, S source) {
        Object sourceValue = FieldUtils.getProtectedFieldValue(sourceField.getName(), source);
        Class<?> sourceFieldType = sourceField.getType();
        Class<?> destFieldType = destField.getType();

        if (sourceFieldType == destFieldType) {
            return copyValue(sourceField, destField, sourceValue);
        }

        if (isWrapperType(sourceFieldType) && destFieldType.isPrimitive() && sourceValue == null) {
            return defaultValueFor(destFieldType);
        }

        if (isWrapperType(destFieldType) && sourceFieldType.isPrimitive() && Objects.equals(sourceValue, defaultValueFor(sourceFieldType))) {
            return null;
        }

        if (sourceFieldType.isEnum() || destFieldType.isEnum()) {
            return validateEnums(sourceField, destField, sourceValue);
        }

        if (isClassMapCollection(destFieldType) || isClassMapCollection(sourceFieldType)) {
            return null;
        }

        return copyValue(sourceField, destField, sourceValue);
    }

    /**
     * <strong>Método validação de enum, para o caso de algum dos valores dos atributos envolvidos
     * na cópia seja do tipo enum</strong>
     *
     * @param sourceField - {@linkplain Field}
     * @param destField   - {@linkplain Field}
     * @param sourceValue - {@linkplain Object}
     * @return {@linkplain Object}
     */
    private static Object validateEnums(Field sourceField, Field destField, Object sourceValue) {
        Class<?> sourceFieldType = sourceField.getType();
        Class<?> destFieldType = destField.getType();
        if (destFieldType.isEnum()) {
            if (sourceFieldType.equals(String.class)) {
                return findEnumConstantEquivalent(destFieldType, sourceValue);
            } else if (sourceFieldType.isEnum() && sourceValue != null) {
                return findEnumConstantEquivalent(destFieldType, sourceValue.toString());
            }
        }
        if (sourceFieldType.isEnum() && (sourceValue != null && destFieldType.equals(String.class))) {
            return sourceValue.toString();
        }
        return null;
    }

    /**
     * <strong> Método para encontrar a constante enum equivalente à String que está
     * sendo copiada.</strong>
     *
     * @param type        - {@linkplain Class}&lt?&gt
     * @param sourceValue {@linkplain Object}
     * @return {@linkplain Object}
     */
    private static Object findEnumConstantEquivalent(Class<?> type, Object sourceValue) {
        Object[] returnValue = {null};
        Stream.of(type.getEnumConstants()).forEach(enumConstant -> {
            if (Objects.equals(enumConstant.toString(), sourceValue)) {
                returnValue[0] = enumConstant;
            }
        });
        return returnValue[0];
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
     * método {@linkplain ObjectFactoryUtil#serializingCloneObjects(Object, Class) objectCopy}, que cria
     * uma nova instância do objeto e faz a cópia via serialização, para garantir
     * que seja feita a cópia por valor, não por referência.
     * <p>
     *
     * @param sourceField - {@linkplain Field}
     * @param destField   - {@linkplain Field}
     * @param sourceValue - {@linkplain Object}
     * @return {@linkplain Object}
     */
    private static Object copyValue(Field sourceField, Field destField, Object sourceValue) {
        Class<?> sourceFieldType = sourceField.getType();
        Class<?> destFieldType = destField.getType();
        if (isPrimitiveOrEnum(sourceFieldType)) {
            return sourceValue;
        }
        if (isWrapperType(sourceFieldType)) {
            return serializingClone(sourceValue, destFieldType);
        }
        if (isClassMapCollection(sourceField.getType())) {
            return serializingCloneCollectionMap(sourceValue, destField.getGenericType());
        }
        try {
            return serializingCloneObjects(sourceValue, destFieldType);
        } catch (Exception ex) {
            throw new ObjectFactoryUtilException(ex.getMessage());
        }
    }

    /**
     * <strong>Método para copiar o valor de objetos do tipo
     * <i>Wrappers</i>.</strong>
     *
     * @param sourceValue - {@linkplain Object}
     * @param clazz       - {@linkplain Class}&lt?&gt
     * @return {@linkplain Object}
     */
    private static Object serializingClone(Object sourceValue, Class<?> clazz) {
        if (sourceValue != null) {
            return serializingCloneObjects(sourceValue, clazz);
        }
        return null;
    }

    /**
     * <strong>Método que efetivamente faz a cópia dos valores via serialização, nos
     * casos de <i>Wrappers</i> e objetos.</strong>
     *
     * @param sourceValue - {@linkplain Object}
     * @param clazz       - {@linkplain Class}&lt?&gt
     * @return {@linkplain Object}
     */
    private static Object serializingCloneObjects(Object sourceValue, Class<?> clazz) {
        Object clone;
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
    private static Object serializingCloneCollectionMap(Object sourceValue, Type genericType) {
        Object clone = null;
        if (sourceValue != null) {
            try {
                byte[] byteClone = SerializationUtil.serializaJsonDeUmObjetoGetAsByte(sourceValue);
                if (isCollection(sourceValue.getClass())) {
                    clone = verifyList(sourceValue, genericType, byteClone);
                } else {
                    clone = GSON.fromJson(SerializationUtil.getDesserealizedObjectAsString(byteClone), genericType);
                }
            } catch (ClassNotFoundException ex) {
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
     * @throws ClassNotFoundException - exception lançada para testar se o tipo existe
     */
    @SuppressWarnings("unchecked")
    private static Object verifyList(Object sourceValue, Type genericType, byte[] byteClone) throws ClassNotFoundException {
        Object clone = null;
        try {
            verifyType(genericType);
            clone = desserializeCollection(byteClone, genericType);
        } catch (BeanInstantiationException | ClassNotFoundException ex) {
            List<Object> aux = new ArrayList<>(Collections.checkedCollection((Collection<Object>) sourceValue, Object.class));
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
     *
     * <p>
     * Primeiramente passa por uma verificação dos parâmetros da
     * {@linkplain Collection}, que identifica se o valor é um tipo primitivo,
     * {@linkplain Enum} ou <i>Wrapper</i>. Caso não seja, tenta instanciar através
     * do seu tipo.
     * <p>
     *
     * @param genericType - {@linkplain Type}
     * @throws ClassNotFoundException     - exception lançada para testar se o tipo existe
     * @throws BeanInstantiationException - exception lançada para testar se o tipo é instanciável
     */
    private static void verifyType(Type genericType) throws ClassNotFoundException, BeanInstantiationException {
        ParameterizedType typeTest = (ParameterizedType) genericType;
        for (Type type : typeTest.getActualTypeArguments()) {
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
     */
    private static Object desserializeCollection(byte[] byteClone, Type genericType) {
        return GSON.fromJson(SerializationUtil.getDesserealizedObjectAsString(byteClone), genericType);
    }

    /**
     * <strong>Método para obter o tipo de {@linkplain Collection} utilizado no
     * atributo.</strong>
     *
     * @param genericType - {@linkplain Type}
     * @return {@linkplain Class}&lt?&gt
     * @throws ClassNotFoundException - exception lançada para testar se o tipo existe
     */
    private static Class<?> getRawType(Type genericType) throws ClassNotFoundException {
        return Class.forName(((ParameterizedType) genericType).getRawType().getTypeName());
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

    /**
     * <strong>Método responsável por criar o predicate que verifica se o campo
     * testado possui os modificadores <i>static</i> e <i>final</i>, que,
     * normalmente, caracteriza uma constante, cujo valor não precisa ser
     * copiado.</strong>
     *
     * @return {@linkplain Predicate}&lt{@linkplain Field}&gt
     */
    private static Predicate<Field> predicateModifiers() {
        return p -> Modifier.isStatic(p.getModifiers()) && Modifier.isFinal(p.getModifiers());
    }

    private static void createMapDefaultValues() {
        DEFAULT_VALUES.put(boolean.class, Boolean.FALSE);
        DEFAULT_VALUES.put(byte.class, (byte) 0);
        DEFAULT_VALUES.put(short.class, (short) 0);
        DEFAULT_VALUES.put(int.class, 0);
        DEFAULT_VALUES.put(long.class, 0L);
        DEFAULT_VALUES.put(char.class, '\0');
        DEFAULT_VALUES.put(float.class, 0.0F);
        DEFAULT_VALUES.put(double.class, 0.0D);
    }

    @SuppressWarnings("unchecked")
    private static <T> T defaultValueFor(Class<T> clazz) {
        return (T) DEFAULT_VALUES.get(clazz);
    }
}