package it.univaq.brewhub.business;

import it.univaq.brewhub.Utente;

/**
 * Singleton per gestire la sessione dell'utente corrente.
 * Sostituisce il passaggio manuale dell'oggetto Utente tra le viste.
 */
public class SessionManager {

    /** Istanza singleton della classe. */
    private static SessionManager instance;
    /** Oggetto utente attualmente loggato (null se nessun utente è loggato). */
    private Utente currentUser;

    /** Costruttore privato per pattern Singleton. */
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
     * Effettua il login di un utente salvandolo nella sessione.
     *
     * @param utente L'utente da loggare.
     */
    public void login(Utente utente) {
        this.currentUser = utente;
    }

    /**
     * Effettua il logout dell'utente corrente rimuovendolo dalla sessione.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Restituisce l'utente attualmente loggato.
     *
     * @return L'utente loggato, oppure null se nessuno è loggato.
     */
    public Utente getCurrentUser() {
        return currentUser;
    }

    /**
     * Verifica se un utente è attualmente loggato.
     *
     * @return True se c'è un utente loggato, false altrimenti.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
