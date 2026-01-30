package it.univaq.brewhub.business;

import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.model.Utente;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link SfidaService}.
 * Verifica la creazione delle sfide e la gestione dei partecipanti.
 */
public class SfidaServiceTest extends BaseTest {

    private SfidaService sfidaService;

    @BeforeEach
    public void setUp() throws Exception {
        sfidaService = SfidaService.getInstance();
        createTestUser("creator");
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
     * Verifica la creazione e il recupero delle sfide.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
    @Test
    public void testCreateAndGetChallenges() throws BusinessException {
        Sfida s = new Sfida();
        s.setTitolo("Challenge1");
        s.setDescrizione("Desc");
        s.setPremio("Prize");
        s.setScadenza("2023-12-01");
        s.setCreatore("creator");

        sfidaService.createChallenge(s);

        List<Sfida> list = sfidaService.getAllChallenges();
        assertFalse(list.isEmpty());
        assertEquals("Challenge1", list.get(0).getTitolo());
    }

    /**
     * Verifica la gestione dei partecipanti alle sfide.
     * 
     * @throws BusinessException se si verifica un errore durante l'operazione.
     */
    @Test
    public void testPartecipazione() throws BusinessException {
        Sfida s = new Sfida();
        s.setTitolo("Challenge1");
        s.setDescrizione("Desc");
        s.setPremio("Prize");
        s.setScadenza("2023-12-01");
        s.setCreatore("creator");
        sfidaService.createChallenge(s);

        List<Sfida> list = sfidaService.getAllChallenges();
        int sfidaId = list.get(0).getId();

        sfidaService.addParticipant(sfidaId, "participant");

        assertTrue(sfidaService.isParticipating(sfidaId, "participant"));
    }
}
