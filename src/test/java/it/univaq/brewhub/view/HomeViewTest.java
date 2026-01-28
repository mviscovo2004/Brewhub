package it.univaq.brewhub.view;


import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import java.sql.SQLException;
import java.util.List;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test dell'interfaccia utente per la HomeView.
 *
 * Verifica le funzionalità principali della pagina Home, come la creazione di
 * nuovi post
 * e la loro visualizzazione nel feed. Utilizza TestFX per simulare
 * l'interazione utente.
 *
 */
@ExtendWith(ApplicationExtension.class)
class HomeViewTest extends BaseUITest {
    private final String TEST_USER = "testUserHome";
    private final String POST_TITLE = "Titolo TestFX";
    private Utente testUtente;

    /**
     * Inizializza l'applicazione JavaFX per il test.
     * Configura il database e mostra la HomeView.
     * 
     * @param stage Lo stage principale di JavaFX.
     */
    @Start
    private void start(Stage stage) {
        ensureDatabaseReady();
        if (testUtente == null) {
            testUtente = createTestUser(TEST_USER, TipoUtente.APPASSIONATO);
        }
        HomeView view = new HomeView(stage, testUtente);
        Scene scene = new Scene(view.getView());
        stage.setScene(scene);

        stage.setWidth(1200);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.setFullScreen(true);
        stage.show();
        stage.toFront();
    }

    /**
     * Prepara i dati del database prima di ogni test.
     * Assicura che l'utente di test esista.
     */
    @BeforeEach
    void setupDB() {
        createTestUser(TEST_USER, TipoUtente.APPASSIONATO);
    }

    /**
     * Pulisce i dati del database dopo ogni test.
     * Rimuove i post creati e l'utente di test.
     * 
     * @throws SQLException se si verifica un errore durante la pulizia.
     */
    @AfterEach
    void cleanup() throws SQLException {
        List<Post> posts = postDAO.findAll();
        for (Post p : posts) {
            if (p.getAutore().getUsername().equals(TEST_USER)) {
                postDAO.delete(p.getId());
            }
        }
        utenteDAO.delete(TEST_USER);
    }

    /**
     * Test: Creazione di un post di testo.
     *
     * Simula il click sul bottone "Nuovo Post", l'inserimento di titolo e
     * contenuto,
     * e la pubblicazione. Verifica che il post sia visibile nel feed.
     *
     * 
     * @param robot Il robot TestFX per interagire con la UI.
     */
    @Test
    void testCreazionePostTesto(FxRobot robot) {
        verifyThat("#btnNewPost", hasText("\u2795 Nuovo Post"));
        robot.clickOn("#btnNewPost");
        // Sostituito Thread.sleep con attesa asincrona
        WaitForAsyncUtils.waitForFxEvents();
        try {
            Thread.sleep(1000); // Wait esplicito per rendering modale sotto carico
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verifyThat("#fldTitolo", isVisible()); // Attende che la modale sia visibile
        robot.clickOn("#fldTitolo").write(POST_TITLE);
        robot.clickOn("#postArea").write("Contenuto del post creato da TestFX.");
        robot.clickOn("#publishBtn");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(POST_TITLE, isVisible());
    }
}
