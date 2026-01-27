package it.univaq.brewhub.UI;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.model.Sfida;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test per la vista delle sfide {@link SfideView}.
 *
 * Verifica la struttura dell'interfaccia, il contenuto della lista sfide
 * e la visibilità del pulsante di creazione per i torrefattori.
 *
 */
@ExtendWith(ApplicationExtension.class)
class SfideViewTest extends BaseUITest {

    private Utente testUser;
    private final String TEST_USERNAME = "sfidaUserTest";
    private final String CHALLENGE_TITLE = "Latte Art Contest";

    @Start
    private void start(Stage stage) {
        ensureDatabaseReady();
        if (testUser == null) {
            testUser = new Utente("Nome", "Cognome", TEST_USERNAME, "pass", TipoUtente.TORREFATTORE, null);
        }

        try {
            utenteDAO.create(testUser);
            Sfida s = new Sfida(CHALLENGE_TITLE, "Fai la foglia", "1KG Caffè", LocalDate.now().plusDays(5).toString(),
                    TEST_USERNAME);
            sfidaDAO.create(s);
        } catch (SQLException e) {
        }

        SfideView view = new SfideView(testUser);
        Scene scene = new Scene(view, 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    void testInterfaceStructure(FxRobot robot) {
        boolean titleExists = robot.lookup(".section-title").queryAll().stream()
                .anyMatch(node -> node instanceof Label && ((Label) node).getText().contains("Sfide"));
        assertTrue(titleExists, "Titolo sezione non trovato");
        boolean activeExists = robot
                .lookup(node -> node instanceof Label && ((Label) node).getText().contains("Sfide Attive")).tryQuery()
                .isPresent();
        assertTrue(activeExists, "Sezione 'Sfide Attive' non trovata");
        boolean closedExists = robot
                .lookup(node -> node instanceof Label && ((Label) node).getText().contains("Sfide Concluse")).tryQuery()
                .isPresent();
        assertTrue(closedExists, "Sezione 'Sfide Concluse' non trovata");
    }

    @Test
    void testChallengeListContent(FxRobot robot) {
        verifyThat(CHALLENGE_TITLE, isVisible());
    }

    @Test
    void testCreateButtonVisibleForTorrefattore(FxRobot robot) {
        verifyThat("+ Crea Sfida", isVisible());
    }
}
