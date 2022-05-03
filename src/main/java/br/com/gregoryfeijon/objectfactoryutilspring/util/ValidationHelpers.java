package br.com.gregoryfeijon.objectfactoryutilspring.util;

import java.util.*;
import java.util.function.Predicate;

/**
 * 
 * 7 de jan de 2020
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
			initializePredicateEmptyCollection();
		}
		return predicateCollection.test(entities);
	}

	public static boolean collectionNotEmpty(Collection<?> entities) {
		return !collectionEmpty(entities);
	}

	private static void initializePredicateEmptyCollection() {
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
		predicateIsNull = Objects::isNull;
	}

	public static List<String> processErrors(Map<String, Boolean> map) {
		Predicate<Boolean> p = processErrorsPredicate();
		List<String> erros = new LinkedList<>();
		map.forEach((mensagem, v) -> {
			if (p.test(v)) {
				erros.add(mensagem);
			}
		});
		return erros;
	}

	private static Predicate<Boolean> processErrorsPredicate() {
		return p -> !p;
	}

	public static List<String> buildErrorsList(String initialMessage, List<String> errors) {
		List<String> errosAdd = new LinkedList<>(Collections.singletonList(initialMessage));
		errosAdd.addAll(errors);
		errors.addAll(errosAdd);
		return errosAdd;
	}
}
