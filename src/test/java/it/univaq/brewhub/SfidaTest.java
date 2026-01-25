package it.univaq.brewhub;

import it.univaq.brewhub.dao.impl.SfidaDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.model.Sfida;
import org.junit.jupiter.api.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SfidaTest {

    private static final String TEST_DB_PATH = "brewhub_test_sfide.db";
    private static SfidaDAOImpl sfidaDAO;
    private static UtenteDAOImpl utenteDAO;

    @BeforeAll
    public static void setUp() throws SQLException {
        // Setup database di test
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        DatabaseManager.configureTestDatabase(TEST_DB_PATH);
        DatabaseManager.init();

        sfidaDAO = new SfidaDAOImpl();
        utenteDAO = new UtenteDAOImpl();
    }

    @Test
    @Order(1)
    public void testCreateSfida() throws SQLException {
        // Crea Torrefattore
        Torrefattore t = new Torrefattore("Torre", "Master", "master_roaster", "pass", null, "111", "Via Coffee",
                "Desc", "Master Roasters inc.");
        try {
            utenteDAO.create(t);
        } catch (SQLException e) {
            // Già esiste
        }

        Sfida s = new Sfida("Miglior Espresso", "Crea il miglior espresso", "1kg Caffè", "2026-12-31",
                "master_roaster");
        sfidaDAO.create(s);

        assertNotNull(s.getId());
        assertTrue(s.getId() > 0);

        Sfida retrieved = sfidaDAO.findById(s.getId());
        assertNotNull(retrieved);
        assertEquals("Miglior Espresso", retrieved.getTitolo());
        assertEquals("master_roaster", retrieved.getCreatore());
    }

    @Test
    @Order(2)
    public void testParticipateSfida() throws SQLException {
        // Crea Utente partecipante
        Utente u = new Utente("Challenger", "User", "challenger_user", "pass", Utente.TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(u);
        } catch (SQLException e) {
            // Già esiste
        }

        // Recupera sfida
        List<Sfida> sfide = sfidaDAO.findAll();
        assertFalse(sfide.isEmpty());
        Sfida target = sfide.get(0);

        // Partecipa
        sfidaDAO.addPartecipante(target.getId(), "challenger_user");

        // Verifica
        boolean isParticipant = sfidaDAO.isPartecipante(target.getId(), "challenger_user");
        assertTrue(isParticipant);

        int count = sfidaDAO.getPartecipantiCount(target.getId());
        assertTrue(count >= 1);
    }

    @Test
    @Order(3)
    public void testRemoveParticipation() throws SQLException {
        List<Sfida> sfide = sfidaDAO.findAll();
        Sfida target = sfide.get(0);

        sfidaDAO.removePartecipante(target.getId(), "challenger_user");

        boolean isParticipant = sfidaDAO.isPartecipante(target.getId(), "challenger_user");
        assertFalse(isParticipant);
    }

    @AfterAll
    public static void tearDown() {
        try {
            System.gc(); // Help release file locks on Windows
            Thread.sleep(100);
            File dbFile = new File(TEST_DB_PATH);
            if (dbFile.exists()) {
                dbFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
