package it.univaq.brewhub.dao;

import it.univaq.brewhub.Torrefattore;
import java.sql.SQLException;

public interface TorrefattoreDAO {

    /**
     * Crea un nuovo Torrefattore nel sistema.
     * Salva sia i dati utente che i dettagli specifici del torrefattore.
     * 
     * @param torrefattore L'oggetto Torrefattore da salvare.
     * @throws SQLException In caso di errore SQL.
     */
    void create(Torrefattore torrefattore) throws SQLException;

    /**
     * Cerca un Torrefattore tramite username.
     * 
     * @param username Lo username da cercare.
     * @return L'oggetto Torrefattore popolato o null se non trovato.
     * @throws SQLException In caso di errore SQL.
     */
    Torrefattore findByUsername(String username) throws SQLException;

    void delete(String username) throws SQLException;
}
