package it.univaq.brewhub.business;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;

/**
 * Test per la classe {@link UserService}.
 * <p>
 * Verifica la logica di business per la gestione degli utenti,
 * inclusi login, registrazione e ricerca.
 * </p>
 */
public class UserServiceTest extends BaseTest {

    private UserService userService;

    /**
     * Configurazione iniziale per ogni test.
     * <p>
     * Ottiene l'istanza singleton di UserService.
     * </p>
     * 
     * @throws SQLException se si verifica un errore durante la configurazione
     */
    @BeforeEach
    public void setUp() throws SQLException {
        userService = UserService.getInstance();
    }

    /**
     * Verifica che UserService sia un Singleton.
     */
    @Test
    public void testSingleton() {
        UserService instance1 = UserService.getInstance();
        UserService instance2 = UserService.getInstance();

        assertSame(instance1, instance2);
    }

    /**
     * Verifica il login con credenziali valide.
     * 
     * @throws BusinessException se si verifica un errore di business
     * @throws SQLException      se si verifica un errore durante l'accesso al
     *                           database
     */
    @Test
    public void testLoginSuccesso() throws BusinessException, SQLException {
        String username = "loginUser_" + System.currentTimeMillis();
        Utente utente = new Utente("Test", "User", username, "password123", TipoUtente.APPASSIONATO, null);
        utenteDAO.create(utente);

        Utente loggedIn = userService.login(username, "password123");

        assertNotNull(loggedIn);
        assertEquals(username, loggedIn.getUsername());
    }

    /**
     * Verifica il login con credenziali errate.
     * 
     * @throws BusinessException se si verifica un errore di business
     */
    @Test
    public void testLoginFallito() throws BusinessException {
        String username = "nonExistentUser_" + System.currentTimeMillis();

        Utente result = userService.login(username, "wrongpassword");

        assertNull(result);
    }

    /**
     * Verifica la registrazione di un nuovo utente.
     * 
     * @throws BusinessException se si verifica un errore di business
     * @throws SQLException      se si verifica un errore durante l'accesso al
     *                           database
     */
    @Test
    public void testRegistrazioneSuccesso() throws BusinessException, SQLException {
        String username = "newUser_" + System.currentTimeMillis();
        Utente nuovoUtente = new Utente("Nuovo", "Utente", username, "password", TipoUtente.CURIOSO, null);

        userService.registerUser(nuovoUtente);

        Utente retrieved = utenteDAO.findByUsername(username);
        assertNotNull(retrieved);
        assertEquals(username, retrieved.getUsername());
    }

    /**
     * Verifica che la registrazione fallisca con username già esistente.
     * 
     * @throws BusinessException se si verifica un errore di business
     * @throws SQLException      se si verifica un errore durante l'accesso al
     *                           database
     */
    @Test
    public void testRegistrazioneUsernameEsistente() throws SQLException {
        String username = "existingUser_" + System.currentTimeMillis();
        Utente utente1 = new Utente("First", "User", username, "pass1", TipoUtente.APPASSIONATO, null);
        utenteDAO.create(utente1);

        Utente utente2 = new Utente("Second", "User", username, "pass2", TipoUtente.CURIOSO, null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.registerUser(utente2);
        });

        assertTrue(exception.getMessage().contains("Username già in uso") ||
                exception.getMessage().contains("Username esistente"));
    }

    /**
     * Verifica la ricerca di utenti per username parziale.
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testRicercaUtenti() throws SQLException {
        String prefix = "searchUser_" + System.currentTimeMillis();

        createTestUser(prefix + "_1", TipoUtente.APPASSIONATO);
        createTestUser(prefix + "_2", TipoUtente.CURIOSO);
        createTestUser("other_" + System.currentTimeMillis(), TipoUtente.APPASSIONATO);

        var risultati = userService.searchUsers(prefix);

        assertTrue(risultati.size() >= 2);
        assertTrue(risultati.stream().allMatch(u -> u.getUsername().contains(prefix)));
    }

    /**
     * Verifica che la ricerca restituisca una lista vuota se non ci sono risultati.
     */
    @Test
    public void testRicercaSenzaRisultati() {
        String query = "nonExistentQuery_" + System.currentTimeMillis();

        var risultati = userService.searchUsers(query);

        assertNotNull(risultati);
        assertTrue(risultati.isEmpty());
    }

    /**
     * Verifica che il login gestisca correttamente username nullo.
     */
    @Test
    public void testLoginUsernameNullo() {
        assertThrows(Exception.class, () -> {
            userService.login(null, "password");
        });
    }

    /**
     * Verifica che il login gestisca correttamente password nulla.
     */
    @Test
    public void testLoginPasswordNulla() {
        assertThrows(Exception.class, () -> {
            userService.login("username", null);
        });
    }

    /**
     * Verifica che la registrazione gestisca correttamente utente nullo.
     */
    @Test
    public void testRegistrazioneUtenteNullo() {
        assertThrows(Exception.class, () -> {
            userService.registerUser(null);
        });
    }
}
