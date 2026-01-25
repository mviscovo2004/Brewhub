package it.univaq.brewhub.UI;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import it.univaq.brewhub.DatabaseManager;
import java.sql.SQLException;

@ExtendWith(ApplicationExtension.class)
class ProfileViewTest {

    private static final String TEST_DB_PATH = "brewhub_test_ui_profile.db";

    @BeforeAll
    public static void setupClass() throws SQLException {
        java.io.File dbFile = new java.io.File(TEST_DB_PATH);
        if (dbFile.exists())
            dbFile.delete();
        DatabaseManager.configureTestDatabase(TEST_DB_PATH);
        DatabaseManager.init();
    }

    @AfterAll
    public static void tearDownClass() {
        try {
            System.gc();
            Thread.sleep(100);
            java.io.File dbFile = new java.io.File(TEST_DB_PATH);
            if (dbFile.exists())
                dbFile.delete();
        } catch (Exception e) {
        }
    }

    private Utente testUtente;

    @Start
    private void start(Stage stage) {
        if (testUtente == null) {
            testUtente = new Utente("Profilo", "Tester", "profileUserTest", "p", TipoUtente.APPASSIONATO, null);
        }
        ProfileView view = new ProfileView(stage, testUtente);
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    void testProfileViewElements(FxRobot robot) {
        // Verify Title "Il mio Profilo"
        verifyThat("Il mio Profilo", isVisible());

        // Verify "Dati Personali" section
        verifyThat("Dati Personali", isVisible());

        // Verify Buttons
        verifyThat("Indietro", isVisible());
        verifyThat("Salva Modifiche", isVisible());
    }
}
