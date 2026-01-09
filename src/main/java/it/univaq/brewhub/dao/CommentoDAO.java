package it.univaq.brewhub.dao;

import it.univaq.brewhub.Commento;
import java.util.List;
import java.sql.SQLException;

public interface CommentoDAO {
    /**
     * Crea un nuovo commento.
     *
     * @param commento Il commento da creare.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void create(Commento commento) throws SQLException;

    /**
     * Trova tutti i commenti associati a un post specifico.
     *
     * @param postId L'ID del post.
     * @return Una lista di commenti per il post.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    List<Commento> findByPostId(int postId) throws SQLException;

    /**
     * Elimina un commento tramite il suo ID.
     *
     * @param id L'ID del commento da eliminare.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void delete(int id) throws SQLException;

    /**
     * Aggiorna il contenuto di un commento esistente.
     *
     * @param commento Il commento con il contenuto aggiornato.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    void update(Commento commento) throws SQLException;
}
