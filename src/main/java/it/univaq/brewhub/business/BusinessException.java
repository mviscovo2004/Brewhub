package it.univaq.brewhub.business;

/**
 * Eccezione checked per errori di logica di business.
 * <p>
 * Utilizzata per incapsulare eccezioni di basso livello (es. SQLException)
 * e fornire messaggi significativi al livello UI.
 * </p>
 */
public class BusinessException extends Exception {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
