package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Sfida;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione delle Sfide.
 */
public interface SfidaDAO {

    /**
     * Crea una nuova sfida.
     * @param sfida La sfida da creare.
     * @throws SQLException Errore SQL.
     */
    void create(Sfida sfida) throws SQLException;

    /**
     * Recupera tutte le sfide.
     * @return Lista di sfide.
     * @throws SQLException Errore SQL.
     */
    List<Sfida> findAll() throws SQLException;

    /**
     * Cerca una sfida per ID.
     * @param id ID sfida.
     * @return Sfida trovata o null.
     * @throws SQLException Errore SQL.
     */
    Sfida findById(int id) throws SQLException;

    /**
     * Iscrive un utente a una sfida.
     * @param sfidaId ID sfida.
     * @param username Username partecipante.
     * @throws SQLException Errore SQL.
     */
    void addPartecipante(int sfidaId, String username) throws SQLException;

    /**
     * Rimuove un partecipante dalla sfida.
     * @param sfidaId ID sfida.
     * @param username Username.
     * @throws SQLException Errore SQL.
     */
    void removePartecipante(int sfidaId, String username) throws SQLException;

    /**
     * Verifica partecipazione.
     * @param sfidaId ID sfida.
     * @param username Username.
     * @return true se partecipa.
     * @throws SQLException Errore SQL.
     */
    boolean isPartecipante(int sfidaId, String username) throws SQLException;

    /**
     * Conta partecipanti.
     * @param sfidaId ID sfida.
     * @return Numero partecipanti.
     * @throws SQLException Errore SQL.
     */
    int getPartecipantiCount(int sfidaId) throws SQLException;

    /**
     * Elimina sfida.
     * @param id ID sfida.
     * @throws SQLException Errore SQL.
     */
    void delete(int id) throws SQLException;
}