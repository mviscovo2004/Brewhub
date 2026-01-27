package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import org.junit.jupiter.api.*;
import it.univaq.brewhub.Utente.TipoUtente;

/**
 * Test unitari per la classe {@link Utente}.
 *
 * Verifica i costruttori, i metodi getter/setter, le operazioni CRUD sul
 * database,
 * l'integrazione con {@link it.univaq.brewhub.business.SessionManager} e le
 * funzionalità
 * specifiche come la ricerca e il ranking degli utenti più attivi.
 *
 */
public class UtenteTest extends BaseTest {
    /**
     * Verifica il costruttore per utenti ospiti.
     *
     * Controlla che un utente ospite venga creato correttamente con tipo OSPITE
     * e senza password.
     *
     */
    @Test
    public void testCostruttoreOspite() {
        String username = "guest";
        Utente ospite = new Utente(username);
        assertEquals(username, ospite.getUsername());
        assertEquals(TipoUtente.OSPITE, ospite.getTipo());
        assertNull(ospite.getPassword());
    }

    /**
     * Verifica il costruttore completo con tutti i parametri.
     *
     * Controlla che tutti i campi vengano inizializzati correttamente.
     *
     */
    @Test
    public void testCostruttoreCompleto() {
        String nome = "Mario";
        String cognome = "Rossi";
        String username = "mario.rossi";
        String password = "passwordSegreta";
        TipoUtente tipo = TipoUtente.APPASSIONATO;
        String foto = "media/foto.jpg";
        Utente utente = new Utente(nome, cognome, username, password, tipo, foto);
        assertEquals(nome, utente.getNome());
        assertEquals(cognome, utente.getCognome());
        assertEquals(username, utente.getUsername());
        assertEquals(tipo, utente.getTipo());
        assertEquals(foto, utente.getFotoProfilo());
        assertEquals(password, utente.getPassword());
    }

    /**
     * Verifica i metodi setter e getter della classe Utente.
     *
     * Testa tutti i setter e getter per assicurarsi che i valori
     * vengano impostati e recuperati correttamente.
     *
     */
    @Test
    public void testSetterGetter() {
        Utente u = new Utente();
        u.setNome("Luigi");
        assertEquals("Luigi", u.getNome());
        u.setCognome("Verdi");
        assertEquals("Verdi", u.getCognome());
        u.setUsername("luigi.verdi");
        assertEquals("luigi.verdi", u.getUsername());
        u.setPassword("passwordSegreta");
        assertEquals("passwordSegreta", u.getPassword());
        u.setFotoProfilo("media/foto.jpg");
        assertEquals("media/foto.jpg", u.getFotoProfilo());
        u.setPasswordCrypto("passwordSegreta");
        assertEquals("passwordSegreta", u.getPasswordCrypto());
        u.setTipo(TipoUtente.BARISTA);
        assertEquals(TipoUtente.BARISTA, u.getTipo());
    }

