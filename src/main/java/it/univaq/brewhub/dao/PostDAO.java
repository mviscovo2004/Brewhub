package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Post;
import java.util.List;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione dei {@link Post}.
 * Gestisce la persistenza dei post, le interazioni (like) e le diverse modalità
 * di ricerca e feed.
 */
public interface PostDAO {

    /**
     * Crea un nuovo post nel database.
     *
     * @param post L'oggetto Post da creare.
     * @throws SQLException Se si verifica un errore durante l'inserimento.
     */
    void create(Post post) throws SQLException;

    /**
     * Elimina un post e tutti i dati correlati (commenti, like) dal database.
     *
     * @param id L'identificativo del post.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(int id) throws SQLException;

    /**
     * Recupera tutti i post presenti nel database (feed globale).
     *
     * @return Una lista di post.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Post> findAll() throws SQLException;

    /**
     * Esegue una ricerca testuale sui post.
     * Considera sia il titolo che il contenuto per il match.
     *
     * @param query La stringa di ricerca.
     * @return Una lista di post che contengono la stringa cercata.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    List<Post> search(String query) throws SQLException;

    /**
     * Aggiunge un like a un post da parte di un utente.
     *
     * @param postId   L'identificativo del post.
     * @param username L'username dell'utente.
     * @throws SQLException Se l'utente ha già messo like o si verifica un errore.
     */
    void addLike(int postId, String username) throws SQLException;

    /**
     * Rimuove il like di un utente da un post.
     *
     * @param postId   L'identificativo del post.
     * @param username L'username dell'utente.
     * @throws SQLException Se si verifica un errore durante l'operazione.
     */
    void removeLike(int postId, String username) throws SQLException;

    /**
     * Verifica se un utente ha già messo like a un determinato post.
     *
     * @param postId   L'identificativo del post.
     * @param username L'username dell'utente.
     * @return true se il like è presente, false altrimenti.
     * @throws SQLException Se si verifica un errore durante la verifica.
     */
    boolean isLiked(int postId, String username) throws SQLException;

    /**
     * Conta il numero totale di like ricevuti da un post.
     *
     * @param postId L'identificativo del post.
     * @return Il numero di like.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int getLikesCount(int postId) throws SQLException;

    /**
     * Recupera tutti i post pubblicati da uno specifico autore.
     *
     * @param username L'username dell'autore.
     * @return Una lista di post.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Post> findByAuthor(String username) throws SQLException;

    /**
     * Recupera tutti i post appartenenti a una specifica categoria.
     *
     * @param categoryId L'identificativo della categoria.
     * @return Una lista di post associati alla categoria.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Post> findByCategory(int categoryId) throws SQLException;

    /**
     * Recupera i post pubblicati da una specifica tipologia di utente.
     *
     * @param userType Il tipo di utente (es. "TORREFATTORE", "BARISTA").
     * @return Una lista di post.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Post> findByUserType(String userType) throws SQLException;

    /**
     * Recupera un singolo post tramite il suo ID.
     *
     * @param id L'identificativo del post.
     * @return L'oggetto Post trovato, o null se non esiste.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    Post findById(int id) throws SQLException;

    /**
     * Restituisce una lista dei post più popolari, ordinati per numero di like.
     *
     * @return Una lista di post popolari.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Post> findPopular() throws SQLException;

    /**
     * Restituisce tutti i post a cui un utente ha messo "Mi Piace".
     *
     * @param username L'username dell'utente.
     * @return Una lista di post.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Post> findLikedBy(String username) throws SQLException;

    /**
     * Restituisce tutti i post che un utente ha salvato nel proprio archivio
     * personale.
     *
     * @param username L'username dell'utente.
     * @return Una lista di post salvati.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Post> findSavedBy(String username) throws SQLException;

    /**
     * Genera un feed personalizzato per l'utente, contenente i post degli utenti
     * seguiti.
     *
     * @param username L'username dell'utente corrente.
     * @return Una lista di post (feed personalizzato).
     * @throws SQLException Se si verifica un errore durante la generazione del
     *                      feed.
     */
    List<Post> findFeedForUser(String username) throws SQLException;

    /**
     * Conta il numero totale di post presenti nel sistema.
     *
     * @return Il totale dei post.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int countAll() throws SQLException;

    /**
     * Conta quanti post sono stati pubblicati nelle ultime 24 ore.
     *
     * @return Il numero di post recenti.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int countPostsLast24h() throws SQLException;
}
