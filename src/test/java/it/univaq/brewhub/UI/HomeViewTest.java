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

import it.univaq.brewhub.Post;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;

import java.sql.SQLException;
import java.util.List;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
class HomeViewTest {

    private final String TEST_USER = "testUserHome";
    private final String POST_TITLE = "Titolo TestFX";
    private Utente testUtente;

    @Start
    private void start(Stage stage) {
        // Setup Utente (creato in @BeforeEach, ma qui serve l'oggetto)
        // Nota: @Start gira PRIMA di @BeforeEach in alcuni contesti TestFX,
        // ma per sicurezza creiamo l'oggetto qui se nullo o lo passiamo.
        // In JUnit 5 + TestFX, @Start è il punto di ingresso JavaFX.

        // Creiamo un utente dummy in memoria per la vista,
        // ma deve esistere nel DB per il DAO.
        if (testUtente == null) {
            testUtente = new Utente("Nome", "Cognome", TEST_USER, "pass", TipoUtente.APPASSIONATO, null);
        }

        HomeView view = new HomeView(stage, testUtente);
        Scene scene = new Scene(view.getView());
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setupDB() throws SQLException {
        // Inizializza il DB (crea tabelle mancanti come 'likes')
        it.univaq.brewhub.DatabaseManager.init();

        UtenteDAOImpl dao = new UtenteDAOImpl();
        // Assicuriamoci che l'utente esista nel DB
        try {
            dao.create(testUtente);
        } catch (SQLException e) {
            // Se esiste già ok
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        PostDAOImpl postDAO = new PostDAOImpl();
        UtenteDAOImpl utenteDAO = new UtenteDAOImpl();

        // Cerca e cancella i post dell'utente test
        List<Post> posts = postDAO.findAll();
        for (Post p : posts) {
            if (p.getAutore().getUsername().equals(TEST_USER)) {
                postDAO.delete(p.getId());
            }
        }

        // Cancella utente
        utenteDAO.delete(TEST_USER);
    }

    @Test
    void testCreazionePostTesto(FxRobot robot) {
        // Verifica presenza bottone nuovo post
        verifyThat("#btnNewPost", hasText("\u2795 Nuovo Post"));

        // Apri form creazione post
        robot.clickOn("#btnNewPost");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Compila form
        robot.clickOn("#fldTitolo").write(POST_TITLE);
        robot.clickOn("#postArea").write("Contenuto del post creato da TestFX.");

        // Click Pubblica
        robot.clickOn("#publishBtn");

        // Verifica che il post sia apparso nel feed.
        // Cerchiamo nello specifico un nodo con il testo del titolo.
        // verifyThat(".post-title", hasText(POST_TITLE)); // Meno robusto se ci sono
        // altri post
        verifyThat(POST_TITLE, isVisible());
    }
}
