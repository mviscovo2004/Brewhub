package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.dao.impl.MessaggioDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.dao.impl.GruppoDAOImpl;
import it.univaq.brewhub.Utente.TipoUtente;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class MessaggioTest {

    private static final String TEST_DB_PATH = "brewhub_test_messaggi.db";

    @BeforeAll
    public static void setUp() throws SQLException {
        java.io.File dbFile = new java.io.File(TEST_DB_PATH);
        if (dbFile.exists())
            dbFile.delete();
        DatabaseManager.configureTestDatabase(TEST_DB_PATH);
        DatabaseManager.init();
    }

    @AfterAll
    public static void tearDown() {
        try {
            System.gc();
            Thread.sleep(100);
            java.io.File dbFile = new java.io.File(TEST_DB_PATH);
            if (dbFile.exists())
                dbFile.delete();
        } catch (Exception e) {
        }
    }

    @Test
    public void testCostruttoriGetterSetter() {
        Messaggio m = new Messaggio();
        m.setId(1);
        m.setSender("u1");
        m.setReceiver("u2");
        m.setContenuto("Ciao");
        m.setTimestamp("2023-01-01T10:00:00");
        m.setLetto(true);
        m.setIdGruppo(5);

        assertEquals(1, m.getId());
        assertEquals("u1", m.getSender());
        assertEquals("u2", m.getReceiver());
        assertEquals("Ciao", m.getContenuto());
        assertEquals("2023-01-01T10:00:00", m.getTimestamp());
        assertTrue(m.isLetto());
        assertEquals(5, m.getIdGruppo());

        Messaggio m2 = new Messaggio("s", "r", "txt", "time");
        assertEquals("s", m2.getSender());
        assertFalse(m2.isLetto());
        assertNull(m2.getIdGruppo());
    }

    @Test
    public void testMetodiDB() throws SQLException {
        MessaggioDAOImpl msgDAO = new MessaggioDAOImpl();
        UtenteDAOImpl userDAO = new UtenteDAOImpl();

        long ts = System.currentTimeMillis();
        String u1 = "msgU1_" + ts;
        String u2 = "msgU2_" + ts;

        // Setup Users
        userDAO.create(new Utente("U1", "U1", u1, "pwd", TipoUtente.APPASSIONATO, null));
        userDAO.create(new Utente("U2", "U2", u2, "pwd", TipoUtente.APPASSIONATO, null));

        // Send Message
        String now = LocalDateTime.now().toString();
        Messaggio m = new Messaggio(u1, u2, "Ciao User 2", now);
        msgDAO.create(m);
        assertTrue(m.getId() > 0, "ID messaggio deve essere generato");

        // Retrieve Conversation
        List<Messaggio> conv = msgDAO.getConversazione(u1, u2);
        assertFalse(conv.isEmpty(), "La conversazione non deve essere vuota");
        boolean found = conv.stream()
                .anyMatch(mess -> mess.getId() == m.getId() && mess.getContenuto().equals("Ciao User 2"));
        assertTrue(found, "Il messaggio inviato deve essere nella conversazione");

        // Unread Count
        int unreadBefore = msgDAO.contaNonLetti(u2);
        assertTrue(unreadBefore > 0, "Dovrebbe esserci almeno 1 messaggio non letto");

        // Mark as Read
        msgDAO.segnaComeLetto(m.getId());

        // Check Read Status
        // Re-fetch conversation to check status
        conv = msgDAO.getConversazione(u1, u2);
        Messaggio readMsg = conv.stream().filter(mess -> mess.getId() == m.getId()).findFirst().orElse(null);
        assertNotNull(readMsg);
        assertTrue(readMsg.isLetto(), "Il messaggio dovrebbe risultare letto");

        // Unread Count should decrease
        int unreadAfter = msgDAO.contaNonLetti(u2);
        assertTrue(unreadAfter < unreadBefore, "Il conteggio messaggi non letti deve scendere");

        // Get Utenti Conversazioni
        List<String> chatPartners = msgDAO.getUtentiConversazioni(u1);
        assertTrue(chatPartners.contains(u2), "User2 deve apparire tra le conversazioni di User1");

        // Test Gruppo Messaggi
        GruppoDAOImpl grpDAO = new GruppoDAOImpl();
        List<String> members = new ArrayList<>();
        members.add(u2);
        int grpId = grpDAO.createGruppo("GMsgTest", u1, members);

        Messaggio gMsg = new Messaggio();
        gMsg.setSender(u1);
        gMsg.setContenuto("Msg Gruppo");
        gMsg.setTimestamp(now);
        gMsg.setIdGruppo(grpId);

        msgDAO.create(gMsg);

        List<Messaggio> gMsgs = msgDAO.getMessaggiGruppo(grpId);
        assertFalse(gMsgs.isEmpty());
        assertEquals("Msg Gruppo", gMsgs.get(0).getContenuto());

        // Cleanup
        userDAO.delete(u1);
        userDAO.delete(u2);
    }
}
