package it.univaq.brewhub.view;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Test per la vista degli eventi {@link EventsView}.
 * Verifica il caricamento dell'interfaccia e la visibilità del pulsante di
 * creazione eventi in base al tipo di utente.
 */
@ExtendWith(ApplicationExtension.class)
public class EventsViewTest extends BaseUITest {
    private Utente testUser;

    /**
     * Inizializza la vista degli eventi prima di ogni test.
     * 
     * @param stage stage JavaFX per il test.
     * @throws Exception se si verifica un errore durante l'inizializzazione.
     */
    @Start
    public void start(Stage stage) throws Exception {
        ensureDatabaseReady();
        testUser = new Utente("Test", "User", "testuser", "password", TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(testUser);
        } catch (Exception e) {
            // Ignora eccezioni se l'utente esiste
        }
        EventsView view = new EventsView(testUser);
        Scene scene = new Scene(view, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Verifica il caricamento corretto dell'interfaccia.
     * Controlla la presenza dei titoli e degli header delle sezioni.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
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

    /**
     * Verifica la visibilità del pulsante di creazione per utenti non autorizzati.
     * Un utente APPASSIONATO non dovrebbe vedere il pulsante crea evento.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     * @throws Exception se si verifica un errore durante il test.
     */
    @Test
    public void testCreateButtonVisibilityForTorrefattore(org.testfx.api.FxRobot robot) throws Exception {
        int buttons = robot.lookup("+ Crea Evento").queryAll().size();
        assertEquals(0, buttons, "Utente Appassionato non dovrebbe vedere bottone crea evento");
    }
}
