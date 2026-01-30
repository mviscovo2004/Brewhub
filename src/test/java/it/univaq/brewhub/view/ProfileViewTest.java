package it.univaq.brewhub.view;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import java.sql.SQLException;

/**
 * Test per la vista del profilo personale {@link ProfileView}.
 * Verifica la presenza degli elementi UI principali della vista profilo.
 */
@ExtendWith(ApplicationExtension.class)
class ProfileViewTest extends BaseUITest {
    private Utente testUtente;

    /**
     * Inizializza la vista del profilo prima di ogni test.
     * 
     * @param stage stage JavaFX per il test.
     * @throws Exception se si verifica un errore durante l'inizializzazione.
     */
    @Start
    private void start(Stage stage) throws Exception {
        ensureDatabaseReady();
        if (testUtente == null) {
            testUtente = new Utente("Profilo", "Tester", "profileUserTest", "p", TipoUtente.APPASSIONATO, null);
        }
        try {
            utenteDAO.create(testUtente);
        } catch (SQLException e) {
            // Ignora eccezioni se l'utente esiste
        }
        ProfileView view = new ProfileView(stage, testUtente);
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    /**
     * Verifica la presenza degli elementi principali nella vista del profilo.
     * Controlla che il titolo, le sezioni dati e i pulsanti di navigazione siano
     * visibili.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testProfileViewElements(FxRobot robot) {
        verifyThat("Il mio Profilo", isVisible());
        verifyThat("Dati Personali", isVisible());
        verifyThat("Indietro", isVisible());
        verifyThat("Salva Modifiche", isVisible());
    }
}
