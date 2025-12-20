package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.*;
import it.univaq.brewhub.Utente.TipoUtente;

public class UtenteTest {

    @Test
    public void testCostruttoreOspite() {
        String username = "guest";
        Utente ospite = new Utente(username);

        assertEquals(username, ospite.getUsername(),
                "Lo username dovrebbe corrispondere a quello passato nel costruttore");
        assertEquals(TipoUtente.OSPITE, ospite.getTipo(), "Il tipo utente dovrebbe essere OSPITE");
        assertNull(ospite.getPassword(), "La password per l'ospite dovrebbe essere null");
    }

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
        assertNotNull(utente.getPasswordCrypto(), "La password criptata non dovrebbe essere null");
    }

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

    @Test
    public void testMetodiDB() throws SQLException {

        Utente u = new Utente();
        u.registraUtente(u);

        u.login(u.getUsername(), u.getPassword());
        u.aggiornaProfilo();

        u.eliminaAccount();

    }

    @BeforeAll
    public void init() {

    }
}
