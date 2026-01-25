package it.univaq.brewhub;

import it.univaq.brewhub.dao.impl.EventoDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import org.junit.jupiter.api.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventoTest {

    private static final String TEST_DB_PATH = "brewhub_test_events.db";
    private static EventoDAOImpl eventoDAO;
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

        eventoDAO = new EventoDAOImpl();
        utenteDAO = new UtenteDAOImpl();
    }

    @Test
    @Order(1)
    public void testCreateEvent() throws SQLException {
        // Crea Torrefattore
        Torrefattore t = new Torrefattore("Torre", "Test", "torre_test", "pass", null, "123", "Via Roma", "Desc",
                "Azienda Test");
        try {
            utenteDAO.create(t);
        } catch (SQLException e) {
            // Già esiste
        }

        Evento e = new Evento("Degustazione", "Assaggio caffè", "2026-12-25 10:00", "Roma", "torre_test");
        eventoDAO.create(e);

        assertNotNull(e.getId());
        assertTrue(e.getId() > 0);
    }

    @Test
    @Order(2)
    public void testParticipateEvent() throws SQLException {
        // Crea Utente partecipante
        Utente u = new Utente("User", "Part", "user_part", "pass", Utente.TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(u);
        } catch (SQLException e) {
            // Già esiste
        }

        // Recupera evento
        List<Evento> eventi = eventoDAO.findAll();
        assertFalse(eventi.isEmpty());
        Evento target = eventi.stream().filter(ev -> ev.getNome().equals("Degustazione")).findFirst().orElse(null);
        assertNotNull(target);

        // Partecipa
        eventoDAO.addPartecipante(target.getId(), "user_part");

        // Verifica
        boolean isParticipant = eventoDAO.isPartecipante(target.getId(), "user_part");
        assertTrue(isParticipant);

        int count = eventoDAO.getPartecipantiCount(target.getId());
        assertTrue(count >= 1);
    }

    @Test
    @Order(3)
    public void testRemoveParticipation() throws SQLException {
        List<Evento> eventi = eventoDAO.findAll();
        Evento target = eventi.stream().filter(ev -> ev.getNome().equals("Degustazione")).findFirst().orElse(null);
        assertNotNull(target);

        eventoDAO.removePartecipante(target.getId(), "user_part");

        boolean isParticipant = eventoDAO.isPartecipante(target.getId(), "user_part");
        assertFalse(isParticipant);
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        // Chiude eventuali connessioni residue (non gestite esplicitamente qui, ma
        // SQLite potrebbe tenerle)
        // In un vero scenario, dovremmo chiudere pool

        // Cancellazione File DB
        try {
            // Give system time to release locks if any
            System.gc();
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
