package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.model.Notifica;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import it.univaq.brewhub.view.BaseUITest;

import javafx.scene.Scene;
import javafx.scene.control.Button;
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
 * Test per la classe {@link NotificationManager}.
 * Verifica la gestione delle notifiche, inclusa la ricezione e la
 * visualizzazione nel menu a tendina.
 */
@ExtendWith(ApplicationExtension.class)
class NotificationManagerTest extends BaseUITest {

    private NotificationManager notificationManager;
    private Button btnNotifiche;
    private Utente testUser;

    /**
     * Inizializza l'ambiente di test JavaFX con un NotificationManager.
     * 
     * @param stage stage JavaFX per il test.
     */
    @Start
    private void start(Stage stage) {
        ensureDatabaseReady();

        btnNotifiche = new Button("🔔");
        btnNotifiche.setId("btnNotifiche");

        testUser = new Utente("Test", "User", "testuser_notif", "password", TipoUtente.APPASSIONATO, null);

        notificationManager = new NotificationManager(stage, testUser, btnNotifiche);
        notificationManager.initialize();

        VBox root = new VBox(btnNotifiche);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Configura i dati preliminari per i test.
     */
    @BeforeEach
    void setupData() {
        testUser = createTestUser("testuser_notif", TipoUtente.APPASSIONATO);
    }

    /**
     * Ferma il NotificationManager dopo ogni test.
     */
    @AfterEach
    void tearDownManager() {
        if (notificationManager != null) {
            notificationManager.shutdown();
        }
    }

    /**
     * Verifica il comportamento quando non ci sono notifiche recenti.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     */
    @Test
    void testNoNotifications(org.testfx.api.FxRobot robot) {
        // Wait for initial poll
        WaitForAsyncUtils.waitForFxEvents();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception ex) {
        }

        // Click button to show dropdown
        robot.clickOn("#btnNotifiche");
        WaitForAsyncUtils.waitForFxEvents();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception ex) {
        }

        // Verify empty message
        verifyThat("Nessuna notifica recente", isVisible());
    }

    /**
     * Verifica la ricezione e visualizzazione di una notifica.
     * 
     * @param robot l'istanza di FxRobot per interagire con la UI.
     * @throws SQLException se si verifica un errore durante la creazione della
     *                      notifica.
     */
    @Test
    void testReceiveNotification(org.testfx.api.FxRobot robot) throws SQLException {
        // Create a notification in DB
        Notifica n = new Notifica(testUser, "Benvenuto su BrewHub!");
        notificaDAO.create(n);

        robot.clickOn("#btnNotifiche");
        WaitForAsyncUtils.waitForFxEvents();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception ex) {
        }

        // Verify content
        verifyThat("Benvenuto su BrewHub!", isVisible());
        // Verify formatted title for welcome
        verifyThat("👋", isVisible());
    }
}
