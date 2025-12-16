package it.univaq.brewhub.UI;

// Importazioni JavaFX e classi del progetto
import java.io.File;
import java.sql.SQLException;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.MediaManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Vista per la Registrazione
public class RegisterView {

    // Riferimento allo stage principale
    private final Stage stage;

    // Percorso immagine profilo
    private String immagine = null;

    // Costruttore
    public RegisterView(Stage stage) {
        this.stage = stage;
    }

    // Metodo per ottenere la vista della Registrazione
    public Parent getView() {
        // Configurazione stage
        stage.setResizable(false);
        stage.setTitle("Brewhub - Registrazione");
        stage.setWidth(600);
        stage.setHeight(750);

        // --- LAYOUT ---
        StackPane root = new StackPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Card centrale di Registrazione
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(480);
        formBox.getStyleClass().add("form-box"); // Stile Card CSS

        // Titolo
        Label lblTitolo = new Label("Crea un account");
        lblTitolo.getStyleClass().add("title-label");

        // Sottotitolo
        Label lblSottotitolo = new Label("Registrati per iniziare");
        lblSottotitolo.getStyleClass().add("subtitle-label");

        // Messaggio Errore
        Label lblErrore = new Label();
        lblErrore.setVisible(false);
        lblErrore.getStyleClass().add("error-label");
        lblErrore.setWrapText(true);

        // Campi Dati
        HBox persona = new HBox(10);
        persona.setAlignment(Pos.CENTER);

        // Campo Nome
        TextField fldNome = new TextField();
        fldNome.setPromptText("Nome");
        fldNome.getStyleClass().add("text-field");
        fldNome.setPrefWidth(200);

        // Campo Cognome
        TextField fldCognome = new TextField();
        fldCognome.setPromptText("Cognome");
        fldCognome.getStyleClass().add("text-field");
        fldCognome.setPrefWidth(200);

        // Aggiunta campi al box persona
        persona.getChildren().addAll(fldNome, fldCognome);

        // Campo Username
        TextField fldUsername = new TextField();
        fldUsername.setPromptText("Username");
        fldUsername.getStyleClass().add("text-field");
        fldUsername.setMaxWidth(Double.MAX_VALUE);

        // Campo Password
        PasswordField fldPassword = new PasswordField();
        fldPassword.setPromptText("Password (min 8 caratteri)");
        fldPassword.getStyleClass().add("password-field");
        fldPassword.setMaxWidth(Double.MAX_VALUE);

        // ChoiceBox Tipo Utente
        ChoiceBox<TipoUtente> cbxTipo = new ChoiceBox<>();
        cbxTipo.getStyleClass().add("choice-box");
        cbxTipo.setMaxWidth(Double.MAX_VALUE);
        cbxTipo.getItems().setAll(TipoUtente.values());
        cbxTipo.getItems().remove(TipoUtente.ADMIN);
        cbxTipo.getItems().remove(TipoUtente.OSPITE);
        cbxTipo.setValue(TipoUtente.UTENTE_MEDIO);

        // Sezione Foto
        HBox fotoBox = new HBox(15);
        fotoBox.setAlignment(Pos.CENTER_LEFT);

        // Bottone scegli foto
        Button btnFoto = new Button("📷 Scegli foto");
        btnFoto.getStyleClass().add("button-accent"); // Stile arancione/rame

        // Label nome file foto
        Label lblFile = new Label("Nessuna foto");
        lblFile.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");

        // Aggiunta componenti al box foto
        fotoBox.getChildren().addAll(btnFoto, lblFile);

        // Bottoni Azione
        VBox bottoni = new VBox(10);
        bottoni.setAlignment(Pos.CENTER);

        // Bottone registrati
        Button btnRegistrati = new Button("Registrati");
        btnRegistrati.setDefaultButton(true);
        btnRegistrati.getStyleClass().add("button-success");
        btnRegistrati.setMaxWidth(Double.MAX_VALUE);

        // Bottone accedi
        Button btnAccedi = new Button("Hai già un account? Accedi");
        btnAccedi.getStyleClass().add("button-secondary");
        btnAccedi.setMaxWidth(Double.MAX_VALUE);

        // Link continua come ospite
        Hyperlink linkOspite = new Hyperlink("Continua come ospite");
        linkOspite.getStyleClass().add("hyperlink");

        // Aggiunta bottoni al box azioni
        bottoni.getChildren().addAll(btnRegistrati, btnAccedi, linkOspite);

        // Assemblaggio Card
        formBox.getChildren().addAll(
                lblTitolo, lblSottotitolo, lblErrore,
                fotoBox, persona, cbxTipo, fldUsername, fldPassword,
                bottoni);

        // Aggiunta form box al root
        root.getChildren().add(formBox);

        // --- LOGICA EVENTI ---
        // Azione bottone accedi
        btnAccedi.setOnAction(e -> {

            // Passa alla vista di Login
            LoginView login = new LoginView(stage);
            stage.getScene().setRoot(login.getView());
        });

        // Azione bottone scegli foto
        btnFoto.setOnAction(e -> {
            
            // Apertura FileChooser
            FileChooser fileFoto = new FileChooser();
            fileFoto.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.gif", "*.jpeg"));
            fileFoto.setTitle("Seleziona una foto");

            // Selezione file
            File fotoSelezionata = fileFoto.showOpenDialog(stage);

            // Se un file è stato selezionato
            if (fotoSelezionata != null) {

                // Copia file nella cartella media dell'applicazione
                MediaManager.initMediaFolder();
                String percorsoMedia = MediaManager.copyMediaToFolder(fotoSelezionata);

                // Se il copia è andato a buon fine
                if (percorsoMedia != null) {

                    // Aggiorna percorso immagine e label
                    immagine = percorsoMedia;
                    lblFile.setText(fotoSelezionata.getName());
                } else {

                    // Mostra errore
                    lblFile.setText("Errore caricamento");
                }
            }
        });

        // Azione bottone registrati
        btnRegistrati.setOnAction(e -> {
            // Reset messaggio errore
            lblErrore.setVisible(false);

            // Recupero dati
            String nome = fldNome.getText();
            String cognome = fldCognome.getText();
            TipoUtente tipo = cbxTipo.getValue();
            String user = fldUsername.getText();
            String pw = fldPassword.getText();

            // Controllo campi vuoti e validità password
            if (nome.isBlank() || cognome.isBlank() || user.isBlank() || pw.isBlank() || immagine == null) {

                // Mostra errore
                lblErrore.setText("⚠ Completa tutti i campi e carica una foto");
                lblErrore.setVisible(true);
            } else if (pw.length() < 8) {

                // Mostra errore
                lblErrore.setText("✗ Password troppo corta (minimo 8 caratteri)");
                lblErrore.setVisible(true);
            } else {

                // Tentativo registrazione
                try {

                    // Crea nuovo utente e registra nel DB
                    Utente nuovoUtente = new Utente(nome, cognome, user, pw, tipo, immagine);
                    Utente dao = new Utente(); 
                    dao.registraUtente(nuovoUtente);

                    // Registrazione riuscita, vai alla Home
                    HomeView home = new HomeView(stage, nuovoUtente);
                    stage.getScene().setRoot(home.getView());
                } catch (SQLException ex) {
                    // Gestione errore di username già esistente
                    if (ex.getMessage().contains("Username già registrato")
                            || ex.getMessage().contains("PRIMARY KEY")) {

                        // Mostra errore
                        lblErrore.setText("✗ Username già esistente!");
                    } else {

                        // Mostra errore generico DB
                        lblErrore.setText("✗ Errore Database: " + ex.getMessage());
                    }
                    lblErrore.setVisible(true);
                    ex.printStackTrace();
                } catch (Exception ex) {

                    // Mostra errore generico
                    lblErrore.setText("✗ Errore generico: " + ex.getMessage());
                    lblErrore.setVisible(true);
                    ex.printStackTrace();
                }
            }
        });

        // Azione link continua come ospite
        linkOspite.setOnAction(e -> {

            // Login come guest
            Utente profilo = new Utente("guest");
            HomeView home = new HomeView(stage, profilo);
            stage.getScene().setRoot(home.getView());
        });

        // Ritorna il root della vista
        return root;
    }
}