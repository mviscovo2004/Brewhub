package it.univaq.brewhub.business;

import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Notifica;
import it.univaq.brewhub.model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testGetUnreadCount() throws BusinessException {
        Utente u = new Utente();
        u.setUsername("userA");

        notificaService.createNotification(new Notifica(u, "Msg 1"));
        notificaService.createNotification(new Notifica(u, "Msg 2"));

        int count = notificaService.getUnreadCount("userA");
        assertEquals(2, count);
    }

    @Test
    public void testMarkAsRead() throws BusinessException {
        Utente u = new Utente();
        u.setUsername("userA");
        Notifica n = new Notifica(u, "To Read");
        notificaService.createNotification(n);

        // Need to fetch to get ID
        List<Notifica> list = notificaService.getUserNotifications("userA");
        if (list.isEmpty())
            fail("Notification not created");
        int id = list.get(0).getId();

        notificaService.markAsRead(id);

        assertEquals(0, notificaService.getUnreadCount("userA"));
    }

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
