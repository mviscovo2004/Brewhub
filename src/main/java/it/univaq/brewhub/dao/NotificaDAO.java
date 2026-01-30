package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Notifica;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione delle {@link Notifica}.
 * Permette la creazione, lettura e gestione dello stato delle notifiche inviate
 * agli utenti.
 */
public interface NotificaDAO {

    /**
     * Crea una nuova notifica per un utente.
     *
     * @param notifica L'oggetto Notifica da salvare.
     * @throws SQLException Se si verifica un errore durante l'operazione.
     */
    void create(Notifica notifica) throws SQLException;

    /**
     * Recupera tutte le notifiche (lette e non lette) di un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di notifiche, ordinate solitamente per data (dalla più
     *         recente).
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Notifica> findByUser(String username) throws SQLException;

    /**
     * Contrassegna una notifica come letta.
     *
     * @param id L'identificativo della notifica.
     * @throws SQLException Se si verifica un errore durante l'aggiornamento.
     */
    void markAsRead(int id) throws SQLException;

    /**
     * Conta il numero di notifiche non ancora lette da un utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di notifiche non lette.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int getUnreadCount(String username) throws SQLException;

    /**
     * Elimina una singola notifica.
     *
     * @param id L'identificativo della notifica da eliminare.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(int id) throws SQLException;

    /**
     * Elimina tutte le notifiche associate a un utente.
     *
     * @param username L'username dell'utente.
     * @throws SQLException Se si verifica un errore durante l'eliminazione massiva.
     */
    void deleteAll(String username) throws SQLException;

    /**
     * Recupera solo le notifiche non lette di un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di notifiche non lette.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Notifica> findAllUnread(String username) throws SQLException;

    /**
     * Segna tutte le notifiche di un utente come lette in un'unica operazione.
     *
     * @param username L'username dell'utente.
     * @throws SQLException Se si verifica un errore durante l'aggiornamento
     *                      massivo.
     */
    void markAllAsRead(String username) throws SQLException;
}
