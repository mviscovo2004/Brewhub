package it.univaq.brewhub.UI;

import it.univaq.brewhub.Utente;
import it.univaq.brewhub.business.SessionManager;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.UI.components.PasswordFieldWithToggler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Gestisce l'interfaccia grafica per il login utente.
 * Offre funzionalità di accesso, link alla registrazione e accesso ospite.
 */
public class LoginView {

    /** Riferimento allo stage principale per il cambio scena. */
    private final Stage stage;

    /**
     * Costruttore della vista di login.
     * 
     * @param stage Lo stage principale dell'applicazione.
     */
    public LoginView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Crea e restituisce il layout principale della schermata di login.
     * Configura i componenti UI (campi testo, bottoni) e la logica degli eventi.
     *
     * @return Parent Il nodo radice della vista (StackPane).
     */
    public Parent getView() {

        // Configurazione delle proprietà dello stage
        stage.setMaximized(false);
        stage.setHeight(600);
        stage.setWidth(800);
        stage.setResizable(false);
        stage.setTitle("Brewhub - Login");
        stage.centerOnScreen();

        // --- LAYOUT ---
        StackPane root = new StackPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        root.getStyleClass().add("login-root");

        // Card centrale che contiene il form di Login
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxSize(400, 480);
        loginCard.getStyleClass().add("form-box");

        // Titolo e Sottotitolo
        Label lblTitolo = new Label("BrewHub");
        lblTitolo.getStyleClass().add("title-label");
        Label lblSottotitolo = new Label("Accedi al tuo account");
        lblSottotitolo.getStyleClass().add("subtitle-label");

        // Label per mostrare i messaggi di errore
        Label lblErrore = new Label();
        lblErrore.setVisible(false);
        lblErrore.getStyleClass().add("error-label");
        lblErrore.setId("errorLabel"); // ID per test automatici
        lblErrore.setWrapText(true);

        // Campo di input per username
        TextField fldUsername = new TextField();
        fldUsername.setPromptText("Username");
        fldUsername.getStyleClass().add("text-field");
        fldUsername.setId("usernameField");

        // Campo di input per password usando il nuovo componente
        PasswordFieldWithToggler fldPassword = new PasswordFieldWithToggler("Password");
        fldPassword.setId("passwordFieldContainer");

        // Bottone principale di accesso
        Button btnAccedi = new Button("Accedi");
        btnAccedi.setDefaultButton(true); // Attivabile con Enter
        btnAccedi.getStyleClass().add("login-btn-primary");
        btnAccedi.setId("loginButton");
        btnAccedi.setMaxWidth(Double.MAX_VALUE);

        // Bottone per navigare alla registrazione
        Button btnRegistrati = new Button("Non hai un account? Registrati");
        btnRegistrati.getStyleClass().add("login-btn-secondary");
        btnRegistrati.setMaxWidth(Double.MAX_VALUE);

        // Link per accesso rapido come ospite
        Hyperlink linkOspite = new Hyperlink("Continua come ospite");
        linkOspite.getStyleClass().add("login-guest-link");

        // Contenitore per i campi di input
        VBox inputs = new VBox(15);
        inputs.setAlignment(Pos.CENTER);
        inputs.getChildren().addAll(fldUsername, fldPassword);

        // Contenitore per i bottoni di azione
        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(btnAccedi, btnRegistrati, linkOspite);

        // Assemblaggio della card
        loginCard.getChildren().addAll(lblTitolo, lblSottotitolo, lblErrore, inputs, buttons);
        root.getChildren().add(loginCard);

        // --- LOGICA EVENTI ---

        // Gestione click bottone Accedi
        btnAccedi.setOnAction(e -> {
            String user = fldUsername.getText();
            String pw = fldPassword.getText();
            lblErrore.setVisible(false);

            // Validazione input
            if (user.isBlank() || pw.isBlank()) {
                lblErrore.setText("\u26A0 Inserisci username e password");
                lblErrore.setVisible(true);
            } else {
                // Tentativo di login tramite DAO
                UtenteDAOImpl dao = new UtenteDAOImpl();
                try {
                    Utente profilo = dao.login(user, pw);

                    if (profilo == null) {
                        lblErrore.setText("\u2717 Credenziali non valide");
                        lblErrore.setVisible(true);
                        fldPassword.setText("");
                    } else {
                        // Login riuscito: Imposta sessione
                        SessionManager.getInstance().login(profilo);

                        // Cambio scena verso HomeView
                        HomeView home = new HomeView(stage, profilo);
                        stage.getScene().setRoot(home.getView());
                    }
                } catch (Exception ex) {
                    lblErrore.setText("\u2717 Errore Login: " + ex.getMessage());
                    lblErrore.setVisible(true);
                    Log.error("Errore durante il login", ex);
                }
            }
        });

        // Gestione click bottone Registrati
        btnRegistrati.setOnAction(e -> {
            RegisterView register = new RegisterView(stage);
            stage.getScene().setRoot(register.getView());
        });

        // Gestione click link Ospite
        linkOspite.setOnAction(e -> {
            String user = "guest";
            Utente profilo = new Utente(user);
            HomeView home = new HomeView(stage, profilo);
            stage.getScene().setRoot(home.getView());
        });

        return root;
    }
}