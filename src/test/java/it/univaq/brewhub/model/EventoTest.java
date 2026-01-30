package it.univaq.brewhub.model;

import it.univaq.brewhub.BaseTest;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per la classe {@link Evento} e il suo DAO.
 * Verifica la creazione di eventi, la gestione delle partecipazioni e la
 * rimozione dei partecipanti.
 */
public class EventoTest extends BaseTest {

    /**
     * Verifica la creazione di un evento.
     * Controlla che un evento creato da un Torrefattore venga salvato correttamente
     * con un ID valido.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testCreateEvent() throws SQLException {
        Torrefattore t = new Torrefattore("Torre", "Test", "torre_test", "pass", null, "123", "Via Roma", "Desc",
                "Azienda Test");
        utenteDAO.create(t);

        Evento e = new Evento("Degustazione", "Assaggio caffè", "2026-12-25 10:00", "Roma", "torre_test");
        eventoDAO.create(e);
        assertNotNull(e.getId());
        assertTrue(e.getId() > 0);
    }

    /**
     * Verifica la partecipazione a un evento.
     * Controlla che un utente possa partecipare a un evento e che il conteggio dei
     * partecipanti sia corretto.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testParticipateEvent() throws SQLException {
        Torrefattore t = new Torrefattore("Torre", "Test", "torre_test", "pass", null, "123", "Via Roma", "Desc",
                "Azienda Test");
        utenteDAO.create(t);

        Evento e = new Evento("Degustazione", "Assaggio caffè", "2026-12-25 10:00", "Roma", "torre_test");
        eventoDAO.create(e);

        Utente u = new Utente("User", "Part", "user_part", "pass", Utente.TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u);

        eventoDAO.addPartecipante(e.getId(), "user_part");
        boolean isParticipant = eventoDAO.isPartecipante(e.getId(), "user_part");
        assertTrue(isParticipant);

        int count = eventoDAO.getPartecipantiCount(e.getId());
        assertTrue(count >= 1);
    }

    /**
     * Verifica la rimozione della partecipazione a un evento.
     * Controlla che un utente possa rimuovere la propria partecipazione e che non
     * risulti più tra i partecipanti.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testRemoveParticipation() throws SQLException {
        Torrefattore t = new Torrefattore("Torre", "Test", "torre_test", "pass", null, "123", "Via Roma", "Desc",
                "Azienda Test");
        utenteDAO.create(t);

        Evento e = new Evento("Degustazione", "Assaggio caffè", "2026-12-25 10:00", "Roma", "torre_test");
        eventoDAO.create(e);

        Utente u = new Utente("User", "Part", "user_part", "pass", Utente.TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u);

        eventoDAO.addPartecipante(e.getId(), "user_part");
        eventoDAO.removePartecipante(e.getId(), "user_part");
        boolean isParticipant = eventoDAO.isPartecipante(e.getId(), "user_part");
        assertFalse(isParticipant);
    }
}
