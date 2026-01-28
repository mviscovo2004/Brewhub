package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Utente;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione degli Utenti.
 */
public interface UtenteDAO {

    /**
     * Crea un nuovo utente.
     * @param utente Dati utente.
     * @throws SQLException Errore SQL.
     */
    void create(Utente utente) throws SQLException;

    /**
     * Verifica credenziali di accesso.
     * @param username Username.
     * @param password Password in chiaro.
     * @return Utente autenticato o null.
     * @throws SQLException Errore SQL.
     */
    Utente login(String username, String password) throws SQLException;

    /**
     * Aggiorna utente.
     * @param utente Dati aggiornati.
     * @throws SQLException Errore SQL.
     */
    void update(Utente utente) throws SQLException;

    /**
     * Elimina utente.
     * @param username Username.
     * @throws SQLException Errore SQL.
     */
    void delete(String username) throws SQLException;

    /**
     * Cerca utente per username.
     * @param username Username.
     * @return Utente o null.
     * @throws SQLException Errore SQL.
     */
    Utente findByUsername(String username) throws SQLException;

    /**
     * Segue un utente.
     * @param follower Chi segue.
     * @param followed Chi viene seguito.
     * @throws SQLException Errore SQL.
     */
    void follow(String follower, String followed) throws SQLException;

    /**
     * Smette di seguire un utente.
     * @param follower Chi smette di seguire.
     * @param followed Chi era seguito.
     * @throws SQLException Errore SQL.
     */
    void unfollow(String follower, String followed) throws SQLException;

    /**
     * Verifica follow.
     * @param follower Chi segue.
     * @param followed Chi è seguito.
     * @return true se segue.
     * @throws SQLException Errore SQL.
     */
    boolean isFollowing(String follower, String followed) throws SQLException;

    /**
     * Conta follower.
     * @param username Username.
     * @return Numero follower.
     * @throws SQLException Errore SQL.
     */
    int getFollowersCount(String username) throws SQLException;

    /**
     * Conta seguiti (following).
     * @param username Username.
     * @return Numero following.
     * @throws SQLException Errore SQL.
     */
    int getFollowingCount(String username) throws SQLException;

    /**
     * Lista follower.
     * @param username Username.
     * @return Lista utenti follower.
     * @throws SQLException Errore SQL.
     */
    java.util.List<Utente> getFollowers(String username) throws SQLException;

    /**
     * Lista seguiti.
     * @param username Username.
     * @return Lista utenti seguiti.
     * @throws SQLException Errore SQL.
     */
    java.util.List<Utente> getFollowing(String username) throws SQLException;

    /**
     * Salva post in archivio.
     * @param username Username.
     * @param postId ID post.
     * @throws SQLException Errore SQL.
     */
    void addToArchive(String username, int postId) throws SQLException;

    /**
     * Rimuove post da archivio.
     * @param username Username.
     * @param postId ID post.
     * @throws SQLException Errore SQL.
     */
    void removeFromArchive(String username, int postId) throws SQLException;

    /**
     * Verifica archivio.
     * @param username Username.
     * @param postId ID post.
     * @return true se archiviato.
     * @throws SQLException Errore SQL.
     */
    boolean isArchived(String username, int postId) throws SQLException;

    /**
     * Conta post archiviati.
     * @param username Username.
     * @return Numero post.
     * @throws SQLException Errore SQL.
     */
    int getNumSavedPosts(String username) throws SQLException;

    /**
     * Recupera archivio completo.
     * @param username Username.
     * @return Lista post archiviati.
     * @throws SQLException Errore SQL.
     */
    java.util.List<it.univaq.brewhub.model.Post> getArchive(String username) throws SQLException;

    /**
     * Cerca utenti (match parziale).
     * @param partialUsername Parte del nome.
     * @return Lista risultati.
     * @throws SQLException Errore SQL.
     */
    java.util.List<Utente> searchByUsername(String partialUsername) throws SQLException;

    /**
     * Conta totale utenti.
     * @return Totale.
     * @throws SQLException Errore SQL.
     */
    int countAll() throws SQLException;

    /**
     * Trova utenti più attivi.
     * @param limit Limite risultati.
     * @return Lista utenti.
     * @throws SQLException Errore SQL.
     */
    java.util.List<Utente> findTopActiveUsers(int limit) throws SQLException;
}
