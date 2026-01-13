package it.univaq.brewhub.dao;

import it.univaq.brewhub.Post;
import java.util.List;
import java.sql.SQLException;

public interface PostDAO {
    /**
     * Crea un nuovo post nel database.
     *
     * @param post Il post da creare.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void create(Post post) throws SQLException;

    /**
     * Elimina un post tramite il suo ID.
     *
     * @param id L'ID del post da eliminare.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void delete(int id) throws SQLException;

    /**
     * Recupera tutti i post ordinati per data di creazione (decrescente).
     *
     * @return Una lista di tutti i post.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    List<Post> findAll() throws SQLException;

    /**
     * Cerca post che corrispondono alla query nel titolo o nel contenuto.
     *
     * @param query La stringa di ricerca.
     * @return Una lista di post corrispondenti.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    List<Post> search(String query) throws SQLException;

    // Gestione Like
    /**
     * Aggiunge un "mi piace" a un post specifico da parte di un utente.
     *
     * @param postId   L'ID del post.
     * @param username Lo username dell'utente che mette "mi piace".
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void addLike(int postId, String username) throws SQLException;

    /**
     * Rimuove un "mi piace" da un post specifico da parte di un utente.
     *
     * @param postId   L'ID del post.
     * @param username Lo username dell'utente che rimuove il "mi piace".
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void removeLike(int postId, String username) throws SQLException;

    /**
     * Verifica se un utente ha già messo "mi piace" a un post.
     *
     * @param postId   L'ID del post.
     * @param username Lo username da controllare.
     * @return True se l'utente ha messo "mi piace", false altrimenti.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    boolean isLiked(int postId, String username) throws SQLException;

    /**
     * Recupera il numero totale di "mi piace" per un post.
     *
     * @param postId L'ID del post.
     * @return Il numero di "mi piace".
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    int getLikesCount(int postId) throws SQLException;

    /**
     * Recupera tutti i post pubblicati da un determinato autore.
     *
     * @param username Lo username dell'autore.
     * @return Una lista di post.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    List<Post> findByAuthor(String username) throws SQLException;

    List<Post> findByCategory(int categoryId) throws SQLException;

    List<Post> findByUserType(String userType) throws SQLException;

    /**
     * Recupera un post tramite il suo ID.
     * 
     * @param id L'ID del post.
     * @return Il post trovato, o null se non esiste.
     * @throws SQLException
     */
    Post findById(int id) throws SQLException;

    /**
     * Recupera i post ordinati per popolarità (numero di like).
     * 
     * @return Lista di post più popolari.
     * @throws SQLException Errore Database.
     */
    List<Post> findPopular() throws SQLException;

    /**
     * Recupera i post a cui l'utente ha messo "mi piace".
     * 
     * @param username Username dell'utente.
     * @return Lista di post piaciuti.
     * @throws SQLException Errore Database.
     */
    List<Post> findLikedBy(String username) throws SQLException;

    /**
     * Recupera il feed personalizzato per l'utente (solo post di chi segue).
     * 
     * @param username Username dell'utente.
     * @return Lista di post personalizzata.
     * @throws SQLException Errore Database.
     */
    List<Post> findFeedForUser(String username) throws SQLException;
}
