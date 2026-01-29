package it.univaq.brewhub.view;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import it.univaq.brewhub.model.Utente.TipoUtente;
import it.univaq.brewhub.model.Utente;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * Test per la vista di registrazione {@link RegisterView}.
 *
 * Verifica i casi di registrazione fallita (campi vuoti, password corta,
 * username esistente)
 * e i casi di successo per utenti normali e torrefattori.
 *
 */
@ExtendWith(ApplicationExtension.class)
class RegisterViewTest extends BaseUITest {
    private final String TEST_USER = "testUserReg";
    private final String TEST_TORREFATTORE = "testTorreReg";

    public static class TestableRegisterView extends RegisterView {
        public TestableRegisterView(Stage stage) {
            super(stage);
        }

        @Override
        protected File openFileChooser(Stage stage) {
            try {
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
        ensureDatabaseReady();
        TestableRegisterView view = new TestableRegisterView(stage);
        Scene scene = new Scene(view.getView());
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @AfterEach
    void cleanup() {
        try {
            utenteDAO.delete(TEST_USER);
            utenteDAO.delete(TEST_TORREFATTORE);
        } catch (SQLException e) {
        }
    }

    @Test
    void testRegistrazioneFallitaCampiVuoti(FxRobot robot) {
        robot.clickOn("#btnRegistrati");
        verifyThat("#lblErrore", isVisible());
        verifyThat("#lblErrore", hasText("⚠ Completa tutti i campi standard e carica una foto"));
    }

    @Test
    void testRegistrazionePasswordCorta(FxRobot robot) {
        robot.clickOn("#fldNome").write("NomeTest");
        robot.clickOn("#fldCognome").write("CognomeTest");
        robot.clickOn("#fldUsername").write(TEST_USER);
        robot.clickOn(".password-field").write("short");
        robot.clickOn("#btnFoto");
        robot.clickOn("#btnRegistrati");
        verifyThat("#lblErrore", isVisible());
        verifyThat("#lblErrore", hasText("✗ Password troppo corta (minimo 8 caratteri)"));
    }

    @Test
    void testRegistrazioneSuccesso(FxRobot robot) {
        robot.clickOn("#fldNome").write("NomeTest");
        robot.clickOn("#fldCognome").write("CognomeTest");
        robot.clickOn("#fldUsername").write(TEST_USER);
        robot.clickOn(".password-field").write("passwordSicura123");
        robot.clickOn("#btnFoto");
        robot.clickOn("#btnRegistrati");
        if (robot.lookup("#lblErrore").tryQuery().map(node -> node.isVisible()).orElse(false)) {
            javafx.scene.control.Label lbl = robot.lookup("#lblErrore").queryAs(javafx.scene.control.Label.class);
            org.junit.jupiter.api.Assertions.fail("Registration failed with UI error: " + lbl.getText());
        }
        try {
            Utente u = utenteDAO.findByUsername(TEST_USER);
            org.junit.jupiter.api.Assertions.assertNotNull(u, "Utente dovrebbe essere stato creato");
        } catch (SQLException e) {
            org.junit.jupiter.api.Assertions.fail("Errore DB: " + e.getMessage());
        }
    }

    @Test
    void testRegistrazioneUsernameEsistente(FxRobot robot) {
        Utente u = new Utente("N", "C", TEST_USER, "pass", TipoUtente.CURIOSO, "img");
        try {
            utenteDAO.create(u);
        } catch (SQLException e) {
        }
        robot.clickOn("#fldNome").write("NuovoNome");
        robot.clickOn("#fldCognome").write("NuovoCognome");
        robot.clickOn("#fldUsername").write(TEST_USER);
        robot.clickOn(".password-field").write("passwordValidissima");
        robot.clickOn("#btnFoto");
        robot.clickOn("#btnRegistrati");
        verifyThat("#lblErrore", isVisible());
        verifyThat("#lblErrore", hasText("✗ Username già in uso"));
    }

    @Test
    void testRegistrazioneTorrefattore(FxRobot robot) {
        robot.clickOn("#cbxTipo");
        robot.clickOn("Torrefattore");
        robot.clickOn("#fldNome").write("Torre");
        robot.clickOn("#fldCognome").write("Fattore");
        robot.clickOn("#fldUsername").write(TEST_TORREFATTORE);
        robot.clickOn(".password-field").write("passwordTorrefattore");
        robot.clickOn("#btnFoto");
        robot.clickOn("#fldNomeAzienda").write("Torrefazione SRL");
        robot.clickOn("#fldPartitaIva").write("12345678901");
        robot.clickOn("#fldIndirizzo").write("Via del Caffè 1");
        robot.clickOn("#fldDescrizione").write("Il miglior caffè.");
        robot.clickOn("#btnRegistrati");
        try {
            Utente created = utenteDAO.findByUsername(TEST_TORREFATTORE);
            org.junit.jupiter.api.Assertions.assertNotNull(created);
            org.junit.jupiter.api.Assertions.assertEquals(TipoUtente.TORREFATTORE, created.getTipo());
        } catch (SQLException e) {
            org.junit.jupiter.api.Assertions.fail(e.getMessage());
        }
    }

    @Test
    void testNavigazioneLogin(FxRobot robot) {
        robot.clickOn("Hai già un account? Accedi");
        verifyThat("#loginButton", isVisible());
    }
}
