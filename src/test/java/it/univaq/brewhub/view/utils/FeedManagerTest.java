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

/**
 * Test per la classe {@link FeedManager}.
 * Verifica il caricamento e la visualizzazione del feed dei post, gestendo sia
 * lo stato vuoto che la presenza di post.
 */
@ExtendWith(ApplicationExtension.class)
class FeedManagerTest extends BaseUITest {

    private FeedManager feedManager;
    private VBox feedContainer;
    private ScrollPane scrollPane;
    private Utente testUser;

    /**
     * Inizializza l'ambiente di test JavaFX.
     * Configura il database e prepara i componenti UI necessari.
     * 
     * @param stage stage JavaFX per il test.
     */
    @Start
    private void start(Stage stage) {
        ensureDatabaseReady();

        feedContainer = new VBox();
        scrollPane = new ScrollPane(feedContainer);

        testUser = new Utente("Test", "User", "testuser", "password", TipoUtente.APPASSIONATO, null);

        feedManager = new FeedManager(testUser, feedContainer, scrollPane);

        Scene scene = new Scene(scrollPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Configura i dati preliminari per i test.
     * Crea un utente di test nel database.
     */
    @BeforeEach
    void setupData() {
        testUser = createTestUser("testuser", TipoUtente.APPASSIONATO);
    }

    /**
     * Pulisce i dati del database dopo ogni test.
     */
    @AfterEach
    void cleanup() throws SQLException {
        // Pulizia gestita da BaseTest, qui vuoto per override se necessario
    }

    /**
     * Verifica il comportamento del feed quando non ci sono post.
     * Si aspetta che venga mostrato un messaggio di stato vuoto.
     */
    @Test
    void testLoadFeed_Empty() {
        javafx.application.Platform.runLater(() -> feedManager.loadFeed());

        WaitForAsyncUtils.waitForFxEvents();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("📭 Nessun Post", isVisible());
    }

    /**
     * Verifica il comportamento del feed quando ci sono post nel database.
     * Si aspetta che i post creati vengano visualizzati nel feed.
     */
    @Test
    void testLoadFeed_WithPosts() {
        createTestPost("Post di Test 1", testUser);
        createTestPost("Post di Test 2", testUser);

        javafx.application.Platform.runLater(() -> feedManager.loadFeed());

        WaitForAsyncUtils.waitForFxEvents();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("Post di Test 1", isVisible());
        verifyThat("Post di Test 2", isVisible());
        verifyThat("🏠 Home Feed", isVisible());
    }
}
