package it.univaq.brewhub.model;

import it.univaq.brewhub.BaseTest;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per la classe {@link it.univaq.brewhub.model.Sfida} e il suo
 * DAO.
 *
 * Verifica la creazione di sfide, la gestione delle partecipazioni
 * e la rimozione dei partecipanti.
 *
 */
public class SfidaTest extends BaseTest {
    @Test
    public void testCreateSfida() throws SQLException {
        Torrefattore t = new Torrefattore("Torre", "Master", "master_roaster", "pass", null, "111", "Via Coffee",
                "Desc", "Master Roasters inc.");
        utenteDAO.create(t);
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
    public void testParticipateSfida() throws SQLException {
        Torrefattore t = new Torrefattore("Torre", "Master", "master_roaster", "pass", null, "111", "Via Coffee",
                "Desc", "Master Roasters inc.");
        utenteDAO.create(t);
        Sfida target = new Sfida("Miglior Espresso", "Desc", "Prize", "2026-12-31", "master_roaster");
        sfidaDAO.create(target);
        Utente u = new Utente("Challenger", "User", "challenger_user", "pass", Utente.TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u);
        sfidaDAO.addPartecipante(target.getId(), "challenger_user");
        boolean isParticipant = sfidaDAO.isPartecipante(target.getId(), "challenger_user");
        assertTrue(isParticipant);
        int count = sfidaDAO.getPartecipantiCount(target.getId());
        assertTrue(count >= 1);
    }

    @Test
    public void testRemoveParticipation() throws SQLException {
        Torrefattore t = new Torrefattore("Torre", "Master", "master_roaster", "pass", null, "111", "Via Coffee",
                "Desc", "Master Roasters inc.");
        utenteDAO.create(t);
        Sfida target = new Sfida("Miglior Espresso", "Desc", "Prize", "2026-12-31", "master_roaster");
        sfidaDAO.create(target);
        Utente u = new Utente("Challenger", "User", "challenger_user", "pass", Utente.TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u);
        sfidaDAO.addPartecipante(target.getId(), "challenger_user");
        sfidaDAO.removePartecipante(target.getId(), "challenger_user");
        boolean isParticipant = sfidaDAO.isPartecipante(target.getId(), "challenger_user");
        assertFalse(isParticipant);
    }
}
