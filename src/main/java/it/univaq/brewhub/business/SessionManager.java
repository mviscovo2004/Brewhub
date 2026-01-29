package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Utente;

/**
 * Gestore della sessione utente.
 * Mantiene il riferimento all'utente attualmente loggato nell'applicazione.
 * Implementa il pattern Singleton per fornire un accesso globale e
 * centralizzato.
 */
public class SessionManager {

    /** Istanza singleton della classe. */
    private static SessionManager instance;

    /** Riferimento all'utente attualmente autenticato. */
    private Utente currentUser;

    /**
     * Costruttore privato.
     */
    private SessionManager() {
    }

    /**
     * Restituisce l'istanza unica di SessionManager.
     * Crea l'istanza se non esiste (lazy initialization).
     *
     * @return L'istanza singleton.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Registra l'utente nella sessione corrente (Login).
     *
     * @param utente L'utente che ha completato l'autenticazione.
     */
    public void login(Utente utente) {
        this.currentUser = utente;
    }

    /**
     * Rimuove l'utente dalla sessione (Logout).
     * Imposta l'utente visualizzato a null.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Recupera l'utente attualmente loggato.
     *
     * @return L'oggetto Utente corrente, o null se nessu utente è loggato.
     */
    public Utente getCurrentUser() {
        return currentUser;
    }

    /**
     * Verifica se esiste una sessione attiva.
     *
     * @return true se c'è un utente loggato, false altrimenti.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
