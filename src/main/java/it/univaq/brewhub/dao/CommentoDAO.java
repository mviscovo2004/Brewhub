package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Commento;
import java.util.List;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione delle entità {@link Commento}.
 * Gestisce le operazioni CRUD relative ai commenti sui post.
 */
public interface CommentoDAO {

    /**
     * Crea un nuovo commento nel database.
     *
     * @param commento L'oggetto Commento da salvare.
     * @throws SQLException Se si verifica un errore durante l'inserimento.
     */
    void create(Commento commento) throws SQLException;

    /**
     * Recupera tutti i commenti associati a uno specifico post.
     *
     * @param postId L'identificativo del post.
     * @return Una lista di oggetti Commento associati al post.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Commento> findByPostId(int postId) throws SQLException;

    /**
     * Elimina un commento dal database.
     *
     * @param id L'identificativo del commento da eliminare.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(int id) throws SQLException;

    /**
     * Aggiorna il contenuto di un commento esistente.
     *
     * @param commento L'oggetto Commento con i dati aggiornati.
     * @throws SQLException Se si verifica un errore durante l'aggiornamento.
     */
    void update(Commento commento) throws SQLException;
}
