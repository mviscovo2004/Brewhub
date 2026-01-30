package it.univaq.brewhub.model;

import it.univaq.brewhub.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test unitari per la classe {@link Notifica} e il suo DAO.
 * Verifica i costruttori, le operazioni CRUD e le notifiche automatiche
 * generate da varie azioni (messaggi, follow, eventi, sfide, recensioni,
 * gruppi).
 */
public class NotificaTest extends BaseTest {

    /**
     * Configurazione iniziale per ogni test.
     * Crea due utenti di test (userA e userB).
     * 
     * @throws SQLException se si verifica un errore durante la creazione degli
     *                      utenti.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        createTestUser("userA");
        createTestUser("userB");
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
     * Verifica i costruttori e i metodi getter/setter della classe Notifica.
     */
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

    /**
     * Verifica le operazioni CRUD sul database per le notifiche.
     * Testa creazione, recupero, conteggio non lette, segna come letto,
     * eliminazione singola e eliminazione multipla.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testMetodiDB() throws SQLException {
        String username = "notifUser_" + System.currentTimeMillis();
        createTestUser(username);
        Utente u = utenteDAO.findByUsername(username);

        Notifica n = new Notifica(u, "Benvenuto!");
        notificaDAO.create(n);
        assertTrue(n.getId() > 0, "ID notifica deve essere valorizzato dopo create");

        List<Notifica> list = notificaDAO.findByUser(username);
        assertFalse(list.isEmpty());
        boolean found = list.stream()
                .anyMatch(notif -> notif.getId() == n.getId() && notif.getMessaggio().equals("Benvenuto!"));
        assertTrue(found);

        int count = notificaDAO.getUnreadCount(username);
        assertTrue(count > 0);

        notificaDAO.markAsRead(n.getId());
        list = notificaDAO.findByUser(username);
        Notifica readNotif = list.stream().filter(x -> x.getId() == n.getId()).findFirst().orElse(null);
        assertNotNull(readNotif);
        assertTrue(readNotif.isLetto());

        count = notificaDAO.getUnreadCount(username);
        assertEquals(0, count);

        notificaDAO.delete(n.getId());
        list = notificaDAO.findByUser(username);
        assertFalse(list.stream().anyMatch(x -> x.getId() == n.getId()));

        notificaDAO.create(new Notifica(u, "One"));
        notificaDAO.create(new Notifica(u, "Two"));
        notificaDAO.deleteAll(username);
        list = notificaDAO.findByUser(username);
        assertTrue(list.isEmpty(), "deleteAll dovrebbe rimuovere tutte le notifiche dell'utente");
    }

    /**
     * Verifica le notifiche per i nuovi messaggi.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testMessaggioNotification() throws SQLException {
        Messaggio m = new Messaggio();
        m.setSender("userA");
        m.setReceiver("userB");
        m.setContenuto("Ciao!");
        m.setTimestamp("2023-10-27T10:00:00");
        m.setLetto(false);
        messaggioDAO.create(m);

        List<Notifica> nList = notificaDAO.findAllUnread("userB");
        assertEquals(1, nList.size(), "UserB should have 1 notification");
        assertTrue(nList.get(0).getMessaggio().contains("Nuovo messaggio da userA"), "Notification text mismatch");
    }

    /**
     * Verifica le notifiche per i nuovi follower.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testFollowNotification() throws SQLException {
        utenteDAO.follow("userA", "userB");
        List<Notifica> nList = notificaDAO.findAllUnread("userB");
        assertEquals(1, nList.size(), "UserB should have 1 notification");
        assertTrue(nList.get(0).getMessaggio().contains("userA ha iniziato a seguirti"), "Notification text mismatch");
    }

    /**
     * Verifica le notifiche per la partecipazione agli eventi.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testEventoNotification() throws SQLException {
        Evento e = new Evento();
        e.setNome("BeerFest");
        e.setDescrizione("Fun");
        e.setData("2023-12-01");
        e.setLuogo("Pub");
        e.setOrganizzatore("userB");
        eventoDAO.create(e);

        List<Evento> all = eventoDAO.findAll();
        assertFalse(all.isEmpty());
        int eId = all.get(0).getId();

        eventoDAO.addPartecipante(eId, "userA");
        List<Notifica> nList = notificaDAO.findAllUnread("userB");
        assertEquals(1, nList.size(), "organizer should be notified");
        assertTrue(nList.get(0).getMessaggio().contains("userA si è iscritto al tuo evento"),
                "Notification text mismatch");
    }

    /**
     * Verifica le notifiche per l'accettazione delle sfide.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testSfidaNotification() throws SQLException {
        Sfida s = new Sfida();
        s.setTitolo("Challenge1");
        s.setDescrizione("Desc");
        s.setPremio("Prize");
        s.setScadenza("2023-12-01");
        s.setCreatore("userB");
        sfidaDAO.create(s);
        List<Sfida> all = sfidaDAO.findAll();
        int sId = all.get(0).getId();

        sfidaDAO.addPartecipante(sId, "userA");
        List<Notifica> nList = notificaDAO.findAllUnread("userB");
        assertEquals(1, nList.size(), "creator should be notified");
        assertTrue(nList.get(0).getMessaggio().contains("userA ha accettato la tua sfida"),
                "Notification text mismatch");
    }

    /**
     * Verifica le notifiche per le nuove recensioni.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testRecensioneNotification() throws SQLException {
        Utente creator = utenteDAO.findByUsername("userB");
        Post p = new Post("My Brew", "Content", creator, Post.TipoPost.TESTO, null);
        postDAO.create(p);
        List<Post> posts = postDAO.findAll();
        p = posts.get(0);

        Utente reviewer = utenteDAO.findByUsername("userA");
        Recensione r = new Recensione(p, reviewer, 5, "Wow", "2023-10-27");
        recensioneDAO.create(r);

        List<Notifica> nList = notificaDAO.findAllUnread("userB");
        assertEquals(1, nList.size(), "author should be notified");
        assertTrue(nList.get(0).getMessaggio().contains("userA ha recensito"), "Notification text mismatch");
    }

    /**
     * Verifica le notifiche per l'aggiunta a un gruppo.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testGruppoNotification() throws SQLException {
        gruppoDAO.createGruppo("BrewGroup", "userA", List.of("userB"));
        List<Notifica> nList = notificaDAO.findAllUnread("userB");
        assertEquals(1, nList.size(), "UserB should be notified of group add");
        assertTrue(nList.get(0).getMessaggio().contains("Sei stato aggiunto al gruppo"), "Notification text mismatch");
    }

    /**
     * Verifica che segnare come letta una notifica inesistente non sollevi
     * eccezioni.
     */
    @Test
    public void testMarkAsReadNonExistent() {
        assertDoesNotThrow(() -> notificaDAO.markAsRead(999999));
    }

    /**
     * Verifica che cancellare tutte le notifiche di un utente senza notifiche non
     * sollevi eccezioni.
     */
    @Test
    public void testDeleteAllNoNotifications() {
        assertDoesNotThrow(() -> notificaDAO.deleteAll("userA"));
    }

    /**
     * Verifica che il conteggio delle notifiche non lette per un utente inesistente
     * sia zero.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testGetUnreadCountNonExistentUser() throws SQLException {
        int count = notificaDAO.getUnreadCount("nonExistentUser");
        assertEquals(0, count);
    }
}
