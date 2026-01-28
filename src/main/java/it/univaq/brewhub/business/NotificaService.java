package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Notifica;
import it.univaq.brewhub.dao.NotificaDAO;
import it.univaq.brewhub.dao.impl.NotificaDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer per la gestione delle Notifiche.
 * <p>
 * Centralizza la logica relativa alle notifiche utente, inclusa la creazione,
 * lettura e gestione dello stato (letto/non letto).
 * Implementa il pattern Singleton.
 * </p>
 */
public class NotificaService {

    private static NotificaService instance;
    private final NotificaDAO notificaDAO;

    private NotificaService() {
        this.notificaDAO = new NotificaDAOImpl();
    }

    public static synchronized NotificaService getInstance() {
        if (instance == null) {
            instance = new NotificaService();
        }
        return instance;
    }

    /**
     * Recupera tutte le notifiche di un utente.
     * 
     * @param username Username dell'utente.
     * @return Lista di notifiche.
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
     * Conta le notifiche non lette di un utente.
     * 
     * @param username Username dell'utente.
     * @return Numero di notifiche non lette.
     */
    public int getUnreadCount(String username) {
        try {
            return notificaDAO.getUnreadCount(username);
        } catch (SQLException e) {
            Log.error("Errore conteggio notifiche non lette", e);
            return 0;
        }
    }

    /**
     * Marca una notifica come letta.
     * 
     * @param notificationId ID della notifica.
     * @throws BusinessException Se si verifica un errore.
     */
    public void markAsRead(int notificationId) throws BusinessException {
        try {
            notificaDAO.markAsRead(notificationId);
        } catch (SQLException e) {
            Log.error("Errore marcatura notifica come letta", e);
            throw new BusinessException("Impossibile aggiornare la notifica", e);
        }
    }

    /**
     * Elimina una notifica.
     * 
     * @param notificationId ID della notifica.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteNotification(int notificationId) throws BusinessException {
        try {
            notificaDAO.delete(notificationId);
        } catch (SQLException e) {
            Log.error("Errore eliminazione notifica", e);
            throw new BusinessException("Impossibile eliminare la notifica", e);
        }
    }

    /**
     * Elimina tutte le notifiche di un utente.
     * 
     * @param username Username dell'utente.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteAllNotifications(String username) throws BusinessException {
        try {
            notificaDAO.deleteAll(username);
        } catch (SQLException e) {
            Log.error("Errore eliminazione tutte le notifiche", e);
            throw new BusinessException("Impossibile eliminare le notifiche", e);
        }
    }

    /**
     * Crea una nuova notifica.
     * 
     * @param notifica La notifica da creare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void createNotification(Notifica notifica) throws BusinessException {
        if (notifica == null || notifica.getMessaggio() == null || notifica.getMessaggio().isBlank()) {
            throw new BusinessException("Il messaggio della notifica è obbligatorio");
        }
        try {
            notificaDAO.create(notifica);
        } catch (SQLException e) {
            Log.error("Errore creazione notifica", e);
            throw new BusinessException("Impossibile creare la notifica", e);
        }
    }
}
