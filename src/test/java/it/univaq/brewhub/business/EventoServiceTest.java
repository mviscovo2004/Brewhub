package it.univaq.brewhub.business;

import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.model.Utente;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link EventoService}.
 * Verifica la creazione degli eventi e la gestione dei partecipanti.
 */
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
            // Ignora se l'utente esiste già
        }
    }

    /**
     * Verifica la creazione e il recupero degli eventi.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
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
        // Verifica che il primo evento abbia il nome corretto
        assertEquals("BeerFest", list.get(0).getNome());
    }

    /**
     * Verifica la gestione dei partecipanti (aggiunta, verifica, rimozione).
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
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
