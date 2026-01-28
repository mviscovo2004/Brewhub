package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Commento;
import java.util.List;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione dei Commenti.
 */
public interface CommentoDAO {

    /**
     * Crea un nuovo commento.
     * @param commento Il commento da persistere.
     * @throws SQLException Errore SQL.
     */
    void create(Commento commento) throws SQLException;

    /**
     * Trova tutti i commenti associati a un post specifico.
     * @param postId L'ID del post.
     * @return Lista di commenti.
     * @throws SQLException Errore SQL.
     */
    List<Commento> findByPostId(int postId) throws SQLException;

    /**
     * Elimina un commento per ID.
     * @param id L'ID del commento.
     * @throws SQLException Errore SQL.
     */
    void delete(int id) throws SQLException;

    /**
     * Aggiorna il contenuto di un commento.
     * @param commento Il commento con i dati aggiornati.
     * @throws SQLException Errore SQL.
     */
    void update(Commento commento) throws SQLException;
}
