package br.com.gregoryfeijon.objectfactoryutil.exception;

import java.util.List;


/**
 * 
 * 08/07/2020
 * 
 * @author gregory.feijon
 * 
 */

public class ObjectFactoryUtilException extends RuntimeException {

	private static final long serialVersionUID = -373125608135393328L;

	public ObjectFactoryUtilException(String message) {
		super(message);
	}

	public ObjectFactoryUtilException(List<String> messages) {
		super(montaMensagemErro(messages));
	}

	public ObjectFactoryUtilException(Throwable ex) {
		super(ex);
	}

	public ObjectFactoryUtilException(String message, Throwable ex) {
		super(message, ex);
	}

	protected static String montaMensagemErro(List<String> errors) {
		StringBuilder sb = new StringBuilder();
		errors.stream().forEach(error -> {
			if (error.contains(":")) {
				sb.append(error).append("\n\n");
			} else {
				sb.append(error).append("\n");
			}
		});
		return sb.toString();
	}
}