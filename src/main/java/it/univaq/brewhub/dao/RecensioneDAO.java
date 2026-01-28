package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Recensione;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione delle Recensioni.
 */
public interface RecensioneDAO {

    /**
     * Crea una nuova recensione.
     * @param recensione La recensione da salvare.
     * @throws SQLException Errore SQL.
     */
    void create(Recensione recensione) throws SQLException;

    /**
     * Trova tutte le recensioni di un post.
     * @param postId ID del post.
     * @return Lista di recensioni.
     * @throws SQLException Errore SQL.
     */
    List<Recensione> findByPost(int postId) throws SQLException;

    /**
     * Elimina una recensione.
     * @param id ID recensione.
     * @throws SQLException Errore SQL.
     */
    void delete(int id) throws SQLException;

    /**
     * Calcola la media dei voti per un post.
     * @param postId ID post.
     * @return Media voti (0.0 se nessuna recensione).
     * @throws SQLException Errore SQL.
     */
    double getAverageRating(int postId) throws SQLException;

    /**
     * Verifica se un utente ha già recensito un post.
     * @param postId ID post.
     * @param username Username.
     * @return true se ha recensito, false altrimenti.
     * @throws SQLException Errore SQL.
     */
    boolean hasUserReviewed(int postId, String username) throws SQLException;
}
