package it.univaq.brewhub.business;

import it.univaq.brewhub.Utente;

/**
 * Gestisce la sessione utente corrente all'interno dell'applicazione.
 * <p>
 * Implementa il pattern Singleton per garantire un unico punto di accesso globale
 * alle informazioni sull'utente attualmente loggato.
 * </p>
 */
public class SessionManager {

    /** L'unica istanza condivisa di SessionManager. */
    private static SessionManager instance;

    /** L'utente attualmente autenticato nel sistema. */
    private Utente currentUser;

    /**
     * Costruttore privato per impedire l'istanziazione esterna (Pattern Singleton).
     */
    private SessionManager() {
    }

    /**
     * Restituisce l'istanza unica del SessionManager.
     * <p>Se l'istanza non esiste, viene creata in modo thread-safe (synchronized).</p>
     * 
     * @return L'istanza di SessionManager.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Effettua il login impostando l'utente corrente nella sessione.
     * 
     * @param utente L'utente che ha effettuato l'accesso.
     */
    public void login(Utente utente) {
        this.currentUser = utente;
    }

    /**
     * Effettua il logout rimuovendo l'utente corrente dalla sessione.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Restituisce l'utente attualmente loggato.
     * 
     * @return L'oggetto {@link Utente} corrente, oppure null se nessuno è loggato.
     */
    public Utente getCurrentUser() {
        return currentUser;
    }

    /**
     * Verifica se c'è un utente loggato.
     * 
     * @return true se un utente è loggato, false altrimenti.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}