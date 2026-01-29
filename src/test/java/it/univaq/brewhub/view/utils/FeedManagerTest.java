package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import it.univaq.brewhub.view.BaseUITest;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
class FeedManagerTest extends BaseUITest {

    private FeedManager feedManager;
    private VBox feedContainer;
    private ScrollPane scrollPane;
    private Utente testUser;

    @Start
    private void start(Stage stage) {
        ensureDatabaseReady();

        feedContainer = new VBox();
        scrollPane = new ScrollPane(feedContainer);

        // We need a dummy user logged in
        testUser = new Utente("Test", "User", "testuser", "password", TipoUtente.APPASSIONATO, null);
        // Persist user? BaseUITest helper does it in BeforeEach usually, but here we do
        // it in start or before
        // ensureDatabaseReady initializes DB. We can create user here if needed for
        // FeedManager logic.
        // FeedManager uses user object mainly for checking likes/ownership, doesn't
        // necessarily need it in DB unless queries fail.
        // But PostService queries usually involve joins or checks.

        feedManager = new FeedManager(testUser, feedContainer, scrollPane);

        Scene scene = new Scene(scrollPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setupData() {
        // Create user in DB to match our testUser object (though ID might differ if not
        // synchronized)
        // Better to recreate testUser from DB factory to be sure.
        testUser = createTestUser("testuser", TipoUtente.APPASSIONATO);
        // Re-init feed manager with persistent user if needed, or just keep as is if
        // only username matters.
        // FeedManager uses 'utenteLoggato' object.
    }

    @AfterEach
    void cleanup() throws SQLException {
        // DB cleanup handled by BaseTest/BaseUITest logic usually, but here we might
        // need explicit table clearing
        // if BaseTest only deletes DB file. (BaseTest deletes DB file, so it's fresh
        // for each test)
    }

    @Test
    void testLoadFeed_Empty() {
        // Act
        // Run on JavaFX thread? loadFeed uses AsyncTaskHelper which offloads, then
        // updates UI on FX thread.
        // calling from test thread is fine.
        javafx.application.Platform.runLater(() -> feedManager.loadFeed());

        WaitForAsyncUtils.waitForFxEvents();
        // Wait specifically for async task completion?
        // simple way: wait a bit
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WaitForAsyncUtils.waitForFxEvents();

        // Assert
        // "Nessun Post" title in empty state
        verifyThat("📭 Nessun Post", isVisible());
    }

    @Test
    void testLoadFeed_WithPosts() {
        // Arrange
        createTestPost("Post di Test 1", testUser);
        createTestPost("Post di Test 2", testUser);

        // Act
        javafx.application.Platform.runLater(() -> feedManager.loadFeed());

        WaitForAsyncUtils.waitForFxEvents();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WaitForAsyncUtils.waitForFxEvents();

        // Assert
        verifyThat("Post di Test 1", isVisible());
        verifyThat("Post di Test 2", isVisible());
        // Verify header
        verifyThat("🏠 Home Feed", isVisible());
    }
}
