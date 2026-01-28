package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Post;
import java.util.List;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione dei Post.
 */
public interface PostDAO {

    /**
     * Crea un nuovo post.
     * @param post Il post da creare.
     * @throws SQLException Errore SQL.
     */
    void create(Post post) throws SQLException;

    /**
     * Elimina un post.
     * @param id ID del post.
     * @throws SQLException Errore SQL.
     */
    void delete(int id) throws SQLException;

    /**
     * Recupera tutti i post.
     * @return Lista di post.
     * @throws SQLException Errore SQL.
     */
    List<Post> findAll() throws SQLException;

    /**
     * Cerca post in base al testo (titolo o contenuto).
     * @param query Stringa di ricerca.
     * @return Lista di post trovati.
     * @throws SQLException Errore SQL.
     */
    List<Post> search(String query) throws SQLException;

    /**
     * Aggiunge un "Mi Piace" a un post.
     * @param postId ID del post.
     * @param username Username dell'utente.
     * @throws SQLException Errore SQL.
     */
    void addLike(int postId, String username) throws SQLException;

    /**
     * Rimuove un "Mi Piace" da un post.
     * @param postId ID del post.
     * @param username Username dell'utente.
     * @throws SQLException Errore SQL.
     */
    void removeLike(int postId, String username) throws SQLException;

    /**
     * Verifica se un utente ha messo "Mi Piace" a un post.
     * @param postId ID post.
     * @param username Username.
     * @return true se like esiste, false altrimenti.
     * @throws SQLException Errore SQL.
     */
    boolean isLiked(int postId, String username) throws SQLException;

    /**
     * Conta i "Mi Piace" di un post.
     * @param postId ID post.
     * @return Numero di like.
     * @throws SQLException Errore SQL.
     */
    int getLikesCount(int postId) throws SQLException;

    /**
     * Trova i post scritti da un autore.
     * @param username Username dell'autore.
     * @return Lista di post.
     * @throws SQLException Errore SQL.
     */
    List<Post> findByAuthor(String username) throws SQLException;

    /**
     * Trova i post appartenenti a una categoria.
     * @param categoryId ID categoria.
     * @return Lista di post.
     * @throws SQLException Errore SQL.
     */
    List<Post> findByCategory(int categoryId) throws SQLException;

    /**
     * Trova i post scritti da utenti di un certo tipo (ruolo).
     * @param userType Tipo utente (es. "BARISTA").
     * @return Lista di post.
     * @throws SQLException Errore SQL.
     */
    List<Post> findByUserType(String userType) throws SQLException;

    /**
     * Cerca un post per ID.
     * @param id ID post.
     * @return Post trovato o null.
     * @throws SQLException Errore SQL.
     */
    Post findById(int id) throws SQLException;

    /**
     * Trova i post più popolari (ordinati per like/commenti).
     * @return Lista post popolari.
     * @throws SQLException Errore SQL.
     */
    List<Post> findPopular() throws SQLException;

    /**
     * Trova i post piaciuti a un utente.
     * @param username Username.
     * @return Lista post.
     * @throws SQLException Errore SQL.
     */
    List<Post> findLikedBy(String username) throws SQLException;

    /**
     * Costruisce il feed personalizzato per un utente (post dei seguiti).
     * @param username Username.
     * @return Feed dei post.
     * @throws SQLException Errore SQL.
     */
    List<Post> findFeedForUser(String username) throws SQLException;

    /**
     * Conta il totale dei post nel sistema.
     * @return Conteggio totale.
     * @throws SQLException Errore SQL.
     */
    int countAll() throws SQLException;

    /**
     * Conta i post creati nelle ultime 24 ore.
     * @return Conteggio recente.
     * @throws SQLException Errore SQL.
     */
    int countPostsLast24h() throws SQLException;
}
