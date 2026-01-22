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

@ExtendWith(ApplicationExtension.class)
class ProfileViewTest {

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
