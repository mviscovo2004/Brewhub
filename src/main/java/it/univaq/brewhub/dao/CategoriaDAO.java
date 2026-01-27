package it.univaq.brewhub.dao;

import it.univaq.brewhub.Categoria;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione delle Categorie.
 */
public interface CategoriaDAO {

    /**
     * Crea una nuova categoria.
     * @param c La categoria da creare.
     * @throws SQLException Errore SQL.
     */
    void create(Categoria c) throws SQLException;

    /**
     * Recupera tutte le categorie.
     * @return Lista di categorie.
     * @throws SQLException Errore SQL.
     */
    List<Categoria> findAll() throws SQLException;

    /**
     * Aggiorna una categoria esistente.
     * @param c La categoria aggiornata.
     * @throws SQLException Errore SQL.
     */
    void update(Categoria c) throws SQLException;

    /**
     * Elimina una categoria per ID.
     * @param id L'ID della categoria.
     * @throws SQLException Errore SQL.
     */
    void delete(int id) throws SQLException;

    /**
     * Cerca una categoria per ID.
     * @param id L'ID da cercare.
     * @return La categoria trovata o null.
     * @throws SQLException Errore SQL.
     */
    Categoria findById(int id) throws SQLException;

    /**
     * Cerca una categoria per nome.
     * @param name Il nome da cercare.
     * @return La categoria trovata o null.
     * @throws SQLException Errore SQL.
     */
    Categoria findByName(String name) throws SQLException;
}