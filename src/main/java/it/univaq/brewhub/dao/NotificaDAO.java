package it.univaq.brewhub.dao;

import it.univaq.brewhub.Notifica;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione delle Notifiche.
 */
public interface NotificaDAO {

    /**
     * Crea una nuova notifica.
     * @param notifica La notifica da creare.
     * @throws SQLException Errore SQL.
     */
    void create(Notifica notifica) throws SQLException;

    /**
     * Recupera le notifiche di un utente.
     * @param username Username dell'utente.
     * @return Lista di notifiche.
     * @throws SQLException Errore SQL.
     */
    List<Notifica> findByUser(String username) throws SQLException;

    /**
     * Segna una notifica come letta.
     * @param id ID della notifica.
     * @throws SQLException Errore SQL.
     */
    void markAsRead(int id) throws SQLException;

    /**
     * Conta le notifiche non lette.
     * @param username Username dell'utente.
     * @return Numero di notifiche non lette.
     * @throws SQLException Errore SQL.
     */
    int getUnreadCount(String username) throws SQLException;

    /**
     * Elimina una singola notifica.
     * @param id ID della notifica.
     * @throws SQLException Errore SQL.
     */
    void delete(int id) throws SQLException;

    /**
     * Elimina tutte le notifiche di un utente.
     * @param username Username dell'utente.
     * @throws SQLException Errore SQL.
     */
    void deleteAll(String username) throws SQLException;

    /**
     * Recupera tutte le notifiche non lette di un utente.
     * @param username Username.
     * @return Lista di notifiche non lette.
     * @throws SQLException Errore SQL.
     */
    List<Notifica> findAllUnread(String username) throws SQLException;
}