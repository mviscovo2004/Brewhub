package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Torrefattore;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione specifica dei Torrefattori.
 */
public interface TorrefattoreDAO {

    /**
     * Crea un nuovo torrefattore.
     * @param torrefattore Dati del torrefattore.
     * @throws SQLException Errore SQL.
     */
    void create(Torrefattore torrefattore) throws SQLException;

    /**
     * Cerca un torrefattore per username.
     * @param username Username.
     * @return Torrefattore trovato o null.
     * @throws SQLException Errore SQL.
     */
    Torrefattore findByUsername(String username) throws SQLException;

    /**
     * Aggiorna i dati di un torrefattore.
     * @param torrefattore Dati aggiornati.
     * @throws SQLException Errore SQL.
     */
    void update(Torrefattore torrefattore) throws SQLException;

    /**
     * Elimina un torrefattore.
     * @param username Username.
     * @throws SQLException Errore SQL.
     */
    void delete(String username) throws SQLException;
}
