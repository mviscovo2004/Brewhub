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
import java.sql.SQLException;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test per la vista del profilo di un altro utente {@link UserProfileView}.
 *
 * Verifica la visualizzazione del profilo di un utente target da parte di un
 * utente viewer.
 *
 */
@ExtendWith(ApplicationExtension.class)
class UserProfileViewTest extends BaseUITest {
    private final String VIEWER_USER = "viewUserT";
    private final String TARGET_USER = "targUserT";
    private Utente viewer;
    private Utente target;

    @Start
    private void start(Stage stage) {
        ensureDatabaseReady();
        if (viewer == null) {
            viewer = new Utente("V", "V", VIEWER_USER, "p", TipoUtente.APPASSIONATO, null);
            target = new Utente("Target", "User", TARGET_USER, "p", TipoUtente.TORREFATTORE, null);
        }
        try {
            utenteDAO.create(viewer);
        } catch (SQLException e) {
        }
        try {
            utenteDAO.create(target);
        } catch (SQLException e) {
        }
        UserProfileView view = new UserProfileView(stage, viewer, target);
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    void testUserProfileElements(FxRobot robot) {
        verifyThat(TARGET_USER, isVisible());
        verifyThat("Post Recenti", isVisible());
        verifyThat("Followers", isVisible());
        verifyThat("Following", isVisible());
    }
}
