package it.univaq.brewhub.UI;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;

import java.sql.SQLException;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
class UserProfileViewTest {

    private final String VIEWER_USER = "viewUserT";
    private final String TARGET_USER = "targUserT";
    private Utente viewer;
    private Utente target;

    @Start
    private void start(Stage stage) {
        if (viewer == null) {
            viewer = new Utente("V", "V", VIEWER_USER, "p", TipoUtente.APPASSIONATO, null);
            target = new Utente("Target", "User", TARGET_USER, "p", TipoUtente.TORREFATTORE, null);
        }

        UserProfileView view = new UserProfileView(stage, viewer, target);
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setupDB() throws SQLException {
        it.univaq.brewhub.DatabaseManager.init();
        UtenteDAOImpl dao = new UtenteDAOImpl();
        try {
            dao.create(viewer);
        } catch (SQLException e) {
        }
        try {
            dao.create(target);
        } catch (SQLException e) {
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        UtenteDAOImpl dao = new UtenteDAOImpl();
        dao.delete(VIEWER_USER);
        dao.delete(TARGET_USER);
    }

    @Test
    void testUserProfileElements(FxRobot robot) {
        // Verify Target Username is displayed
        verifyThat(TARGET_USER, isVisible());

        // Verify "Post Recenti" section title
        verifyThat("Post Recenti", isVisible());

        // Verify Stats labels
        verifyThat("Followers", isVisible());
        verifyThat("Following", isVisible());
    }
}
