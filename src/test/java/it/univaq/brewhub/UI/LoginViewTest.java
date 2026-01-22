package it.univaq.brewhub.UI;

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

@ExtendWith(ApplicationExtension.class)
/**
 * Classe di test per l'interfaccia grafica di Login (LoginView).
 * Estende ApplicationExtension di TestFX per supportare i test su JavaFX con
 * JUnit 5.
 * 
 * Verifica il comportamento dell'interfaccia utente in scenari di errore come:
 * - Tentativo di login con campi vuoti.
 * - Tentativo di login con credenziali errate.
 */
class LoginViewTest {

    /**
     * Metodo di avvio (Start) eseguito prima di ogni test.
     * Inizializza lo stage principale e mostra la LoginView.
     * 
     * @param stage Lo stage primario fornito da TestFX.
     */
    @Start
    private void start(Stage stage) {
        // Inizializza la vista di login passando lo stage
        LoginView loginView = new LoginView(stage);

        // Crea la scena con il root ottenuto dalla vista
        Scene scene = new Scene(loginView.getView());

        // Imposta la scena sullo stage e lo mostra
        stage.setScene(scene);
        stage.show();
        stage.toFront(); // Porta la finestra in primo piano per garantire l'interazione
    }

    /**
     * Test: Login fallito per campi vuoti.
     * 
     * Scenario:
     * 1. L'utente clicca su "Accedi" senza inserire nulla.
     * 2. Il sistema deve mostrare un messaggio di errore.
     * 
     * @param robot Il robot di TestFX per simulare le interazioni utente.
     */
    @Test
    void testLoginFallitoCampiVuoti(FxRobot robot) {
        // Assicura che i campi siano vuoti all'avvio (non è necessario cancellare se è
        // il primo avvio)

        // Clicca sul bottone 'Accedi' identificato dall'ID "#loginButton"
        robot.clickOn("#loginButton");

        // Verifica che l'etichetta di errore (#errorLabel) sia visibile a schermo
        verifyThat("#errorLabel", isVisible());

        // Verifica che il testo dell'errore sia esattamente quello atteso
        verifyThat("#errorLabel", hasText("⚠ Inserisci username e password"));
    }

    /**
     * Test: Login fallito per credenziali errate.
     * 
     * Scenario:
     * 1. L'utente inserisce uno username e una password non validi.
     * 2. L'utente clicca su "Accedi".
     * 3. Il sistema deve mostrare un messaggio di errore specifico.
     * 
     * @param robot Il robot di TestFX per simulare le interazioni utente.
     */
    @Test
    void testLoginFallitoCredenzialiErrate(FxRobot robot) {
        // Inserisce credenziali sicuramente errate nei campi di testo identificati
        // dagli ID
        robot.clickOn("#usernameField").write("utenteInesistenteTest");
        // Use class selector since PasswordFieldWithToggler uses it internally
        robot.clickOn(".password-field").write("passwordErrataTest");

        // Clicca su 'Accedi'
        robot.clickOn("#loginButton");

        // Verifica che l'etichetta di errore sia diventata visibile
        verifyThat("#errorLabel", isVisible());

        // Verifica il messaggio di errore specifico per credenziali errate
        // Nota: Questo test assume che le credenziali usate non esistano nel DB.
        verifyThat("#errorLabel", hasText("✗ Credenziali non valide"));
    }

    /**
     * Test Added from PasswordFieldWithTogglerTest.
     * Verifies the custom password component used in Login and Registration.
     */
    @Test
    void testPasswordTogglerComponent(FxRobot robot) {
        // Interact with the password field in the Login View which uses
        // PasswordFieldWithToggler
        // Ensure we are on login view (start method does this)

        // The LoginView uses PasswordFieldWithToggler for the password field.
        // We can test the toggle functionality directly on the live view.

        // 1. Write text into password field
        robot.clickOn(".password-field").write("secretLogin");

        // 2. Verify text is hidden in password field (default) and plain text field is
        // hidden
        // Note: PasswordField text is masked in UI, but getText() returns the text.
        // We verify the nodes visibility.
        verifyThat(".password-field", isVisible());
        // The plain text field should be invisible originally
        // Note: LoginView implementation of PasswordFieldWithToggler might effectively
        // hide the text field.
        // We use the same CSS selectors logic as the isolated test.
        // If the component is correctly implemented, the .text-field inside it (with
        // .plain-text-mode) should be invisible.
        // However, LoginViewTest didn't verify .plain-text-mode existence before.
        // Let's rely on the button existence.

        // 3. Find the toggle button. It has class .password-toggle-btn
        verifyThat(".password-toggle-btn", isVisible());

        // 4. Click toggle
        robot.clickOn(".password-toggle-btn");

        // 5. Verify password field hidden, text field visible
        verifyThat(".password-field", org.testfx.matcher.base.NodeMatchers.isInvisible());

        // We need to look up the text field. It should have .plain-text-mode if my
        // previous edit persisted to the source class.
        // Wait, did I edit the SOURCE PasswordFieldWithToggler.java or just the TEST?
        // I edited the SOURCE in step 209 (successfully applied). So it has
        // .plain-text-mode.
        verifyThat(".plain-text-mode", isVisible());

        javafx.scene.control.TextField tf = robot.lookup(".plain-text-mode")
                .queryAs(javafx.scene.control.TextField.class);
        org.junit.jupiter.api.Assertions.assertEquals("secretLogin", tf.getText());

        // 6. Click again to hide
        robot.clickOn(".password-toggle-btn");
        verifyThat(".password-field", isVisible());
        verifyThat(".plain-text-mode", org.testfx.matcher.base.NodeMatchers.isInvisible());
    }
}
