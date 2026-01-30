package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Notifica;
import it.univaq.brewhub.dao.NotificaDAO;
import it.univaq.brewhub.dao.impl.NotificaDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer responsabile della gestione delle notifiche.
 * Permette di recuperare, creare, eliminare e aggiornare lo stato di lettura
 * delle notifiche utente.
 * Implementa il pattern Singleton.
 */
public class NotificaService {

    private static NotificaService instance;
    private final NotificaDAO notificaDAO;

    /**
     * Costruttore privato.
     */
    private NotificaService() {
        this.notificaDAO = new NotificaDAOImpl();
    }

    /**
     * Restituisce l'istanza univoca di NotificaService.
     *
     * @return L'istanza singleton.
     */
    public static synchronized NotificaService getInstance() {
        if (instance == null) {
            instance = new NotificaService();
        }
        return instance;
    }

    /**
     * Recupera tutte le notifiche associate a un determinato utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di Notifiche. In caso di errore restituisce una lista
     *         vuota.
     */
    public List<Notifica> getUserNotifications(String username) {
        try {
            return notificaDAO.findByUser(username);
        } catch (SQLException e) {
            Log.error("Errore recupero notifiche per " + username, e);
            return Collections.emptyList();
        }
    }

    /**
     * Calcola il numero di notifiche non ancora lette dall'utente.
     *
     * @param username L'username dell'utente.
     * @return Il totale delle notifiche non lette.
     */
    public int getUnreadCount(String username) {
        try {
            return notificaDAO.getUnreadCount(username);
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio delle notifiche non lette", e);
            return 0;
        }
    }

    /**
     * Contrassegna una specifica notifica come letta.
     *
     * @param notificationId L'ID della notifica.
     * @throws BusinessException Se si verifica un errore durante l'aggiornamento.
     */
    public void markAsRead(int notificationId) throws BusinessException {
        try {
            notificaDAO.markAsRead(notificationId);
        } catch (SQLException e) {
            Log.error("Errore durante la marcatura della notifica come letta", e);
            throw new BusinessException("Impossibile aggiornare la notifica", e);
        }
    }

    /**
     * Elimina una singola notifica.
     *
     * @param notificationId L'ID della notifica da eliminare.
     * @throws BusinessException Se si verifica un errore tecnico.
     */
    public void deleteNotification(int notificationId) throws BusinessException {
        try {
            notificaDAO.delete(notificationId);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione della notifica", e);
            throw new BusinessException("Impossibile eliminare la notifica", e);
        }
    }

    /**
     * Cancella tutte le notifiche di un utente (funzione "Svuota notifiche").
     *
     * @param username L'username dell'utente.
     * @throws BusinessException Se si verifica un errore durante l'operazione.
     */
    public void deleteAllNotifications(String username) throws BusinessException {
        try {
            notificaDAO.deleteAll(username);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione di tutte le notifiche", e);
            throw new BusinessException("Impossibile eliminare le notifiche", e);
        }
    }

    /**
     * Crea e invia una nuova notifica a un utente.
     *
     * @param notifica L'oggetto Notifica da creare.
     * @throws BusinessException Se il messaggio è vuoto o si verifica un errore
     *                           tecnico.
     */
    public void createNotification(Notifica notifica) throws BusinessException {
        if (notifica == null || notifica.getMessaggio() == null || notifica.getMessaggio().isBlank()) {
            throw new BusinessException("Il messaggio della notifica è obbligatorio");
        }
        try {
            notificaDAO.create(notifica);
        } catch (SQLException e) {
            Log.error("Errore durante la creazione della notifica", e);
            throw new BusinessException("Impossibile creare la notifica", e);
        }
    }

    /**
     * Segna tutte le notifiche di un utente come lette.
     *
     * @param username L'username dell'utente.
     * @throws BusinessException Se si verifica un errore tecnico.
     */
    public void markAllAsRead(String username) throws BusinessException {
        try {
            notificaDAO.markAllAsRead(username);
        } catch (SQLException e) {
            Log.error("Errore durante la marcatura di tutte le notifiche come lette", e);
            throw new BusinessException("Impossibile aggiornare le notifiche", e);
        }
    }
}
