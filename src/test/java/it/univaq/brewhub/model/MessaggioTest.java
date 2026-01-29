package it.univaq.brewhub.model;

import it.univaq.brewhub.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.model.Utente.TipoUtente;
import java.util.ArrayList;

/**
 * Test unitari per la classe {@link Messaggio} e il suo DAO.
 *
 * Verifica i costruttori, i metodi getter/setter, le operazioni CRUD
 * e le funzionalità di messaggistica privata e di gruppo.
 *
 */
public class MessaggioTest extends BaseTest {
    /**
     * Verifica i costruttori e i metodi getter/setter della classe Messaggio.
     *
     * Testa tutti i costruttori disponibili e i metodi di accesso
     * per messaggi privati e di gruppo.
     *
     */
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

    /**
     * Verifica le operazioni CRUD e le funzionalità di messaggistica.
     *
     * Testa creazione di messaggi, recupero conversazioni, conteggio messaggi non
     * letti,
     * segna come letto e messaggi di gruppo.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testMetodiDB() throws SQLException {
        long ts = System.currentTimeMillis();
        String u1 = "msgU1_" + ts;
        String u2 = "msgU2_" + ts;
        utenteDAO.create(new Utente("U1", "U1", u1, "pwd", TipoUtente.APPASSIONATO, null));
        utenteDAO.create(new Utente("U2", "U2", u2, "pwd", TipoUtente.APPASSIONATO, null));
        String now = LocalDateTime.now().toString();
        Messaggio m = new Messaggio(u1, u2, "Ciao User 2", now);
        messaggioDAO.create(m);
        assertTrue(m.getId() > 0, "ID messaggio deve essere generato");
        List<Messaggio> conv = messaggioDAO.getConversazione(u1, u2);
        assertFalse(conv.isEmpty(), "La conversazione non deve essere vuota");
        boolean found = conv.stream()
                .anyMatch(mess -> mess.getId() == m.getId() && mess.getContenuto().equals("Ciao User 2"));
        assertTrue(found, "Il messaggio inviato deve essere nella conversazione");
        int unreadBefore = messaggioDAO.contaNonLetti(u2);
        assertTrue(unreadBefore > 0, "Dovrebbe esserci almeno 1 messaggio non letto");
        messaggioDAO.segnaComeLetto(m.getId());
        conv = messaggioDAO.getConversazione(u1, u2);
        Messaggio readMsg = conv.stream().filter(mess -> mess.getId() == m.getId()).findFirst().orElse(null);
        assertNotNull(readMsg);
        assertTrue(readMsg.isLetto(), "Il messaggio dovrebbe risultare letto");
        int unreadAfter = messaggioDAO.contaNonLetti(u2);
        assertTrue(unreadAfter < unreadBefore, "Il conteggio messaggi non letti deve scendere");
        List<String> chatPartners = messaggioDAO.getUtentiConversazioni(u1);
        assertTrue(chatPartners.contains(u2), "User2 deve apparire tra le conversazioni di User1");
        List<String> members = new ArrayList<>();
        members.add(u2);
        int grpId = gruppoDAO.createGruppo("GMsgTest", u1, members);
        Messaggio gMsg = new Messaggio();
        gMsg.setSender(u1);
        gMsg.setContenuto("Msg Gruppo");
        gMsg.setTimestamp(now);
        gMsg.setIdGruppo(grpId);
        messaggioDAO.create(gMsg);
        List<Messaggio> gMsgs = messaggioDAO.getMessaggiGruppo(grpId);
        assertFalse(gMsgs.isEmpty());
        assertEquals("Msg Gruppo", gMsgs.get(0).getContenuto());
        gruppoDAO.deleteGruppo(grpId);
        messaggioDAO.deleteConversazione(u1, u2);
        utenteDAO.delete(u1);
        utenteDAO.delete(u2);
    }
}
