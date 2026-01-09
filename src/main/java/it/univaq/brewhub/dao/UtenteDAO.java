package it.univaq.brewhub.dao;

import it.univaq.brewhub.Utente;
import java.sql.SQLException;

public interface UtenteDAO {
    /**
     * Crea un nuovo account utente.
     *
     * @param utente L'oggetto utente da creare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database o
     *                      se lo username esiste già.
     */
    void create(Utente utente) throws SQLException;

    /**
     * Autentica un utente.
     *
     * @param username Lo username.
     * @param password La password in chiaro.
     * @return L'oggetto Utente autenticato, oppure null se l'autenticazione
     *         fallisce.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    Utente login(String username, String password) throws SQLException;

    /**
     * Aggiorna le informazioni del profilo utente.
     *
     * @param utente L'oggetto utente con i campi aggiornati.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void update(Utente utente) throws SQLException;

    /**
     * Elimina un account utente.
     *
     * @param username Lo username dell'account da eliminare.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void delete(String username) throws SQLException;

    /**
     * Cerca un utente tramite il suo username.
     *
     * @param username Lo username da cercare.
     * @return L'oggetto Utente oppure null se non trovato.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    Utente findByUsername(String username) throws SQLException;
}
