package it.univaq.brewhub.model;

import it.univaq.brewhub.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.model.Utente.TipoUtente;

/**
 * Test unitari per la classe {@link Gruppo} e il suo DAO.
 * Verifica i costruttori, i metodi getter/setter e le operazioni CRUD sul
 * database per i gruppi di chat.
 */
public class GruppoTest extends BaseTest {

    /**
     * Verifica i costruttori e i metodi getter/setter della classe Gruppo.
     * Testa tutti i costruttori disponibili e i metodi di accesso.
     */
    @Test
    public void testCostruttoriGetterSetter() {
        Gruppo g = new Gruppo();
        g.setId(10);
        g.setNome("Gruppo Test");
        g.setCreatore("admin");
        List<String> membri = new ArrayList<>();
        membri.add("user1");
        g.setMembri(membri);
        assertEquals(10, g.getId());
        assertEquals("Gruppo Test", g.getNome());
        assertEquals("admin", g.getCreatore());
        assertEquals(membri, g.getMembri());

        Gruppo g2 = new Gruppo(11, "G2", "creator");
        assertEquals(11, g2.getId());
        assertEquals("G2", g2.getNome());
        assertEquals("creator", g2.getCreatore());
    }

    /**
     * Verifica le operazioni CRUD sul database per i gruppi.
     * Testa creazione di gruppi, aggiunta e rimozione di membri, e recupero dei
     * gruppi di un utente.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testMetodiDB() throws SQLException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String creatorUser = "gCreator" + timestamp;
        String memberUser = "gMember" + timestamp;
        Utente c = new Utente("C", "C", creatorUser, "pwd", TipoUtente.APPASSIONATO, null);
        Utente m = new Utente("M", "M", memberUser, "pwd", TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(c);
        } catch (Exception e) {
        }
        try {
            utenteDAO.create(m);
        } catch (Exception e) {
        }

        String groupName = "Test Group " + timestamp;
        List<String> initMembri = new ArrayList<>();
        initMembri.add(memberUser);
        int groupId = gruppoDAO.createGruppo(groupName, creatorUser, initMembri);
        assertTrue(groupId > 0, "L'ID del gruppo deve essere > 0");

        Gruppo retrieved = gruppoDAO.getGruppo(groupId);
        assertNotNull(retrieved);
        assertEquals(groupName, retrieved.getNome());
        assertEquals(creatorUser, retrieved.getCreatore());

        List<Gruppo> gruppiUser = gruppoDAO.getGruppiUtente(memberUser);
        assertFalse(gruppiUser.isEmpty());
        assertTrue(gruppiUser.stream().anyMatch(g -> g.getId() == groupId));

        String member2 = "gMember2" + timestamp;
        Utente m2 = new Utente("M2", "M2", member2, "pwd", TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(m2);
        } catch (Exception e) {
        }

        gruppoDAO.addMembro(groupId, member2);
        List<Gruppo> gruppiUser2 = gruppoDAO.getGruppiUtente(member2);
        assertTrue(gruppiUser2.stream().anyMatch(g -> g.getId() == groupId), "Il nuovo membro deve vedere il gruppo");

        gruppoDAO.removeMembro(groupId, memberUser);
        gruppiUser = gruppoDAO.getGruppiUtente(memberUser);
        assertFalse(gruppiUser.stream().anyMatch(g -> g.getId() == groupId),
                "Il membro rimosso non deve più vedere il gruppo");

        gruppoDAO.deleteGruppo(groupId);
        utenteDAO.delete(creatorUser);
        utenteDAO.delete(memberUser);
        utenteDAO.delete(member2);
    }
}
