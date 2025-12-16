package it.univaq.brewhub.UI;

// Importazioni JavaFX e classi del progetto
import it.univaq.brewhub.Utente;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Vista per il Login
public class LoginView {
    // Riferimento allo stage principale
    private final Stage stage;
    
    // Costruttore
    public LoginView(Stage stage) {
        this.stage = stage;
    }
    
    // Metodo per ottenere la vista del Login
    public Parent getView() {

        // Configurazione stage
        stage.setHeight(600);
        stage.setWidth(800);
        stage.setResizable(false);
        stage.setTitle("Brewhub - Login");
        stage.centerOnScreen();
        
        // --- LAYOUT ---
        StackPane root = new StackPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        // Card centrale di Login
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxSize(400, 480);
        loginCard.getStyleClass().add("form-box");
        
        // Titolo e Sottotitolo
        Label lblTitolo = new Label("BrewHub");
        lblTitolo.getStyleClass().add("title-label");
        Label lblSottotitolo = new Label("Accedi al tuo account");
        lblSottotitolo.getStyleClass().add("subtitle-label");
        
        // Messaggio Errore
        Label lblErrore = new Label();
        lblErrore.setVisible(false);
        lblErrore.getStyleClass().add("error-label");
        lblErrore.setWrapText(true);
        
        // Campo usename
        TextField fldUsername = new TextField();
        fldUsername.setPromptText("Username");
        fldUsername.getStyleClass().add("text-field");
        
        // Campo password
        PasswordField fldPassword = new PasswordField();
        fldPassword.setPromptText("Password");
        fldPassword.getStyleClass().add("password-field");

        // Bottone accedi
        Button btnAccedi = new Button("Accedi");
        btnAccedi.setDefaultButton(true);
        btnAccedi.getStyleClass().add("button-primary"); // Stile dal CSS
        btnAccedi.setMaxWidth(Double.MAX_VALUE); // Bottone largo
        
        // Bottone registrati
        Button btnRegistrati = new Button("Non hai un account? Registrati");
        btnRegistrati.getStyleClass().add("button-secondary");
        btnRegistrati.setMaxWidth(Double.MAX_VALUE);
        
        // Link continua come ospite
        Hyperlink linkOspite = new Hyperlink("Continua come ospite");
        linkOspite.getStyleClass().add("hyperlink");
        
        // Contenitore input
        VBox inputs = new VBox(15);
        inputs.setAlignment(Pos.CENTER);
        inputs.getChildren().addAll(fldUsername, fldPassword);

        // Contenitore bottoni
        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(btnAccedi, btnRegistrati, linkOspite);
        
        // Aggiunta componenti alla card di login
        loginCard.getChildren().addAll(lblTitolo, lblSottotitolo, lblErrore, inputs, buttons);
        
        // Aggiunta card al root
        root.getChildren().add(loginCard);
        
        
        // --- LOGICA ---
        // Azione bottone accedi
        btnAccedi.setOnAction(e -> {
            // Recupero credenziali
            String user = fldUsername.getText();
            String pw = fldPassword.getText();
            lblErrore.setVisible(false);
            
            // Controllo campi vuoti
            if (user.isBlank() || pw.isBlank()) {
                // Mostra errore
                lblErrore.setText("⚠ Inserisci username e password");
                lblErrore.setVisible(true);
            } else {
                // Tentativo login
                Utente profilo = new Utente().login(user, pw);

                // Verifica risultato login
                if (profilo == null) {
                    // Mostra errore
                    lblErrore.setText("✗ Credenziali non valide");
                    lblErrore.setVisible(true);
                    fldPassword.clear();
                } else {
                    // Login riuscito, vai alla Home
                    HomeView home = new HomeView(stage, profilo);
                    stage.getScene().setRoot(home.getView());
                }
            }
        });
        
        // Azione bottone registrati
        btnRegistrati.setOnAction(e -> {
            // Vai alla vista di registrazione
            RegisterView register = new RegisterView(stage);
            stage.getScene().setRoot(register.getView());
        });
        
        // Azione link continua come ospite
        linkOspite.setOnAction(e -> {
            // Login come guest
            String user = "guest";
            Utente profilo = new Utente(user);
            
            // Vai alla Home come ospite
            HomeView home = new HomeView(stage, profilo);
            stage.getScene().setRoot(home.getView());
        });
        
        // Ritorna il root della vista
        return root;
    }
}