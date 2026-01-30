package it.univaq.brewhub.view;

import javafx.scene.Scene;
import javafx.stage.Stage;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import java.sql.SQLException;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Test per la vista della chat {@link ChatView}.
 * Verifica la corretta visualizzazione degli elementi UI della chat,
 * inclusa la sidebar dei messaggi e il placeholder iniziale.
 */
@ExtendWith(ApplicationExtension.class)
class ChatViewTest extends BaseUITest {

    private final String TEST_USER = "chatViewTestUser";
    private Utente testUtente;

    /**
     * Inizializza la vista della chat prima di ogni test.
     * 
     * @param stage stage JavaFX per il test.
     * @throws Exception se si verifica un errore durante l'inizializzazione.
     */
    @Start
    private void start(Stage stage) throws Exception {
        ensureDatabaseReady();
        if (testUtente == null) {
            testUtente = new Utente("Chat", "Tester", TEST_USER, "p", TipoUtente.APPASSIONATO, null);
        }
        try {
            utenteDAO.create(testUtente);
        } catch (SQLException e) {
        }
        ChatView view = new ChatView(stage, testUtente, null);
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    /**
     * Verifica che la sidebar e il placeholder siano visibili.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testSidebarAndPlaceholderVisible(FxRobot robot) {
        verifyThat("Messaggi", isVisible());
        verifyThat("Scegli una chat o creane una nuova", isVisible());
        verifyThat("+", isVisible());
    }
}
