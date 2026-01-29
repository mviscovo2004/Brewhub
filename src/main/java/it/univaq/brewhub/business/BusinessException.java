package it.univaq.brewhub.business;

/**
 * Eccezione checked personalizzata per la gestione degli errori a livello di
 * Business Logic.
 * 
 * Questa classe viene utilizzata per incapsulare eccezioni di livello inferiore
 * (come SQLException)
 * o per segnalare violazioni delle regole di business, permettendo al livello
 * di presentazione (UI)
 * di gestire gli errori in modo uniforme senza dipendere dai dettagli di
 * implementazione del DAO.
 */
public class BusinessException extends Exception {

    /**
     * Costruisce una nuova eccezione di business con il messaggio specificato.
     *
     * @param message Il messaggio che descrive l'errore.
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Costruisce una nuova eccezione di business con il messaggio e la causa
     * specificata.
     * Utile per il chaining delle eccezioni.
     *
     * @param message Il messaggio che descrive l'errore.
     * @param cause   L'eccezione originale che ha causato questo errore.
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
