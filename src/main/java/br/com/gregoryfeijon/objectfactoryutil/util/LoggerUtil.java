package br.com.gregoryfeijon.objectfactoryutil.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 11 de agosto de 2020 
 * <p>
 * <strong>Classe Util criada para facilitar o uso do Logger do pacote
 * {@linkplain java.util.logging}, principalmente quando necessário utilizar
 * múltiplos argumentos para compor a mensagem de log.</strong>
 * </p>
 * @author gregory.feijon
 * 
 */

public final class LoggerUtil {

	private final Logger LOG;

	public static LoggerUtil getLog(Class<?> clazz) {
		return new LoggerUtil(clazz);
	}

	private LoggerUtil(Class<?> clazz) {
		LOG = Logger.getLogger(clazz.getName());
	}

	public void info(String mensagem) {
		LOG.info(mensagem);
	}

	public void info(String mensagem, Object... args) {
		LOG.log(Level.INFO, mensagem, args);
	}

	public void warning(String mensagem) {
		LOG.warning(mensagem);
	}

	public void warning(String mensagem, Object... args) {
		LOG.log(Level.WARNING, mensagem, args);
	}

	public void severe(String mensagem) {
		LOG.severe(mensagem);
	}

	public void severe(String mensagem, Object... args) {
		LOG.log(Level.SEVERE, mensagem, args);
	}

	public void log(Level level, String mensagem) {
		LOG.log(level, mensagem);
	}

	public void log(Level level, String mensagem, Object... args) {
		LOG.log(level, mensagem, args);
	}
}
