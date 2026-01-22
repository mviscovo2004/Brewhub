package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.dao.impl.NotificaDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.Utente.TipoUtente;

public class NotificaTest {

    @Test
    public void testCostruttoriGetterSetter() {
        Utente u = new Utente();
        String msg = "New follower";
        Notifica n = new Notifica(u, msg);

        assertEquals(u, n.getUtente());
        assertEquals(msg, n.getMessaggio());
        assertFalse(n.isLetto());
        assertNotNull(n.getDataCreazione());

        n.setId(10);
        n.setLetto(true);
        LocalDateTime now = LocalDateTime.now();
        n.setDataCreazione(now);

        assertEquals(10, n.getId());
        assertTrue(n.isLetto());
        assertEquals(now, n.getDataCreazione());
    }

    @Test
    public void testMetodiDB() throws SQLException {
        it.univaq.brewhub.DatabaseManager.init();
        NotificaDAOImpl notificaDAO = new NotificaDAOImpl();
        UtenteDAOImpl utenteDAO = new UtenteDAOImpl();

        String username = "notifUser_" + System.currentTimeMillis();
        Utente u = new Utente("N", "N", username, "pwd", TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u);

        // Create Notifica
        Notifica n = new Notifica(u, "Benvenuto!");
        notificaDAO.create(n);
        assertTrue(n.getId() > 0, "ID notifica deve essere valorizzato dopo create");

        // Find By User
        List<Notifica> list = notificaDAO.findByUser(username);
        assertFalse(list.isEmpty());
        // Potrebbe esserci caching o ordine, verifichiamo presenza
        boolean found = list.stream()
                .anyMatch(notif -> notif.getId() == n.getId() && notif.getMessaggio().equals("Benvenuto!"));
        assertTrue(found);

        // Count Unread
        // Nota: Assicurarsi che i test siano isolati o tolleranti.
        // Qui contiamo per questo specifico user appena creato.
        int count = notificaDAO.getUnreadCount(username);
        assertTrue(count > 0);

        // Mark as Read
        notificaDAO.markAsRead(n.getId());

        // Check Read
        list = notificaDAO.findByUser(username);
        Notifica readNotif = list.stream().filter(x -> x.getId() == n.getId()).findFirst().orElse(null);
        assertNotNull(readNotif);
        assertTrue(readNotif.isLetto());

        count = notificaDAO.getUnreadCount(username);
        assertEquals(0, count);

        // Delete
        notificaDAO.delete(n.getId());
        list = notificaDAO.findByUser(username);
        assertFalse(list.stream().anyMatch(x -> x.getId() == n.getId()));

        // Delete All
        notificaDAO.create(new Notifica(u, "One"));
        notificaDAO.create(new Notifica(u, "Two"));
        notificaDAO.deleteAll(username);
        list = notificaDAO.findByUser(username);
        assertTrue(list.isEmpty(), "deleteAll dovrebbe rimuovere tutte le notifiche dell'utente");

        utenteDAO.delete(username);
    }
}
