package it.univaq.brewhub;

import it.univaq.brewhub.UI.LoginView;
import it.univaq.brewhub.UI.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale dell'applicazione BrewHub.
 * <p>
 * Estende {@link Application} di JavaFX e funge da punto di ingresso.
 * Si occupa di inizializzare il database e mostrare la prima schermata (Login).
 * </p>
 */
public class App extends Application {

    /**
     * Metodo di avvio dell'applicazione JavaFX.
     * <p>Configura lo stage primario, applica il tema e mostra la vista di login.</p>
     * 
     * @param stage Lo stage (finestra) principale fornito dal sistema.
     */
    @Override
    public void start(Stage stage) {
        try {
            LoginView login = new LoginView(stage);
            Scene scene = new Scene(login.getView());
            ThemeManager.applyTheme(scene);
            
            stage.setScene(scene);
            stage.setTitle("BrewHub"); 
            stage.show();
        } catch (Throwable t) {
            // Gestione errori critici all'avvio per evitare crash silenziosi
            t.printStackTrace();
            System.err.println("CRITICAL ERROR STARTING APP: " + t.getMessage());
        }
    }

    /**
     * Punto di ingresso standard (main).
     * <p>Inizializza il database e lancia il runtime JavaFX.</p>
     * 
     * @param args Argomenti da riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        // Inizializza il database (creazione tabelle, migrazioni)
        DatabaseManager.init();
        
        // Avvia l'interfaccia grafica
        launch();
    }
}
