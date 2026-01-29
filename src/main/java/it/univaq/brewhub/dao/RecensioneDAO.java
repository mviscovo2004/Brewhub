package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Recensione;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione delle {@link Recensione} sui post.
 * Gestisce la creazione, cancellazione e il calcolo delle valutazioni medie.
 */
public interface RecensioneDAO {

    /**
     * Salva una nuova recensione nel database.
     *
     * @param recensione L'oggetto Recensione da salvare.
     * @throws SQLException Se si verifica un errore durante l'inserimento.
     */
    void create(Recensione recensione) throws SQLException;

    /**
     * Recupera tutte le recensioni associate a un post.
     *
     * @param postId L'identificativo del post.
     * @return Una lista di recensioni.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Recensione> findByPost(int postId) throws SQLException;

    /**
     * Elimina una recensione dal sistema.
     *
     * @param id L'identificativo della recensione.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(int id) throws SQLException;

    /**
     * Calcola la media aritmetica dei voti ricevuti da un post.
     *
     * @param postId L'identificativo del post.
     * @return La media dei voti (da 1 a 5).
     * @throws SQLException Se si verifica un errore durante il calcolo.
     */
    double getAverageRating(int postId) throws SQLException;

    /**
     * Verifica se un utente ha già inviato una recensione per un determinato post.
     *
     * @param postId   L'identificativo del post.
     * @param username L'username dell'utente.
     * @return true se l'utente ha già recensito il post, false altrimenti.
     * @throws SQLException Se si verifica un errore durante la verifica.
     */
    boolean hasUserReviewed(int postId, String username) throws SQLException;
}
