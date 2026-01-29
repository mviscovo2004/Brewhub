package it.univaq.brewhub.business;

import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Notifica;
import it.univaq.brewhub.model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link NotificaService}.
 * Verifica la creazione, il recupero, la marcatura come letta e l'eliminazione
 * delle notifiche.
 */
public class NotificaServiceTest extends BaseTest {

    private NotificaService notificaService;

    @BeforeEach
    public void setUp() throws SQLException {
        notificaService = NotificaService.getInstance();
        createTestUser("userA");
    }

    private void createTestUser(String username) throws SQLException {
        try {
            Utente u = new Utente("Test", "User", username, "pass", Utente.TipoUtente.APPASSIONATO, null);
            utenteDAO.create(u);
        } catch (SQLException e) {
            if (!"Username esistente".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    /**
     * Verifica la creazione e il recupero delle notifiche utente.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
    @Test
    public void testCreateAndGetNotifications() throws BusinessException {
        Utente u = new Utente();
        u.setUsername("userA");

        Notifica n = new Notifica(u, "Test Notification");
        notificaService.createNotification(n);

        List<Notifica> list = notificaService.getUserNotifications("userA");
        assertEquals(1, list.size());
        assertEquals("Test Notification", list.get(0).getMessaggio());
    }

    /**
     * Verifica il conteggio delle notifiche non lette.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
    @Test
    public void testGetUnreadCount() throws BusinessException {
        Utente u = new Utente();
        u.setUsername("userA");

        notificaService.createNotification(new Notifica(u, "Msg 1"));
        notificaService.createNotification(new Notifica(u, "Msg 2"));

        int count = notificaService.getUnreadCount("userA");
        assertEquals(2, count);
    }

    /**
     * Verifica la marcatura di una notifica come letta.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
    @Test
    public void testMarkAsRead() throws BusinessException {
        Utente u = new Utente();
        u.setUsername("userA");
        Notifica n = new Notifica(u, "To Read");
        notificaService.createNotification(n);

        // Bisogna recuperare per ottenere l'ID generato
        List<Notifica> list = notificaService.getUserNotifications("userA");
        if (list.isEmpty())
            fail("Notification not created");
        int id = list.get(0).getId();

        notificaService.markAsRead(id);

        assertEquals(0, notificaService.getUnreadCount("userA"));
    }

    /**
     * Verifica l'eliminazione di una notifica.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
    @Test
    public void testDeleteNotification() throws BusinessException {
        Utente u = new Utente();
        u.setUsername("userA");
        notificaService.createNotification(new Notifica(u, "To Delete"));

        List<Notifica> list = notificaService.getUserNotifications("userA");
        int id = list.get(0).getId();

        notificaService.deleteNotification(id);

        list = notificaService.getUserNotifications("userA");
        assertTrue(list.isEmpty());
    }

    /**
     * Verifica l'eliminazione di tutte le notifiche di un utente.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
    @Test
    public void testDeleteAllNotifications() throws BusinessException {
        Utente u = new Utente();
        u.setUsername("userA");
        notificaService.createNotification(new Notifica(u, "1"));
        notificaService.createNotification(new Notifica(u, "2"));

        notificaService.deleteAllNotifications("userA");

        List<Notifica> list = notificaService.getUserNotifications("userA");
        assertTrue(list.isEmpty());
    }

    /**
     * Verifica la gestione delle notifiche non valide.
     */
    @Test
    public void testCreateNotificationInvalid() {
        assertThrows(BusinessException.class, () -> {
            notificaService.createNotification(null);
        });

        assertThrows(BusinessException.class, () -> {
            notificaService.createNotification(new Notifica(new Utente(), null)); // Null message
        });
    }
}
