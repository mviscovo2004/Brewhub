package it.univaq.brewhub.model;

import it.univaq.brewhub.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.model.Utente.TipoUtente;

/**
 * Test unitari e di integrazione per la classe {@link Torrefattore}.
 * Verifica i costruttori, i getter/setter specifici di Torrefattore e le
 * operazioni CRUD tramite il DAO.
 */
public class TorrefattoreTest extends BaseTest {

    /**
     * Verifica il costruttore vuoto di Torrefattore.
     * Il tipo utente deve essere automaticamente impostato a TORREFATTORE.
     */
    @Test
    public void testCostruttoreVuoto() {
        Torrefattore t = new Torrefattore();
        assertNotNull(t);
        assertEquals(TipoUtente.TORREFATTORE, t.getTipo());
    }

    /**
     * Verifica il costruttore completo di Torrefattore.
     * Tutti i campi devono essere correttamente inizializzati.
     */
    @Test
    public void testCostruttoreCompleto() {
        Torrefattore t = new Torrefattore(
                "Mario",
                "Rossi",
                "mariorossi",
                "password123",
                "foto.jpg",
                "12345678901",
                "Via Roma 1, Milano",
                "Torrefazione artigianale dal 1950",
                "Caffè Rossi SRL");

        assertEquals("Mario", t.getNome());
        assertEquals("Rossi", t.getCognome());
        assertEquals("mariorossi", t.getUsername());
        assertEquals(TipoUtente.TORREFATTORE, t.getTipo());
        assertEquals("12345678901", t.getPartitaIva());
        assertEquals("Via Roma 1, Milano", t.getIndirizzo());
        assertEquals("Torrefazione artigianale dal 1950", t.getDescrizione());
        assertEquals("Caffè Rossi SRL", t.getNomeAzienda());
    }

    /**
     * Verifica i getter e setter per la Partita IVA.
     */
    @Test
    public void testPartitaIvaGetterSetter() {
        Torrefattore t = new Torrefattore();
        t.setPartitaIva("98765432109");
        assertEquals("98765432109", t.getPartitaIva());
    }

    /**
     * Verifica i getter e setter per l'indirizzo.
     */
    @Test
    public void testIndirizzoGetterSetter() {
        Torrefattore t = new Torrefattore();
        t.setIndirizzo("Via Verdi 10, Roma");
        assertEquals("Via Verdi 10, Roma", t.getIndirizzo());
    }

    /**
     * Verifica i getter e setter per la descrizione.
     */
    @Test
    public void testDescrizioneGetterSetter() {
        Torrefattore t = new Torrefattore();
        t.setDescrizione("Specialisti in caffè biologico");
        assertEquals("Specialisti in caffè biologico", t.getDescrizione());
    }

    /**
     * Verifica i getter e setter per il nome azienda.
     */
    @Test
    public void testNomeAziendaGetterSetter() {
        Torrefattore t = new Torrefattore();
        t.setNomeAzienda("Bio Coffee");
        assertEquals("Bio Coffee", t.getNomeAzienda());
    }

    /**
     * Verifica la creazione di un Torrefattore nel database.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testCreazioneTorrefattore() throws SQLException {
        String username = "torreTest_" + System.currentTimeMillis();
        Torrefattore t = new Torrefattore(
                "Giuseppe",
                "Bianchi",
                username,
                "password",
                null,
                "11223344556",
                "Via Milano 5",
                "Caffè di qualità",
                "Bianchi Coffee");

        torrefattoreDAO.create(t);

        Torrefattore retrieved = (Torrefattore) torrefattoreDAO.findByUsername(username);
        assertNotNull(retrieved);

        assertEquals("Giuseppe", retrieved.getNome());
        assertEquals("Bianchi", retrieved.getCognome());
        assertEquals(username, retrieved.getUsername());
        assertEquals(TipoUtente.TORREFATTORE, retrieved.getTipo());
        assertEquals("11223344556", retrieved.getPartitaIva());
        assertEquals("Via Milano 5", retrieved.getIndirizzo());
        assertEquals("Caffè di qualità", retrieved.getDescrizione());
        assertEquals("Bianchi Coffee", retrieved.getNomeAzienda());
    }

    /**
     * Verifica l'aggiornamento dei dati di un Torrefattore.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testAggiornamentoTorrefattore() throws SQLException {
        String username = "torreUpdate_" + System.currentTimeMillis();
        Torrefattore t = createTestTorrefattore(username);

        t.setNomeAzienda("Nuova Azienda");
        t.setPartitaIva("99999999999");
        t.setIndirizzo("Nuovo Indirizzo");
        t.setDescrizione("Nuova Descrizione");

        torrefattoreDAO.update(t);

        Torrefattore retrieved = (Torrefattore) torrefattoreDAO.findByUsername(username);
        assertEquals("Nuova Azienda", retrieved.getNomeAzienda());
        assertEquals("99999999999", retrieved.getPartitaIva());
        assertEquals("Nuovo Indirizzo", retrieved.getIndirizzo());
        assertEquals("Nuova Descrizione", retrieved.getDescrizione());
    }

    /**
     * Verifica l'eliminazione di un Torrefattore dal database.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database.
     */
    @Test
    public void testEliminazioneTorrefattore() throws SQLException {
        String username = "torreDelete_" + System.currentTimeMillis();
        createTestTorrefattore(username);

        torrefattoreDAO.delete(username);

        Utente retrieved = utenteDAO.findByUsername(username);
        assertNull(retrieved);
    }

    /**
     * Verifica che un Torrefattore erediti correttamente i campi da Utente.
     */
    @Test
    public void testEreditarietaDaUtente() {
        Torrefattore t = new Torrefattore();

        // Testa campi ereditati da Utente
        t.setNome("Test");
        t.setCognome("Torrefattore");
        t.setUsername("testtorre");

        assertEquals("Test", t.getNome());
        assertEquals("Torrefattore", t.getCognome());
        assertEquals("testtorre", t.getUsername());
        assertEquals(TipoUtente.TORREFATTORE, t.getTipo());
    }
}
