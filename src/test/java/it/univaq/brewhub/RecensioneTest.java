package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.model.Recensione;
import it.univaq.brewhub.Utente.TipoUtente;

/**
 * Test unitari e di integrazione per la classe {@link Recensione}.
 *
 * Verifica i costruttori, i getter/setter e le operazioni CRUD tramite il DAO.
 *
 */
public class RecensioneTest extends BaseTest {

    /**
     * Verifica il costruttore vuoto di Recensione.
     */
    @Test
    public void testCostruttoreVuoto() {
        Recensione r = new Recensione();
        assertNotNull(r);
    }

    /**
     * Verifica il costruttore completo di Recensione.
     */
    @Test
    public void testCostruttoreCompleto() {
        Utente autore = new Utente("Mario", "Rossi", "mario", "pass", TipoUtente.APPASSIONATO, null);
        Post post = new Post("Titolo", "Contenuto", autore, Post.TipoPost.TESTO, null);

        Recensione r = new Recensione(
                post,
                autore,
                5,
                "Ottimo caffè!",
                "2024-01-27");

        assertEquals(post, r.getPost());
        assertEquals(autore, r.getAutore());
        assertEquals(5, r.getVoto());
        assertEquals("Ottimo caffè!", r.getTesto());
        assertEquals("2024-01-27", r.getDataCreazione());
    }

    /**
     * Verifica i getter e setter per l'ID.
     */
    @Test
    public void testIdGetterSetter() {
        Recensione r = new Recensione();
        r.setId(42);
        assertEquals(42, r.getId());
    }

    /**
     * Verifica i getter e setter per il Post.
     */
    @Test
    public void testPostGetterSetter() {
        Recensione r = new Recensione();
        Utente autore = new Utente();
        Post post = new Post("Test", "Content", autore, Post.TipoPost.TESTO, null);

        r.setPost(post);
        assertEquals(post, r.getPost());
    }

    /**
     * Verifica i getter e setter per l'Autore.
     */
    @Test
    public void testAutoreGetterSetter() {
        Recensione r = new Recensione();
        Utente autore = new Utente("Test", "User", "testuser", "pass", TipoUtente.CURIOSO, null);

        r.setAutore(autore);
        assertEquals(autore, r.getAutore());
    }

    /**
     * Verifica i getter e setter per il Voto.
     */
    @Test
    public void testVotoGetterSetter() {
        Recensione r = new Recensione();
        r.setVoto(4);
        assertEquals(4, r.getVoto());
    }

    /**
     * Verifica i getter e setter per il Testo.
     */
    @Test
    public void testTestoGetterSetter() {
        Recensione r = new Recensione();
        r.setTesto("Recensione molto positiva");
        assertEquals("Recensione molto positiva", r.getTesto());
    }

    /**
     * Verifica i getter e setter per la Data di Creazione.
     */
    @Test
    public void testDataCreazioneGetterSetter() {
        Recensione r = new Recensione();
        r.setDataCreazione("2024-12-25");
        assertEquals("2024-12-25", r.getDataCreazione());
    }

    /**
     * Verifica la creazione di una Recensione nel database.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testCreazioneRecensione() throws SQLException {
        String username = "recUser_" + System.currentTimeMillis();
        Utente autore = createTestUser(username, TipoUtente.APPASSIONATO);
        Post post = createTestPost("Post per recensione", autore);

        Recensione r = new Recensione(
                post,
                autore,
                5,
                "Eccellente!",
                "2024-01-27");

        recensioneDAO.create(r);

        assertTrue(r.getId() > 0);
    }

    /**
     * Verifica il recupero di recensioni per un post specifico.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testRecuperoRecensioniPerPost() throws SQLException {
        String username = "recUser2_" + System.currentTimeMillis();
        Utente autore = createTestUser(username, TipoUtente.APPASSIONATO);
        Post post = createTestPost("Post con recensioni", autore);

        Recensione r1 = new Recensione(post, autore, 5, "Ottimo", "2024-01-27");

        String username2 = "recUser3_" + System.currentTimeMillis();
        Utente autore2 = createTestUser(username2, TipoUtente.CURIOSO);
        Recensione r2 = new Recensione(post, autore2, 4, "Buono", "2024-01-27");

        recensioneDAO.create(r1);
        recensioneDAO.create(r2);

        var recensioni = recensioneDAO.findByPost(post.getId());
        assertTrue(recensioni.size() >= 2);
    }

    /**
     * Verifica l'eliminazione di una Recensione dal database.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testEliminazioneRecensione() throws SQLException {
        String username = "recUser3_" + System.currentTimeMillis();
        Utente autore = createTestUser(username, TipoUtente.APPASSIONATO);
        Post post = createTestPost("Post da eliminare", autore);

        Recensione r = new Recensione(post, autore, 3, "Media", "2024-01-27");
        recensioneDAO.create(r);

        int id = r.getId();
        recensioneDAO.delete(id);

        // Verifica che la recensione sia stata eliminata
        var recensioni = recensioneDAO.findByPost(post.getId());
        assertTrue(recensioni.stream().noneMatch(rec -> rec.getId() == id));
    }

    /**
     * Verifica che i voti siano nel range corretto (1-5).
     */
    @Test
    public void testVotoRange() {
        Recensione r = new Recensione();

        // Test voti validi
        r.setVoto(1);
        assertEquals(1, r.getVoto());

        r.setVoto(3);
        assertEquals(3, r.getVoto());

        r.setVoto(5);
        assertEquals(5, r.getVoto());

        // Nota: La validazione del range dovrebbe essere implementata
        // nella logica di business o nel DAO
    }
}