    /**
     * Verifica le operazioni CRUD sul database.
     *
     * Testa creazione, recupero, login, aggiornamento ed eliminazione
     * di un utente nel database.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testMetodiDB() throws SQLException {
        Utente u = createTestUser("mario_rossi", TipoUtente.APPASSIONATO);
        Utente retrieved = utenteDAO.findByUsername("mario_rossi");
        assertNotNull(retrieved, "L'utente dovrebbe essere stato salvato nel DB");
        assertEquals("Test", retrieved.getNome());
        Utente logged = utenteDAO.login("mario_rossi", "password");
        assertNotNull(logged, "Il login con password corretta dovrebbe riuscire");
        Utente failedLogin = utenteDAO.login("mario_rossi", "wrongpass");
        assertNull(failedLogin, "Il login con password errata dovrebbe fallire");
        u.setPassword("newpass");
        utenteDAO.update(u);
        Utente loggedNew = utenteDAO.login("mario_rossi", "newpass");
        assertNotNull(loggedNew, "Il login con la nuova password dovrebbe riuscire");
        utenteDAO.delete("mario_rossi");
        Utente deleted = utenteDAO.findByUsername("mario_rossi");
        assertNull(deleted, "L'utente dovrebbe essere stato rimosso");
    }

    /**
     * Verifica che la ricerca escluda gli utenti eliminati.
     *
     * Controlla che gli utenti eliminati (soft delete) non compaiano
     * nei risultati di ricerca.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testSearchExcludesDeleted() throws SQLException {
        String tempUsername = "toBeDeletedUser";
        createTestUser(tempUsername, TipoUtente.APPASSIONATO);
        utenteDAO.delete(tempUsername);
        java.util.List<Utente> results = utenteDAO.searchByUsername("deleted");
        assertTrue(results.isEmpty(), "Searching for 'deleted' should return empty list");
        java.util.List<Utente> all = utenteDAO.searchByUsername("");
        for (Utente r : all) {
            assertFalse(r.getUsername().startsWith("deleted_"),
                    "Found a deleted user in search results: " + r.getUsername());
        }
    }

    /**
     * Verifica il pattern Singleton di SessionManager.
     *
     * Controlla che getInstance() restituisca sempre la stessa istanza.
     *
     */
    @Test
    public void testSessionManagerSingleton() {
        it.univaq.brewhub.business.SessionManager s1 = it.univaq.brewhub.business.SessionManager.getInstance();
        it.univaq.brewhub.business.SessionManager s2 = it.univaq.brewhub.business.SessionManager.getInstance();
        assertSame(s1, s2, "SessionManager deve essere un Singleton");
    }

    /**
     * Verifica le funzionalità di login e logout di SessionManager.
     *
     * Testa il ciclo completo di login e logout, verificando lo stato
     * della sessione e l'utente corrente.
     *
     */
    @Test
    public void testSessionManagerLoginLogout() {
        it.univaq.brewhub.business.SessionManager session = it.univaq.brewhub.business.SessionManager.getInstance();
        session.logout();
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());
        Utente u = new Utente("test", "user", "testUserSession", "pwd", TipoUtente.APPASSIONATO, null);
        session.login(u);
        assertTrue(session.isLoggedIn());
        assertEquals(u, session.getCurrentUser());
        session.logout();
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());
    }

    /**
     * Verifica il workflow completo per un Torrefattore.
     *
     * Testa creazione, recupero e verifica dei dati specifici
     * di un utente di tipo Torrefattore.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testTorrefattoreWorkflow() throws SQLException {
        String username = "test_torrefattore_" + System.currentTimeMillis();
        createTestTorrefattore(username);
        Torrefattore retrieved = torrefattoreDAO.findByUsername(username);
        assertNotNull(retrieved, "Il torrefattore dovrebbe essere stato trovato");
        assertEquals(username, retrieved.getUsername());
        assertEquals("Torre", retrieved.getNome());
        assertEquals("12345678901", retrieved.getPartitaIva());
        Utente asUtente = utenteDAO.findByUsername(username);
        assertNotNull(asUtente);
        assertEquals(TipoUtente.TORREFATTORE, asUtente.getTipo());
        torrefattoreDAO.delete(username);
    }

    /**
     * Verifica il ranking degli utenti più attivi.
     *
     * Controlla che gli utenti con più post vengano classificati
     * prima di quelli con meno post.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testFindTopActiveUsers() throws SQLException {
        String uTop = "test_top_" + System.currentTimeMillis();
        String uBot = "test_bot_" + System.currentTimeMillis();
        Utente top = createTestUser(uTop, TipoUtente.CURIOSO);
        createTestUser(uBot, TipoUtente.CURIOSO);
        createTestPost("T1", top);
        createTestPost("T2", top);
        java.util.List<Utente> toplist = utenteDAO.findTopActiveUsers(100);
        int topRank = -1;
        int botRank = -1;
        for (int i = 0; i < toplist.size(); i++) {
            if (toplist.get(i).getUsername().equals(uTop))
                topRank = i;
            if (toplist.get(i).getUsername().equals(uBot))
                botRank = i;
        }
        assertTrue(topRank != -1, "Top user should be in list");
        if (botRank != -1) {
            assertTrue(topRank < botRank,
                    "Top user (2 posts) must be ranked higher/earlier than Bottom user (0 posts)");
        }
        utenteDAO.delete(uTop);
        utenteDAO.delete(uBot);
    }
}
