package br.com.gregoryfeijon.objectfactoryutilspring.util;

import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 17 de fev de 2020
 *
 * @author gregory.feijon
 */
public final class ReflectionUtil {

    private ReflectionUtil() {}

    /**
     * <strong>Método para obter todos os getters de um objeto.</strong>
     *
     * @param object - {@linkplain Object}
     * @return {@linkplain List}&lt{@linkplain Method}&gt
     */
    public static List<Method> findGetMethods(Object object) {
        return getMethodsAsList(object).stream().filter(method -> method.getName().toLowerCase().startsWith("get")
                || method.getName().toLowerCase().startsWith("is")).collect(Collectors.toList());
    }

    /**
     * <strong>Método para obter todos os setters de um objeto.</strong>
     *
     * @param object - {@linkplain Object}
     * @return {@linkplain List}&lt{@linkplain Method}&gt
     */
    public static List<Method> findSetMethods(Object object) {
        return getMethodsAsList(object).stream().filter(method -> method.getName().toLowerCase().startsWith("set"))
                .collect(Collectors.toList());
    }

    /**
     * <strong>Método para obter todos os getters e setters de um objeto.</strong>
     *
     * @param object - {@linkplain Object}
     * @return {@linkplain Collection}&lt{@linkplain Method}&gt
     */
    public static Collection<Method> getMethodsAsList(Object object) {
        return Arrays.asList(ReflectionUtils.getAllDeclaredMethods(object.getClass()));
    }

    /**
     * <strong>Método para obter todos os campos de um objeto, incluido os
     * provenientes de herança.</strong>
     *
     * @param object - {@linkplain Object}
     * @return {@linkplain List}&lt{@linkplain Field}&gt
     */
    public static Collection<Field> getFieldsAsCollection(Object object) {
        return getFieldsAsCollection(object, true);
    }

