package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Utente;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione degli {@link Utente}.
 * Gestisce l'autenticazione, la registrazione, la gestione del profilo e le
 * relazioni sociali (Follow).
 */
public interface UtenteDAO {

    /**
     * Registra un nuovo utente nel database.
     *
     * @param utente L'oggetto Utente da creare.
     * @throws SQLException Se esistono vincoli di unicità violati o errori SQL.
     */
    void create(Utente utente) throws SQLException;

    /**
     * Verifica le credenziali di accesso di un utente.
     *
     * @param username L'username fornito.
     * @param password La password fornita (in chiaro, da confrontare con l'hash).
     * @return L'oggetto Utente autenticato, oppure null se le credenziali sono
     *         errate.
     * @throws SQLException Se si verifica un errore durante il controllo.
     */
    Utente login(String username, String password) throws SQLException;

    /**
     * Aggiorna i dati anagrafici e di profilo di un utente.
     *
     * @param utente L'oggetto Utente con i dati aggiornati.
     * @throws SQLException Se si verifica un errore durante l'aggiornamento.
     */
    void update(Utente utente) throws SQLException;

    /**
     * Elimina un utente dal sistema.
     *
     * @param username L'username dell'utente da eliminare.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(String username) throws SQLException;

    /**
     * Cerca un utente tramite il suo username esatto.
     *
     * @param username L'username da cercare.
     * @return L'oggetto Utente trovato, o null se non esiste.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    Utente findByUsername(String username) throws SQLException;

    /**
     * Crea una relazione "segue" (Follow) tra due utenti.
     *
     * @param follower Username dell'utente che vuole seguire.
     * @param followed Username dell'utente da seguire.
     * @throws SQLException Se la relazione esiste già o si verifica un errore.
     */
    void follow(String follower, String followed) throws SQLException;

    /**
     * Rimuove una relazione "segue" (Unfollow) tra due utenti.
     *
     * @param follower Username dell'utente che smette di seguire.
     * @param followed Username dell'utente che era seguito.
     * @throws SQLException Se si verifica un errore durante l'operazione.
     */
    void unfollow(String follower, String followed) throws SQLException;

    /**
     * Verifica se esiste una relazione di follow tra due utenti.
     *
     * @param follower Username dell'utente follower.
     * @param followed Username dell'utente seguito.
     * @return true se la relazione esiste, false altrimenti.
     * @throws SQLException Se si verifica un errore durante la verifica.
     */
    boolean isFollowing(String follower, String followed) throws SQLException;

    /**
     * Conta il numero di follower di un utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di follower.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int getFollowersCount(String username) throws SQLException;

    /**
     * Conta il numero di persone seguite (following) da un utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di following.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int getFollowingCount(String username) throws SQLException;

    /**
     * Recupera la lista dei follower di un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di oggetti Utente (i follower).
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    java.util.List<Utente> getFollowers(String username) throws SQLException;

    /**
     * Recupera la lista delle persone seguite da un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di oggetti Utente (i seguiti).
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    java.util.List<Utente> getFollowing(String username) throws SQLException;

    /**
     * Aggiunge un post all'archivio dei post salvati dell'utente.
     *
     * @param username L'username dell'utente.
     * @param postId   L'ID del post da salvare.
     * @throws SQLException Se il post è già salvato o si verifica un errore.
     */
    void addToArchive(String username, int postId) throws SQLException;

    /**
     * Rimuove un post dall'archivio dei post salvati.
     *
     * @param username L'username dell'utente.
     * @param postId   L'ID del post da rimuovere.
     * @throws SQLException Se si verifica un errore durante l'operazione.
     */
    void removeFromArchive(String username, int postId) throws SQLException;

    /**
     * Verifica se un post è presente nell'archivio dell'utente.
     *
     * @param username L'username dell'utente.
     * @param postId   L'ID del post.
     * @return true se il post è salvato, false altrimenti.
     * @throws SQLException Se si verifica un errore durante la verifica.
     */
    boolean isArchived(String username, int postId) throws SQLException;

    /**
     * Conta il numero di post salvati nell'archivio di un utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di post salvati.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int getNumSavedPosts(String username) throws SQLException;

    /**
     * Recupera l'elenco completo dei post salvati da un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di Post.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    java.util.List<it.univaq.brewhub.model.Post> getArchive(String username) throws SQLException;

    /**
     * Cerca utenti il cui username contiene parzialmente la stringa fornita.
     *
     * @param partialUsername La stringa parziale da cercare.
     * @return Una lista di utenti corrispondenti.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    java.util.List<Utente> searchByUsername(String partialUsername) throws SQLException;

    /**
     * Restituisce il numero totale di utenti registrati nel sistema.
     *
     * @return Il totale degli utenti.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int countAll() throws SQLException;

    /**
     * Trova gli utenti più attivi (es. basandosi su metriche come post pubblicati o
     * interazioni).
     *
     * @param limit Il numero massimo di utenti da restituire.
     * @return Una lista dei top utenti.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    java.util.List<Utente> findTopActiveUsers(int limit) throws SQLException;
}
