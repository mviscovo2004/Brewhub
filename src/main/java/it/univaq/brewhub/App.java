package it.univaq.brewhub;

import it.univaq.brewhub.UI.LoginView;
import it.univaq.brewhub.UI.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale dell'applicazione BrewHub.
 * Estende la classe Application di JavaFX e gestisce l'avvio dell'interfaccia
 * grafica.
 */
public class App extends Application {

    /**
     * Metodo di avvio dell'applicazione JavaFX.
     * Inizializza la LoginView, configura la scena principale, applica il tema
     * e mostra la finestra (Stage).
     *
     * @param stage Lo stage primario per questa applicazione, nel quale la scena
     *              dell'app viene impostata.
     */
    @Override
    public void start(Stage stage) {
        // Creazione della vista di login
        LoginView login = new LoginView(stage);

        // Creazione della scena con dimensioni fisse
        Scene scene = new Scene(login.getView(), 400, 500);

        // Applica il tema personalizzato (caffè)
        ThemeManager.applyTheme(scene);

        // Configurazione e visualizzazione dello stage
        stage.setScene(scene);
        stage.setTitle("BrewHub"); // Imposta titolo opzionale
        stage.show();
    }

    /**
     * Entry point principale dell'applicazione Java.
     * Inizializza il database e lancia l'applicazione JavaFX.
     *
     * @param args Argomenti da riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        // Inizializzazione delle tabelle del database se non esistono
        DatabaseManager.init();

        // Avvio del ciclo di vita JavaFX
        launch();
    }
}