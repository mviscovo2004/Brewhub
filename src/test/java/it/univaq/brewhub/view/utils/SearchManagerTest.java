package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.model.Evento;

import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import it.univaq.brewhub.view.BaseUITest;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
class SearchManagerTest extends BaseUITest {

    private SearchManager searchManager;
    private VBox feedContainer;
    private ScrollPane scrollPane;
    private Utente testUser;
    private Stage stage;

    @Start
    private void start(Stage stage) {
        this.stage = stage;
        ensureDatabaseReady();

        feedContainer = new VBox();
        scrollPane = new ScrollPane(feedContainer);

        // Dummy user
        testUser = new Utente("Test", "User", "testuser_search", "password", TipoUtente.APPASSIONATO, null);

        // Initialize SearchManager
        searchManager = new SearchManager(stage, testUser, feedContainer, scrollPane, () -> {
        });

        Scene scene = new Scene(scrollPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setupData() {
        testUser = createTestUser("testuser_search", TipoUtente.APPASSIONATO);
    }

    @Test
    void testSearch_NoResults() {
        javafx.application.Platform.runLater(() -> searchManager.performSearch("NonEsistoAssolutamente"));

        WaitForAsyncUtils.waitForFxEvents();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception ex) {
        }
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("😔 Nessun risultato trovato per \"NonEsistoAssolutamente\"", isVisible());
    }

    @Test
    void testSearch_Users(org.testfx.api.FxRobot robot) throws Exception {
        // Create another user to find
        createTestUser("TrovamiUser", TipoUtente.APPASSIONATO);

        javafx.application.Platform.runLater(() -> searchManager.performSearch("Trovami"));

        // Wait for async results
        org.testfx.util.WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS, () -> {
            return robot.lookup(n -> n instanceof javafx.scene.control.Label
                    && ((javafx.scene.control.Label) n).getText().contains("@TrovamiUser")).tryQuery().isPresent();
        });

        verifyThat("👥 Utenti (1)", isVisible());
    }

    @Test
    void testSearch_Events(org.testfx.api.FxRobot robot) throws Exception {
        // Create event
        Evento e = createTestEvento("Evento Super Bello", "testuser_search");

        javafx.application.Platform.runLater(() -> searchManager.performSearch("Super Bello"));

        // Wait for async results
        org.testfx.util.WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS, () -> {
            return robot.lookup("Evento Super Bello").tryQuery().isPresent();
        });

        verifyThat("📅 Eventi (1)", isVisible());
    }

    // Since SfidaDAO creation helper is missing in BaseTest or not public/standard,
    // we might skip Challenge test or need to implement createTestSfida if
    // possible.
    // BaseTest has sfidaDAO field. Let's check BaseTest for createTestSfida.
    // I recall reading BaseTest and it had createTestEvento but I don't recall
    // createTestSfida.
    // Searching BaseTest content again...
    /*
     * Checking cached BaseTest content from previous turn...
     * Lines 32: protected it.univaq.brewhub.dao.impl.SfidaDAOImpl sfidaDAO;
     * It does NOT have createTestSfida. It has createTestUser, Torrefattore, Post,
     * Categoria, Evento, Gruppo, Recensione.
     * So I can manually use sfidaDAO to create one in the test if needed.
     */
}
