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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import it.univaq.brewhub.DatabaseManager;

@ExtendWith(ApplicationExtension.class)
class ChatViewTest {

    private static final String TEST_DB_PATH = "brewhub_test_ui_chat.db";

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
