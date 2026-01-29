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

/**
 * Test per la classe {@link SearchManager}.
 * Verifica le funzionalità di ricerca per utenti ed eventi.
 */
@ExtendWith(ApplicationExtension.class)
class SearchManagerTest extends BaseUITest {

    private SearchManager searchManager;
    private VBox feedContainer;
    private ScrollPane scrollPane;
    private Utente testUser;
    private Stage stage;

    /**
     * Inizializza l'interfaccia grafica per i test.
     * Configura il SearchManager e i componenti UI necessari.
     * 
     * @param stage lo stage primario.
     */
    @Start
    private void start(Stage stage) {
        this.stage = stage;
        ensureDatabaseReady();

        feedContainer = new VBox();
        scrollPane = new ScrollPane(feedContainer);

        testUser = new Utente("Test", "User", "testuser_search", "password", TipoUtente.APPASSIONATO, null);

        searchManager = new SearchManager(stage, testUser, feedContainer, scrollPane, () -> {
        });

        Scene scene = new Scene(scrollPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Configura i dati di test prima di ogni metodo.
     * Crea l'utente di test.
     */
    @BeforeEach
    void setupData() {
        testUser = createTestUser("testuser_search", TipoUtente.APPASSIONATO);
    }

    /**
     * Verifica il comportamento della ricerca quando non ci sono risultati.
     * Controlla che venga visualizzato un messaggio appropriato.
     */
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

    /**
     * Verifica la ricerca degli utenti.
     * Controlla che un utente esistente venga trovato e visualizzato.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     * @throws Exception in caso di errore durante l'attesa asincrona.
     */
    @Test
    void testSearch_Users(org.testfx.api.FxRobot robot) throws Exception {
        createTestUser("TrovamiUser", TipoUtente.APPASSIONATO);

        javafx.application.Platform.runLater(() -> searchManager.performSearch("Trovami"));

        org.testfx.util.WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS, () -> {
            return robot.lookup(n -> n instanceof javafx.scene.control.Label
                    && ((javafx.scene.control.Label) n).getText().contains("@TrovamiUser")).tryQuery().isPresent();
        });

        verifyThat("👥 Utenti (1)", isVisible());
    }

    /**
     * Verifica la ricerca degli eventi.
     * Controlla che un evento esistente venga trovato e visualizzato.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     * @throws Exception in caso di errore durante l'attesa asincrona.
     */
    @Test
    void testSearch_Events(org.testfx.api.FxRobot robot) throws Exception {
        Evento e = createTestEvento("Evento Super Bello", "testuser_search");

        javafx.application.Platform.runLater(() -> searchManager.performSearch("Super Bello"));

        org.testfx.util.WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS, () -> {
            return robot.lookup("Evento Super Bello").tryQuery().isPresent();
        });

        verifyThat("📅 Eventi (1)", isVisible());
    }
}
