package it.univaq.brewhub.business;

import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.model.Utente;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventoServiceTest extends BaseTest {

    private EventoService eventoService;

    @BeforeEach
    public void setUp() throws Exception {
        eventoService = EventoService.getInstance();
        createTestUser("organizer");
        createTestUser("participant");
    }

    private void createTestUser(String username) throws Exception {
        try {
            Utente u = new Utente("Test", "User", username, "pass", Utente.TipoUtente.APPASSIONATO, null);
            utenteDAO.create(u);
        } catch (Exception e) {
            // Ignore if exists
        }
    }

    @Test
    public void testCreateAndGetEvents() throws BusinessException {
        Evento e = new Evento();
        e.setNome("BeerFest");
        e.setDescrizione("Fun");
        e.setData("2023-12-01");
        e.setLuogo("Pub");
        e.setOrganizzatore("organizer");

        eventoService.createEvent(e);

        List<Evento> list = eventoService.getAllEvents();
        assertFalse(list.isEmpty());
        // Verify via stream or ID if possible, but first element check is rudimentary
        // but ok here
        assertEquals("BeerFest", list.get(0).getNome());
    }

    @Test
    public void testPartecipazione() throws BusinessException {
        Evento e = new Evento();
        e.setNome("BeerFest");
        e.setDescrizione("Fun");
        e.setData("2023-12-01");
        e.setLuogo("Pub");
        e.setOrganizzatore("organizer");
        eventoService.createEvent(e);

        List<Evento> list = eventoService.getAllEvents();
        int eventId = list.get(0).getId();

        eventoService.addParticipant(eventId, "participant");

        assertTrue(eventoService.isParticipating(eventId, "participant"));

        eventoService.removeParticipant(eventId, "participant");

        assertFalse(eventoService.isParticipating(eventId, "participant"));
    }
}
