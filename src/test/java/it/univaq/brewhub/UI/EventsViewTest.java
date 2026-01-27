package it.univaq.brewhub.UI;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Test per la vista degli eventi {@link EventsView}.
 *
 * Verifica il caricamento dell'interfaccia e la visibilità del pulsante
 * di creazione eventi in base al tipo di utente.
 *
 */
@ExtendWith(ApplicationExtension.class)
public class EventsViewTest extends BaseUITest {
    private Utente testUser;

    @Start
    public void start(Stage stage) throws Exception {
        ensureDatabaseReady();
        testUser = new Utente("Test", "User", "testuser", "password", TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(testUser);
        } catch (Exception e) {
        }
        EventsView view = new EventsView(testUser);
        Scene scene = new Scene(view, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testInterfaceLoad(org.testfx.api.FxRobot robot) {
        int size = robot.lookup(".section-title").queryAll().size();
        assertTrue(size > 0, "Dovrebbe esserci almeno un titolo di sezione (Eventi)");
        int headers = robot.lookup(".section-header").queryAll().size();
        assertTrue(headers > 0, "Dovrebbero esserci headers per gli eventi (in programma/passati)");
        boolean hasText = robot.lookup(".section-header").queryAll().stream()
                .anyMatch(node -> node instanceof Label && ((Label) node).getText().contains("Eventi"));
        assertTrue(hasText, "Almeno un header deve contenere testo identificabile 'Eventi'");
    }

    @Test
    public void testCreateButtonVisibilityForTorrefattore(org.testfx.api.FxRobot robot) throws Exception {
        int buttons = robot.lookup("+ Crea Evento").queryAll().size();
        assertEquals(0, buttons, "Utente Appassionato non dovrebbe vedere bottone crea evento");
    }
}
