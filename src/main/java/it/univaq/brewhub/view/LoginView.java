package it.univaq.brewhub.view;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.SessionManager;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.view.components.PasswordFieldWithToggler;
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
 * Gestisce l'interfaccia grafica per il Login.
 * Permette agli utenti di autenticarsi, registrarsi o accedere come ospite.
 */
public class LoginView {

    private final Stage stage;

    /**
     * Costruisce la vista di Login.
     *
     * @param stage Lo stage principale dell'applicazione.
     */
    public LoginView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Costruisce e restituisce il nodo radice della vista di Login.
     * Configura inoltre le dimensioni e il titolo dello stage.
     *
     * @return Il nodo {@link Parent} contenente l'interfaccia.
     */
    public Parent getView() {
        stage.setMaximized(false);
        stage.setHeight(700);
        stage.setWidth(900);
        stage.setResizable(false);
        stage.setTitle("Brewhub - Login");
        stage.centerOnScreen();

        StackPane root = new StackPane();
        try {
            root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            // Ignora se CSS non trovato
        }
        root.getStyleClass().add("login-root");

        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxSize(400, 480);
        loginCard.getStyleClass().add("form-box");

        Label lblTitolo = new Label("BrewHub");
        lblTitolo.getStyleClass().add("title-label");

        Label lblSottotitolo = new Label("Accedi al tuo account");
        lblSottotitolo.getStyleClass().add("subtitle-label");

        Label lblErrore = new Label();
        lblErrore.setVisible(false);
        lblErrore.getStyleClass().add("error-label");
        lblErrore.setId("errorLabel");
        lblErrore.setWrapText(true);

        TextField fldUsername = new TextField();
        fldUsername.setPromptText("Username");
        fldUsername.getStyleClass().add("text-field");
        fldUsername.setId("usernameField");

        PasswordFieldWithToggler fldPassword = new PasswordFieldWithToggler("Password");
        fldPassword.setId("passwordFieldContainer");

        Button btnAccedi = new Button("Accedi");
        btnAccedi.setDefaultButton(true);
        btnAccedi.getStyleClass().add("login-btn-primary");
        btnAccedi.setId("loginButton");
        btnAccedi.setMaxWidth(Double.MAX_VALUE);

        Button btnRegistrati = new Button("Non hai un account? Registrati");
        btnRegistrati.getStyleClass().add("login-btn-secondary");
        btnRegistrati.setMaxWidth(Double.MAX_VALUE);

        Hyperlink linkOspite = new Hyperlink("Continua come ospite");
        linkOspite.getStyleClass().add("login-guest-link");

        VBox inputs = new VBox(15);
        inputs.setAlignment(Pos.CENTER);
        inputs.getChildren().addAll(fldUsername, fldPassword);

        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(btnAccedi, btnRegistrati, linkOspite);

        loginCard.getChildren().addAll(lblTitolo, lblSottotitolo, lblErrore, inputs, buttons);
        root.getChildren().add(loginCard);

        // Gestione Login
        btnAccedi.setOnAction(e -> {
            String user = fldUsername.getText();
            String pw = fldPassword.getText();
            lblErrore.setVisible(false);

            if (user.isBlank() || pw.isBlank()) {
                lblErrore.setText("⚠ Inserisci username e password");
                lblErrore.setVisible(true);
            } else {
                UserService userService = UserService.getInstance();
                try {
                    Utente profilo = userService.login(user, pw);

                    if (profilo == null) {
                        lblErrore.setText("✗ Credenziali non valide");
                        lblErrore.setVisible(true);
                        fldPassword.setText("");
                    } else {
                        SessionManager.getInstance().login(profilo);
                        HomeView home = new HomeView(stage, profilo);
                        stage.getScene().setRoot(home.getView());
                    }
                } catch (BusinessException ex) {
                    lblErrore.setText("✗ Errore Login: " + ex.getMessage());
                    lblErrore.setVisible(true);
                    Log.error("Errore durante il login", ex);
                } catch (Exception ex) {
                    lblErrore.setText("✗ Errore di sistema: " + ex.getMessage());
                    lblErrore.setVisible(true);
                    Log.error("Errore generico login", ex);
                }
            }
        });

        // Navigazione a Registrazione
        btnRegistrati.setOnAction(e -> {
            RegisterView register = new RegisterView(stage);
            stage.getScene().setRoot(register.getView());
        });

        // Accesso Ospite
        linkOspite.setOnAction(e -> {
            String user = "guest";
            Utente profilo = new Utente(user);
            HomeView home = new HomeView(stage, profilo);
            stage.getScene().setRoot(home.getView());
        });

        return root;
    }
}
