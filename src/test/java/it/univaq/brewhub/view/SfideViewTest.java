package it.univaq.brewhub.view;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import it.univaq.brewhub.model.Sfida;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test per la vista delle sfide {@link SfideView}.
 * Verifica la struttura dell'interfaccia, il contenuto della lista sfide e la
 * visibilità del pulsante di creazione per i torrefattori.
 */
@ExtendWith(ApplicationExtension.class)
class SfideViewTest extends BaseUITest {

    private Utente testUser;
    private final String TEST_USERNAME = "sfidaUserTest";
    private final String CHALLENGE_TITLE = "Latte Art Contest";

    /**
     * Inizializza la vista delle sfide prima di ogni test.
     * Crea un utente torrefattore e una sfida di test se necessario.
     * 
     * @param stage stage JavaFX per il test.
     */
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
            // Ignora se la sfida o l'utente esistono già
        }

        SfideView view = new SfideView(testUser);
        Scene scene = new Scene(view, 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    /**
     * Verifica la struttura dell'interfaccia.
     * Controlla la presenza del titolo della sezione e delle categorie "Sfide
     * Attive" e "Sfide Concluse".
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
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

    /**
     * Verifica il contenuto della lista delle sfide.
     * Controlla che le sfide create siano visibili nella lista.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testChallengeListContent(FxRobot robot) {
        verifyThat(CHALLENGE_TITLE, isVisible());
    }

    /**
     * Verifica la visibilità del pulsante di creazione per gli utenti Torrefattori.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testCreateButtonVisibleForTorrefattore(FxRobot robot) {
        verifyThat("+ Crea Sfida", isVisible());
    }
}