    /**
     * <strong> Método para obter os campos de um objeto, incluindo ou não os
     * <p>
     * provenientes de herança.</strong>
     *
     * @param object            {@linkplain Object} - objeto do qual serão obtidos
     *                          <p>
     *                          os atributos
     * @param getFromSuperclass {@linkplain Boolean} - boolean para especificar se
     *                          <p>
     *                          deve ou não verificar as super classes para obter
     *                          <p>
     *                          seus atributos.
     * @return {@linkplain Collection}&lt{@linkplain Field}&gt
     */
    public static Collection<Field> getFieldsAsCollection(Object object, boolean getFromSuperclass) {
        Class<?> clazz = object.getClass();
        Collection<Field> fields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        if (getFromSuperclass) {
            if (clazz.getSuperclass() != null) {
                clazz = clazz.getSuperclass();
                while (clazz != null) {
                    fields.addAll(Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList()));
                    clazz = clazz.getSuperclass();
                }
            }
        }
        return fields;
    }

    /**
     * <strong>Método para fazer a comparação entre objetos de mesmo tipo. Se pelo
     * <p>
     * menos 1 for diferente, retorna false.</strong>
     *
     * @param <T>     - type of objects to compare
     * @param entity1 - &ltT&gt
     * @param entity2 - &ltT&gt
     * @return {@linkplain Boolean}
     * @throws Exception
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2) throws Exception {
        List<Method> getsEntity1 = ReflectionUtil.findGetMethods(entity1);
        List<Method> getsEntity2 = ReflectionUtil.findGetMethods(entity2);
        return compareLists(getsEntity1, getsEntity2, entity1, entity2);
    }

    /**
     * <strong>Método para fazer a comparação entre objetos de mesmo tipo. Se pelo
     * <p>
     * menos 1 for diferente, retorna false. Possui a opção de excluir campos da
     * <p>
     * comparação.</strong>
     *
     * @param <T>         - type of objects to compare
     * @param entity1     - &ltT&gt
     * @param entity2     - &ltT&gt
     * @param filterNames - {@linkplain String}[]
     * @return {@linkplain Boolean}
     * @throws Exception
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2, String[] filterNames) throws Exception {
        if (filterNames == null) {
            return compareObjectsValues(entity1, entity2);
        }
        return compare(entity1, entity2, filterNames, true);
    }

    /**
     * <strong>Método para fazer a comparação entre objetos de mesmo tipo. Se pelo
     * <p>
     * menos 1 for diferente, retorna false. Possui a opção de excluir campos da
     * <p>
     * comparação ou utilizar os campos especificados, excluindo os demais.</strong>
     *
     * @param <T>         - type of objects to compare
     * @param entity1     - &ltT&gt
     * @param entity2     - &ltT&gt
     * @param filterNames - {@linkplain String}[]
     * @param remove      {@linkplain Boolean} - true: exclui campos; false: utiliza
     *                    <p>
     *                    campos
     * @return {@linkplain Boolean}
     * @throws Exception
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2, String[] filterNames, boolean remove)
            throws Exception {
        return compare(entity1, entity2, filterNames, remove);
    }

    /**
     * <strong>Método que busca os getters e filtra, baseado nas configurações
     * <p>
     * estabelecidas.</strong>
     *
     * @param <T>         - type of objects to compare
     * @param entity1     - &ltT&gt
     * @param entity2     - &ltT&gt
     * @param filterNames - {@linkplain String}[]
     * @param remove      {@linkplain Boolean} - true: exclui campos; false: utiliza
     *                    <p>
     *                    campos
     * @return {@linkplain Boolean}
     * @throws Exception
     */
    private static <T> boolean compare(T entity1, T entity2, String[] filterNames, boolean remove) throws Exception {
        List<Method> getsEntity1 = ReflectionUtil.findGetMethods(entity1);
        List<Method> getsEntity2 = ReflectionUtil.findGetMethods(entity2);
        getsEntity1 = filterList(getsEntity1, filterNames, remove);
        getsEntity2 = filterList(getsEntity2, filterNames, remove);
        return compareLists(getsEntity1, getsEntity2, entity1, entity2);
    }

    /**
     * <strong>Método que efetivamente filtra a lista de getters, de acordo com os
     * <p>
     * parâmetros especificados.</strong>
     *
     *
     *
     * <p>
     * <p>
     * Filtra os métodos pelos prefixos get/is, referentes aos getters + cada um dos
     * <p>
     * nomes dos atributos (nomeação devida dos métodos getters, de acordo com as
     * <p>
     * boas práticas). Baseado no valor do {@linkplain Boolean remove}, retorna a
     * <p>
     * lista de todos os métodos sem os especificados, ou uma lista composta somente
     * <p>
     * por eles.
     *
     * <p>
     *
     * @param listMethod  - {@linkplain List}&lt{@linkplain Method}&gt
     * @param filterNames - {@linkplain String}[]
     * @param remove      - {@linkplain Boolean}
     * @return {@linkplain List}&lt{@linkplain Method}&gt
     */
    public static List<Method> filterList(List<Method> listMethod, String[] filterNames, boolean remove) {
        List<Method> methodsFiltered = new LinkedList<>();
        Arrays.stream(filterNames).forEach(name -> {
            Optional<Method> methodRemove = listMethod.stream()
                    .filter(method -> method.getName().toLowerCase().equals("get" + name.toLowerCase())
                            || method.getName().toLowerCase().equals("is" + name.toLowerCase()))
                    .findAny();
            methodRemove.ifPresent(methodsFiltered::add);
        });
        if (ValidationHelpers.collectionNotEmpty(methodsFiltered)) {
            if (remove) {
                listMethod.removeAll(methodsFiltered);
            } else {
                return methodsFiltered;
            }
        }
        return listMethod;
    }

    /**
     * <strong>Método que efetivamente compara as 2 listas de getters e retorna
     * <p>
     * true, caso não encontre nenhum valor diferente, ou false, caso
     * <p>
     * encontre.</strong>
     *
     *
     *
     * <p>
     * <p>
     * Para cada 1 dos métodos da lista 1, encontra o correspondente da lista 2
     * <p>
     * (garantido, pois é necessário que os objetos — dos quais os métodos são
     * <p>
     * extraídos — sejam do mesmo tipo) e obtem seus valores através do método
     * <p>
     * {@linkplain Method#invoke(Object, Object...) invoke}. É feita uma comparação
     * <p>
     * desses valores utilizando o método
     * <p>
     * {@linkplain ObjectUtils#nullSafeEquals(Object, Object) nullSafeEquals} e,
     * <p>
     * caso seja diferente e do tipo {@linkplain String}, uma segunda verificação,
     * <p>
     * pro caso de 1 ser null e a outra vazia, que, logicamente, são caracterizados
     * <p>
     * como valores iguais. Se todos os valores sejam iguais, retorna true, caso
     * <p>
     * contrário, retorna false.
     *
     * <p>
     *
     * @param getsEntity1 - {@linkplain List}&lt{@linkplain Method}&gt
     * @param getsEntity2 - {@linkplain List}&lt{@linkplain Method}&gt
     * @param entity1     - {@linkplain Object}
     * @param entity2     - {@linkplain Object}
     * @return {@linkplain Boolean}
     * @throws Exception
     */
    private static boolean compareLists(List<Method> getsEntity1, List<Method> getsEntity2, Object entity1,
                                        Object entity2) throws Exception {
        boolean retorno = true;
        for (Method methodEntity1 : getsEntity1) {
            Optional<Method> methodEntity2 = getsEntity2.stream()
                    .filter(method -> method.getName().toLowerCase().equals(methodEntity1.getName().toLowerCase()))
                    .findAny();
            if (methodEntity2.isPresent()) {
                Object valorSalvo = methodEntity1.invoke(entity1);
                Object valorUpdate = methodEntity2.get().invoke(entity2);
                if (!ObjectUtils.nullSafeEquals(valorSalvo, valorUpdate)) {
                    if (methodEntity1.getReturnType() == methodEntity2.get().getReturnType()) {
                        retorno = verificaTipoValor(valorSalvo, valorUpdate, methodEntity1.getReturnType());
                    } else {
                        retorno = false;
                    }
                }
            }
        }
        return retorno;
    }

    private static boolean verificaTipoValor(Object valorSalvo, Object valorUpdate, Class<?> returnType) {
        boolean retorno;
        if (returnType.isAssignableFrom(String.class)) {
            retorno = verificaStrings(valorSalvo, valorUpdate);
        } else if (returnType.isAssignableFrom(Integer.class) || returnType.isAssignableFrom(Double.class)) {
            retorno = verificaNumber(valorSalvo, valorUpdate);
        } else if (returnType.isAssignableFrom(Collection.class)) {
            retorno = verificaCollection(valorSalvo, valorUpdate);
        } else {
            retorno = false;
        }
        return retorno;
    }

    private static boolean verificaCollection(Object valorSalvo, Object valorUpdate) {
        boolean retorno = false;
        if (valorSalvo == null && valorUpdate != null) {
            retorno = isCollectionEmpty(valorUpdate);
        } else if (valorUpdate == null && valorSalvo != null) {
            retorno = isCollectionEmpty(valorSalvo);
        }
        return retorno;
    }

    private static boolean isCollectionEmpty(Object valor) {
        return ValidationHelpers.collectionEmpty((Collection<?>) valor);
    }

    /**
     * <strong>Comparação de {@linkplain String strings}, que considera null e vazia
     * <p>
     * como valores iguais.</strong>
     *
     * @param valorSalvo  - {@linkplain Object}
     * @param valorUpdate - {@linkplain Object}
     * @return {@linkplain Boolean}
     */
    private static boolean verificaStrings(Object valorSalvo, Object valorUpdate) {
        boolean retorno = false;
        if (valorSalvo == null && valorUpdate != null) {
            retorno = isValorEmpty(valorUpdate);
        } else if (valorUpdate == null && valorSalvo != null) {
            retorno = isValorEmpty(valorSalvo);
        }
        return retorno;
    }

    /**
     * <strong>Verificação de {@linkplain String string} vazia.</strong>
     *
     * @param valor - {@linkplain Object}
     * @return {@linkplain Boolean}
     */
    private static boolean isValorEmpty(Object valor) {
        return ((String) valor).isEmpty();
    }

    /**
     * <strong>Comparação de {@linkplain Number numeros}, que considera null e 0
     * <p>
     * como valores iguais.</strong>
     *
     * @param valorSalvo  - {@linkplain Object}
     * @param valorUpdate - {@linkplain Object}
     * @return {@linkplain Boolean}
     */
    private static boolean verificaNumber(Object valorSalvo, Object valorUpdate) {
        boolean retorno = false;
        if (valorSalvo == null && valorUpdate != null) {
            retorno = isValorZero(valorUpdate);
        } else if (valorUpdate == null && valorSalvo != null) {
            retorno = isValorZero(valorSalvo);
        }
        return retorno;
    }

    /**
     * <strong>Verificação de {@linkplain Number numeros} com valor 0.</strong>
     *
     * @param valorUpdate - {@linkplain Object}
     * @return {@linkplain Boolean}
     */
    private static boolean isValorZero(Object valorUpdate) {
        BigDecimal aux = BigDecimal.valueOf(((Number) valorUpdate).doubleValue());
        return aux.compareTo(BigDecimal.ZERO) == 0;
    }
}