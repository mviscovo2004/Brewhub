package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.*;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;

/**
 * Classe di test per la gestione degli Utenti (Utente.java).
 * Verifica i costruttori, i metodi getter/setter e, crucialmente,
 * le operazioni dirette sul database (Registrazione, Login, Modifica,
 * Cancellazione).
 */
public class UtenteTest {

    /**
     * Test del costruttore per il profilo OSPITE.
     * Verifica che un utente ospite abbia username impostato, ma password null
     * e tipo corretto.
     */
    @Test
    public void testCostruttoreOspite() {
        String username = "guest";
        Utente ospite = new Utente(username);

        assertEquals(username, ospite.getUsername(),
                "Lo username dovrebbe corrispondere a quello passato nel costruttore");
        assertEquals(TipoUtente.OSPITE, ospite.getTipo(), "Il tipo utente dovrebbe essere OSPITE");
        assertNull(ospite.getPassword(), "La password per l'ospite dovrebbe essere null");
    }

    /**
     * Test del costruttore completo (per registrazione).
     * Verifica che tutti i campi vengano assegnati correttamente e che la password
     * venga criptata.
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

        // Asserzioni su campi in chiaro
        assertEquals(nome, utente.getNome());
        assertEquals(cognome, utente.getCognome());
        assertEquals(username, utente.getUsername());
        assertEquals(tipo, utente.getTipo());
        assertEquals(foto, utente.getFotoProfilo());
        assertEquals(password, utente.getPassword());

        // Asserzione su campo calcolato (Hash Password) -> RIMOSSO: Il POJO non calcola
        // più l'hash, lo fa il DAO.
        // assertNotNull(utente.getPasswordCrypto(), "La password criptata non dovrebbe
        // essere null");
    }

    /**
     * Test dei metodi (Setter/Getter).
     * Verifica il corretto incapsulamento dei dati.
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
     * Test di integrazione Database (CRUD Completo).
     * Questo test simula l'intero ciclo di vita di un utente nel sistema:
     * 1. Creazione (Registrazione)
     * 2. Autenticazione (Login)
     * 3. Modifica (Aggiornamento profilo)
     * 4. Cancellazione (Eliminazione account)
     * 
     * @throws SQLException In caso di errori SQL.
     */
    @Test
    public void testMetodiDB() throws SQLException {
        // Creazione utente completo per evitare NullPointerException durante la insert
        Utente u = new Utente("NomeTest", "CognomeTest", "userTestDB", "passwordTest", TipoUtente.APPASSIONATO,
                "media/test.jpg");

        UtenteDAOImpl dao = new UtenteDAOImpl();

        // CLEANUP PREVENTIVO:
        try {
            dao.delete(u.getUsername());
        } catch (SQLException e) {
            // Ignora se l'utente non esiste già (situazione normale)
        }

        // 1. REGISTRAZIONE
        // Salva il nuovo utente nel DB
        dao.create(u);

        // 2. VERIFICA LOGIN
        // Tenta di recuperare l'utente usando le credenziali corrette
        Utente logged = dao.login("userTestDB", "passwordTest");

        // Verifiche
        assertNotNull(logged, "Il login dovrebbe restituire un utente valido dopo la registrazione");
        assertEquals("NomeTest", logged.getNome(), "Il nome recuperato dal DB dovrebbe corrispondere");
        assertEquals("CognomeTest", logged.getCognome(), "Il cognome recuperato dal DB dovrebbe corrispondere");

        // 3. AGGIORNAMENTO PROFILO
        // Modifica l'oggetto locale
        // Nota: Bisogna aggiornare l'oggetto che useremo per l'update. 'logged' è
        // quello DB. 'u' è quello originale.
        // Possiamo usare 'logged' restituito dal login che ha ID, ecc (se avesse ID).
        // Utente usa username come key.
        logged.setNome("NomeAggiornato");
        // Salva le modifiche nel DB
        dao.update(logged);

        // Effettua un nuovo login per verificare che i dati siano stati aggiornati
        // persistentemente
        Utente updated = dao.login("userTestDB", "passwordTest");
        assertEquals("NomeAggiornato", updated.getNome(), "Il nome nel DB dovrebbe essere stato aggiornato");

        // 4. ELIMINAZIONE ACCOUNT
        // Rimuove l'utente dal DB
        dao.delete(u.getUsername());

        // Verifica finale: il login non deve più essere possibile
        assertNull(dao.login("userTestDB", "passwordTest"),
                "Dopo l'eliminazione il login deve fallire (restituire null)");
    }

    @BeforeAll
    public static void init() {

    }
}
