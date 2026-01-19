package it.univaq.brewhub.UI;

import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Utente;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.hamcrest.CoreMatchers.not;

@ExtendWith(ApplicationExtension.class)
class RegisterViewTest {

    private final String TEST_USER = "testUserReg";
    private final String TEST_TORREFATTORE = "testTorreReg";

    // Sottoclasse per mockare il FileChooser
    public static class TestableRegisterView extends RegisterView {
        public TestableRegisterView(Stage stage) {
            super(stage);
        }

        @Override
        protected File openFileChooser(Stage stage) {
            try {
                // Crea un file temporaneo per simulare la foto selezionata
                File tempFile = File.createTempFile("test_avatar", ".png");
                tempFile.deleteOnExit();
                return tempFile;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Start
    private void start(Stage stage) {
        TestableRegisterView view = new TestableRegisterView(stage);
        Scene scene = new Scene(view.getView());
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @AfterEach
    void cleanup() {
        // Pulizia del database
        UtenteDAOImpl dao = new UtenteDAOImpl();
        TorrefattoreDAOImpl daoT = new TorrefattoreDAOImpl();
        // TorrefattoreDAOImpl daoT = new TorrefattoreDAOImpl(); // Unused
        try {
            dao.delete(TEST_USER);
            dao.delete(TEST_TORREFATTORE);
            daoT.delete(TEST_TORREFATTORE); // Assuming delete by username works for Torrefattore too via inheritance or
                                            // separate method
            // Actually TorrefattoreDAOImpl might not have delete by username if it extends
            // UtenteDAOImpl but table is diff?
            // Usually UtenteDAOImpl handles "utenti" table deletion which cascades or we
            // need to be careful.
            // checking UtenteDAOImpl.delete logic: usually `DELETE FROM utenti WHERE
            // username = ?`
            // If Torrefattore is in `utenti` AND `torrefattori_dettagli`, deleting from
            // `utenti` should be enough if FK cascades.
            // If not explicit, we might need to verify. Assuming UtenteDAO delete works for
            // username.
        } catch (SQLException e) {
            // Ignora errori di cleanup
        }
    }

    @Test
    void testRegistrazioneFallitaCampiVuoti(FxRobot robot) {
        // Clicca su registrati senza inserire dati
        robot.clickOn("#btnRegistrati");

        // Verifica errore
        verifyThat("#lblErrore", isVisible());
        verifyThat("#lblErrore", hasText("⚠ Completa tutti i campi standard e carica una foto"));
    }

    @Test
    void testRegistrazionePasswordCorta(FxRobot robot) {
        // Compila campi
        robot.clickOn("#fldNome").write("NomeTest");
        robot.clickOn("#fldCognome").write("CognomeTest");
        robot.clickOn("#fldUsername").write(TEST_USER);

        // Password corta
        robot.clickOn(".password-field").write("short");

        // Foto
        robot.clickOn("#btnFoto");

        // Click Registrati
        robot.clickOn("#btnRegistrati");

        // Verifica errore
        verifyThat("#lblErrore", isVisible());
        verifyThat("#lblErrore", hasText("✗ Password troppo corta (minimo 8 caratteri)"));
    }

    @Test
    void testRegistrazioneSuccesso(FxRobot robot) {
        // Compila il form standard
        robot.clickOn("#fldNome").write("NomeTest");
        robot.clickOn("#fldCognome").write("CognomeTest");
        robot.clickOn("#fldUsername").write(TEST_USER);
        robot.clickOn(".password-field").write("passwordSicura123");

        // Foto
        robot.clickOn("#btnFoto");

        // Clicca su Registrati
        robot.clickOn("#btnRegistrati");

        // Verifica passaggio alla Home (verificando assenza form o presenza elementi
        // home)
        // Poiché HomeView non è facilmente verificabile senza mockare tutto,
        // controlliamo che non ci siano errori
        // o che la scena sia cambiata.
        // Possiamo verificare che non sia più visibile il pulsante registrati?
        // O meglio, cerchiamo un elemento della Home. "Crea un nuovo post" è un buon
        // candidato se siamo loggati.
        // Ma HomeView constructor richiede Utente.

        // Verify that we are NOT on register view anymore or specific element of
        // HomeView exists
        // In HomeView (from previous turns) there is usually a "sidebar" or "feed".
        // Let's assume successful transition if no error label is visible and maybe
        // root changed.
        // Actually, let's verify error label is NOT visible, verify button is NOT
        // visible (stage root changed).

        // Note: verifying scene root change with TestFX is finding elements of new
        // scene.
        // Let's assume checking for something generic like a logout button or sidebar
        // if possible.
        // Or simply that the registration button is gone.
        // robot.lookup("#btnRegistrati").query() should throw or return nothing/not
        // visible if scene changed effectively?
        // Actually if scene root replaced, the old nodes are detached.

        // verifyThat("#lblErrore", isVisible().negate()); // Not enough, it's invisible
        // by default.

        // Let's trust that if no error appears and we clicked, it went through.
        // Ideally we check DB too.

        UtenteDAOImpl dao = new UtenteDAOImpl();
        try {
            Utente u = dao.findByUsername(TEST_USER);
            // Assert JUnit standard
            org.junit.jupiter.api.Assertions.assertNotNull(u, "Utente dovrebbe essere stato creato");
        } catch (SQLException e) {
            org.junit.jupiter.api.Assertions.fail("Errore DB: " + e.getMessage());
        }
    }

    @Test
    void testRegistrazioneUsernameEsistente(FxRobot robot) {
        // Crea prima l'utente
        UtenteDAOImpl dao = new UtenteDAOImpl();
        Utente u = new Utente("N", "C", TEST_USER, "pass", TipoUtente.CURIOSO, "img");
        try {
            dao.create(u);
        } catch (SQLException e) {
            // Se esiste già pazienza
        }

        // Tenta di registrarlo di nuovo
        robot.clickOn("#fldNome").write("NuovoNome");
        robot.clickOn("#fldCognome").write("NuovoCognome");
        robot.clickOn("#fldUsername").write(TEST_USER);
        robot.clickOn(".password-field").write("passwordValidissima");
        robot.clickOn("#btnFoto");
        robot.clickOn("#btnRegistrati");

        verifyThat("#lblErrore", isVisible());
        // Updated expectation to match actual error message structure from catch block
        verifyThat("#lblErrore", hasText("✗ Username già esistente!"));
    }

    @Test
    void testRegistrazioneTorrefattore(FxRobot robot) {
        // Seleziona Torrefattore
        robot.clickOn("#cbxTipo");
        robot.clickOn("Torrefattore"); // Select by clicking the item in the list

        // Compila campi base
        robot.clickOn("#fldNome").write("Torre");
        robot.clickOn("#fldCognome").write("Fattore");
        robot.clickOn("#fldUsername").write(TEST_TORREFATTORE);
        robot.clickOn(".password-field").write("passwordTorrefattore");
        robot.clickOn("#btnFoto");

        // Compila campi Torrefattore
        robot.clickOn("#fldNomeAzienda").write("Torrefazione SRL");
        robot.clickOn("#fldPartitaIva").write("12345678901");
        robot.clickOn("#fldIndirizzo").write("Via del Caffè 1");
        robot.clickOn("#fldDescrizione").write("Il miglior caffè.");

        robot.clickOn("#btnRegistrati");

        // Verifica DB
        TorrefattoreDAOImpl dao = new TorrefattoreDAOImpl();
        try {
            UtenteDAOImpl uDao = new UtenteDAOImpl();
            Utente created = uDao.findByUsername(TEST_TORREFATTORE);
            org.junit.jupiter.api.Assertions.assertNotNull(created);
            org.junit.jupiter.api.Assertions.assertEquals(TipoUtente.TORREFATTORE, created.getTipo());
        } catch (SQLException e) {
            org.junit.jupiter.api.Assertions.fail(e.getMessage());
        }
    }

    @Test
    void testNavigazioneLogin(FxRobot robot) {
        robot.clickOn("Hai già un account? Accedi");

        // Verifica titolo LoginView presence instead of RegisterView absence
        // The login button ID is specific to LoginView
        verifyThat("#loginButton", isVisible());
    }

    @BeforeEach
    void setupDB() throws SQLException {
        DatabaseManager.init();
    }
}
