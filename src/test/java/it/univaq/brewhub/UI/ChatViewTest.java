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
class ChatViewTest {

    private final String TEST_USER = "chatViewTestUser";
    private Utente testUtente;

    @Start
    private void start(Stage stage) {
        if (testUtente == null) {
            testUtente = new Utente("Chat", "Tester", TEST_USER, "p", TipoUtente.APPASSIONATO, null);
        }
        // Start view with no active chat
        ChatView view = new ChatView(stage, testUtente, null);
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
            dao.create(testUtente);
        } catch (SQLException e) {
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        UtenteDAOImpl dao = new UtenteDAOImpl();
        dao.delete(TEST_USER);
    }

    @Test
    void testSidebarAndPlaceholderVisible(FxRobot robot) {
        // Verify Sidebar Title
        verifyThat("Messaggi", isVisible());

        // Verify Placeholder Text (since no chat is selected)
        verifyThat("Scegli una chat o creane una nuova", isVisible());

        // Verify New Chat Button exists (by text or class, but text "+" is on button)
        verifyThat("+", isVisible());
    }
}
