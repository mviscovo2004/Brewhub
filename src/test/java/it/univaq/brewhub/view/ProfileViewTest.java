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
 *
 * Verifica la presenza degli elementi UI principali della vista profilo.
 *
 */
@ExtendWith(ApplicationExtension.class)
class ProfileViewTest extends BaseUITest {
    private Utente testUtente;

    @Start
    private void start(Stage stage) throws Exception {
        ensureDatabaseReady();
        if (testUtente == null) {
            testUtente = new Utente("Profilo", "Tester", "profileUserTest", "p", TipoUtente.APPASSIONATO, null);
        }
        try {
            utenteDAO.create(testUtente);
        } catch (SQLException e) {
        }
        ProfileView view = new ProfileView(stage, testUtente);
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    void testProfileViewElements(FxRobot robot) {
        verifyThat("Il mio Profilo", isVisible());
        verifyThat("Dati Personali", isVisible());
        verifyThat("Indietro", isVisible());
        verifyThat("Salva Modifiche", isVisible());
    }
}
