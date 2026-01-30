package it.univaq.brewhub.view;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * Test per la vista di login {@link LoginView}.
 * Verifica i casi di login fallito (campi vuoti, credenziali errate) e il
 * funzionamento del componente password toggler.
 */
@ExtendWith(ApplicationExtension.class)
class LoginViewTest extends BaseUITest {

    /**
     * Inizializza la vista di login prima di ogni test.
     * 
     * @param stage stage JavaFX per il test.
     */
    @Start
    private void start(Stage stage) {
        ensureDatabaseReady();
        LoginView loginView = new LoginView(stage);
        Scene scene = new Scene(loginView.getView());
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    /**
     * Verifica il comportamento quando si tenta il login con campi vuoti.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testLoginFallitoCampiVuoti(FxRobot robot) {
        robot.clickOn("#loginButton");
        verifyThat("#errorLabel", isVisible());
        verifyThat("#errorLabel", hasText("⚠ Inserisci username e password"));
    }

    /**
     * Verifica il comportamento quando si tenta il login con credenziali errate.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testLoginFallitoCredenzialiErrate(FxRobot robot) {
        robot.clickOn("#usernameField").write("utenteInesistenteTest");
        robot.clickOn(".password-field").write("passwordErrataTest");
        robot.clickOn("#loginButton");
        verifyThat("#errorLabel", isVisible());
        verifyThat("#errorLabel", hasText("✗ Credenziali non valide"));
    }

    /**
     * Verifica il funzionamento del componente per mostrare/nascondere la password.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testPasswordTogglerComponent(FxRobot robot) {
        robot.clickOn(".password-field").write("secretLogin");
        verifyThat(".password-field", isVisible());
        verifyThat(".password-toggle-btn", isVisible());
        robot.clickOn(".password-toggle-btn");
        verifyThat(".password-field", org.testfx.matcher.base.NodeMatchers.isInvisible());
        verifyThat(".plain-text-mode", isVisible());
        javafx.scene.control.TextField tf = robot.lookup(".plain-text-mode")
                .queryAs(javafx.scene.control.TextField.class);
        org.junit.jupiter.api.Assertions.assertEquals("secretLogin", tf.getText());
        robot.clickOn(".password-toggle-btn");
        verifyThat(".password-field", isVisible());
        verifyThat(".plain-text-mode", org.testfx.matcher.base.NodeMatchers.isInvisible());
    }
}
