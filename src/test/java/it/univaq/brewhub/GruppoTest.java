package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.dao.impl.GruppoDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.Utente.TipoUtente;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class GruppoTest {

    private static final String TEST_DB_PATH = "brewhub_test_gruppi.db";

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

    @Test
    public void testMetodiDB() throws SQLException {
        GruppoDAOImpl gruppoDAO = new GruppoDAOImpl();
        UtenteDAOImpl utenteDAO = new UtenteDAOImpl();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String creatorUser = "gCreator" + timestamp;
        String memberUser = "gMember" + timestamp;

        // Setup Users
        Utente c = new Utente("C", "C", creatorUser, "pwd", TipoUtente.APPASSIONATO, null);
        Utente m = new Utente("M", "M", memberUser, "pwd", TipoUtente.APPASSIONATO, null);

        // Ignore create errors if user exists (though timestamp ensures uniqueness
        // mostly)
        try {
            utenteDAO.create(c);
        } catch (Exception e) {
        }
        try {
            utenteDAO.create(m);
        } catch (Exception e) {
        }

        // Create Gruppo
        String groupName = "Test Group " + timestamp;
        List<String> initMembri = new ArrayList<>();
        initMembri.add(memberUser);

        int groupId = gruppoDAO.createGruppo(groupName, creatorUser, initMembri);
        assertTrue(groupId > 0, "L'ID del gruppo deve essere > 0");

        // Get Gruppo
        Gruppo retrieved = gruppoDAO.getGruppo(groupId);
        assertNotNull(retrieved);
        assertEquals(groupName, retrieved.getNome());
        assertEquals(creatorUser, retrieved.getCreatore());

        // Verifica appartenenza membro iniziale
        List<Gruppo> gruppiUser = gruppoDAO.getGruppiUtente(memberUser);
        assertFalse(gruppiUser.isEmpty());
        assertTrue(gruppiUser.stream().anyMatch(g -> g.getId() == groupId));

        // Add Membro
        String member2 = "gMember2" + timestamp;
        Utente m2 = new Utente("M2", "M2", member2, "pwd", TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(m2);
        } catch (Exception e) {
        }

        gruppoDAO.addMembro(groupId, member2);

        List<Gruppo> gruppiUser2 = gruppoDAO.getGruppiUtente(member2);
        assertTrue(gruppiUser2.stream().anyMatch(g -> g.getId() == groupId), "Il nuovo membro deve vedere il gruppo");

        // Remove Membro
        gruppoDAO.removeMembro(groupId, memberUser);
        gruppiUser = gruppoDAO.getGruppiUtente(memberUser);
        assertFalse(gruppiUser.stream().anyMatch(g -> g.getId() == groupId),
                "Il membro rimosso non deve più vedere il gruppo");

        // Cleanup Utenti (Gruppo resta, no delete method)
        utenteDAO.delete(creatorUser);
        utenteDAO.delete(memberUser);
        utenteDAO.delete(member2);
    }
}
