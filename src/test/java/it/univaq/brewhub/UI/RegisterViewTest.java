package it.univaq.brewhub.UI;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import it.univaq.brewhub.dao.impl.UtenteDAOImpl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class RegisterViewTest {

    private final String TEST_USER = "testUserReg";

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
        try {
            dao.delete(TEST_USER);
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
        verifyThat("#lblErrore", hasText("⚠ Completa tutti i campi e carica una foto"));
    }

    @Test
    void testRegistrazioneSuccesso(FxRobot robot) {
        // Compila il form
        robot.clickOn("#fldNome").write("NomeTest");
        robot.clickOn("#fldCognome").write("CognomeTest");

        // Seleziona Tipo Utente (di default è UTENTE_MEDIO, lo lasciamo così o lo
        // cambiamo)
        // robot.clickOn("#cbxTipo")...

        robot.clickOn("#fldUsername").write(TEST_USER);
        robot.clickOn("#fldPassword").write("passwordSicura123");

        // Simula click su foto (che userà il metodo mockato)
        robot.clickOn("#btnFoto");

        // Clicca su Registrati
        robot.clickOn("#btnRegistrati");

        // Verifica successo: Dovremmo essere nella HomeView.
        // Verifichiamo la presenza del titolo della dashboard
        verifyThat(".dashboard-title", hasText("✍️ Crea un nuovo post"));
    }
}
