package it.univaq.brewhub.dao;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Torrefattore;
import it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class TorrefattoreTest {

    private static TorrefattoreDAO torrefattoreDAO;

    @BeforeAll
    static void setup() {
        // Inizializza DB (crea tabelle se non esistono)
        DatabaseManager.init();
        torrefattoreDAO = new TorrefattoreDAOImpl();
    }

    @Test
    void testCreateAndRetrieveTorrefattore() throws SQLException {
        String username = "test_torrefattore_" + System.currentTimeMillis();

        Torrefattore t = new Torrefattore();
        t.setUsername(username);
        t.setNome("Luigi");
        t.setCognome("Verdi");
        t.setPassword("password123");
        t.setPartitaIva("IT12345678901");
        t.setIndirizzo("Via del Caffè, 10");
        t.setDescrizione("Torrefazione artigianale di alta qualità");
        t.setNomeAzienda("Caffè Molinari");
        t.setFotoProfilo("default_avatar.png");

        // 1. Create
        torrefattoreDAO.create(t);

        // 2. Retrieve
        Torrefattore retrieved = torrefattoreDAO.findByUsername(username);

        assertNotNull(retrieved, "Il torrefattore dovrebbe essere stato trovato");
        assertEquals(username, retrieved.getUsername());
        assertEquals("Luigi", retrieved.getNome());
        assertEquals("IT12345678901", retrieved.getPartitaIva());
        assertEquals("Via del Caffè, 10", retrieved.getIndirizzo());
        assertEquals("Torrefazione artigianale di alta qualità", retrieved.getDescrizione());
        assertEquals("Caffè Molinari", retrieved.getNomeAzienda());

        // 3. Verify Polymorphism
        assertTrue(retrieved instanceof it.univaq.brewhub.Utente);
        assertEquals(it.univaq.brewhub.Utente.TipoUtente.TORREFATTORE, retrieved.getTipo());
    }
}
