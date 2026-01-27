package it.univaq.brewhub.business;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.UtenteDAO;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.List;

/**
 * Service Layer per la gestione degli Utenti.
 * <p>
 * Agisce come intermediario tra la UI (Controller/View) e il livello di
 * persistenza (DAO).
 * Gestisce la logica di business relativa agli utenti, come login,
 * registrazione e ricerca.
 * Implementa il pattern Singleton.
 * </p>
 */
public class UserService {

    private static UserService instance;
    private final UtenteDAO utenteDAO;

    private UserService() {
        this.utenteDAO = new UtenteDAOImpl();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Tenta il login di un utente.
     * 
     * @param username Username.
     * @param password Password in chiaro.
     * @return L'oggetto Utente se le credenziali sono valide, altrimenti null.
     * @throws BusinessException Se si verifica un errore tecnico.
     */
    public Utente login(String username, String password) throws BusinessException {
        if (username == null || password == null) {
            throw new BusinessException("Username e password non possono essere nulli");
        }
        try {
            return utenteDAO.login(username, password);
        } catch (SQLException e) {
            Log.error("Errore durante il login", e);
            throw new BusinessException("Errore di sistema durante il login", e);
        }
    }

    /**
     * Registra un nuovo utente.
     * 
     * @param utente L'utente da registrare.
     * @throws BusinessException Se l'utente esiste già o errore tecnico.
     */
    public void registerUser(Utente utente) throws BusinessException {
        if (utente == null) {
            throw new BusinessException("L'utente non può essere nullo");
        }
        try {
            utenteDAO.create(utente);
        } catch (SQLException e) {
            if (e.getMessage().contains("Username esistente")) {
                throw new BusinessException("Username già in uso", e);
            }
            Log.error("Errore durante la registrazione", e);
            throw new BusinessException("Errore durante la registrazione", e);
        }
    }

    /**
     * Cerca utenti per username parziale.
     * 
     * @param query Stringa di ricerca.
     * @return Lista di utenti trovati.
     */
    public List<Utente> searchUsers(String query) {
        try {
            return utenteDAO.searchByUsername(query);
        } catch (SQLException e) {
            Log.error("Errore ricerca utenti", e);
            return List.of(); // Ritorna lista vuota per non rompere la UI
        }
    }
}
