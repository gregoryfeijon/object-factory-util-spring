package br.com.gregoryfeijon.objectfactoryutil.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 
 * 7 de jan de 2020
 * 
 * 
 * 
 * @author gregory.feijon
 * 
 */

public final class ValidationHelpers {

	private ValidationHelpers() {}

	private static Predicate<Collection<?>> predicateCollection;
	private static Predicate<Object> predicateIsNull;

	public static boolean collectionEmpty(Collection<?> entities) {
		if (predicateCollection == null) {
			criaPredicateCollectionVazia();
		}
		return predicateCollection.test(entities);
	}

	public static boolean collectionNotEmpty(Collection<?> entities) {
		return !collectionEmpty(entities);
	}

	private static void criaPredicateCollectionVazia() {
		predicateCollection = collection -> collection == null || collection.isEmpty();
	}

	public static boolean isNull(Object entity) {
		if (predicateIsNull == null) {
			criaPredicateIsNull();
		}
		return predicateIsNull.test(entity);
	}

	public static boolean isNotNull(Object entity) {
		return !isNull(entity);
	}

	private static void criaPredicateIsNull() {
		predicateIsNull = p -> p == null;
	}

	public static List<String> processaErros(Map<String, Boolean> map) {
		Predicate<Boolean> p = criaPredicateProcessaErros();
		List<String> erros = new LinkedList<>();
		map.forEach((mensagem, v) -> {
			if (p.test(v)) {
				erros.add(mensagem);
			}
		});
		return erros;
	}

	private static Predicate<Boolean> criaPredicateProcessaErros() {
		Predicate<Boolean> predicate = p -> !p;
		return predicate;
	}

	public static List<String> montaListaErro(String mensagemInicial, List<String> erros) {
		List<String> errosAdd = new LinkedList<>(Arrays.asList(mensagemInicial));
		errosAdd.addAll(erros);
		erros.addAll(errosAdd);
		return errosAdd;
	}
}
