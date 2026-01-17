package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.business.SessionManager;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.UI.components.PasswordFieldWithToggler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Gestisce l'interfaccia grafica per la registrazione di nuovi utenti.
 * Permette l'inserimento di dati anagrafici, credenziali e foto profilo.
 */
public class RegisterView {

    /** Riferimento allo stage principale dell'applicazione. */
    private final Stage stage;

    /** Percorso dell'immagine del profilo selezionata (se presente). */
    private String immagine = null;

    /**
     * Costruttore della vista di registrazione.
     * 
     * @param stage Lo stage principale.
     */
    public RegisterView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Crea e restituisce il layout principale della schermata di registrazione.
     * Configura il form di input e la logica di validazione e salvataggio.
     *
     * @return Parent Il nodo radice della vista.
     */
    public Parent getView() {
        // Configurazione form properties
        stage.setResizable(false);
        stage.setTitle("Brewhub - Registrazione");
        stage.setWidth(850);
        stage.setHeight(700);

        // --- LAYOUT ---
        StackPane root = new StackPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        root.getStyleClass().add("login-root");

        // Card centrale di Registrazione
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(480);
        formBox.getStyleClass().add("form-box");

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
        lblErrore.setId("lblErrore");

        // Campi Dati Anagrafici
        HBox persona = new HBox(10);
        persona.setAlignment(Pos.CENTER);

        TextField fldNome = new TextField();
        fldNome.setPromptText("Nome");
        fldNome.getStyleClass().add("text-field");
        fldNome.setPrefWidth(200);
        fldNome.setId("fldNome");

        TextField fldCognome = new TextField();
        fldCognome.setPromptText("Cognome");
        fldCognome.getStyleClass().add("text-field");
        fldCognome.setPrefWidth(200);
        fldCognome.setId("fldCognome");

        persona.getChildren().addAll(fldNome, fldCognome);

        // Campo Username
        TextField fldUsername = new TextField();
        fldUsername.setPromptText("Username");
        fldUsername.getStyleClass().add("text-field");
        fldUsername.setMaxWidth(Double.MAX_VALUE);
        fldUsername.setId("fldUsername");

        // Campo Password usando il nuovo componente
        PasswordFieldWithToggler fldPassword = new PasswordFieldWithToggler("Password (min 8 caratteri)");
        fldPassword.setId("fldPasswordContainer");

        // ChoiceBox Tipo Utente
        ChoiceBox<TipoUtente> cbxTipo = new ChoiceBox<>();
        cbxTipo.getStyleClass().add("choice-box");
        cbxTipo.setMaxWidth(Double.MAX_VALUE);
        cbxTipo.setId("cbxTipo");
        cbxTipo.getItems().setAll(TipoUtente.values());
        // Rimuove tipi non selezionabili in registrazione pubblica
        cbxTipo.getItems().remove(TipoUtente.ADMIN);
        cbxTipo.getItems().remove(TipoUtente.OSPITE);
        cbxTipo.setValue(TipoUtente.UTENTE_MEDIO);

        // Sezione Foto
        HBox fotoBox = new HBox(15);
        fotoBox.setAlignment(Pos.CENTER_LEFT);

        Button btnFoto = new Button("\uD83D\uDCF7 Scegli foto");
        btnFoto.getStyleClass().add("button-accent");
        btnFoto.setId("btnFoto");

        Label lblFile = new Label("Nessuna foto");
        lblFile.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");

        fotoBox.getChildren().addAll(btnFoto, lblFile);

        // Bottoni Azione
        VBox bottoni = new VBox(10);
        bottoni.setAlignment(Pos.CENTER);

        Button btnRegistrati = new Button("Registrati");
        btnRegistrati.setDefaultButton(true);
        btnRegistrati.getStyleClass().add("login-btn-primary");
        btnRegistrati.setMaxWidth(Double.MAX_VALUE);
        btnRegistrati.setId("btnRegistrati");

        Button btnAccedi = new Button("Hai già un account? Accedi");
        btnAccedi.getStyleClass().add("login-btn-secondary");
        btnAccedi.setMaxWidth(Double.MAX_VALUE);

        Hyperlink linkOspite = new Hyperlink("Continua come ospite");
        linkOspite.getStyleClass().add("login-guest-link");

        bottoni.getChildren().addAll(btnRegistrati, btnAccedi, linkOspite);

        // Assemblaggio Card
        formBox.getChildren().addAll(
                lblTitolo, lblSottotitolo, lblErrore,
                fotoBox, persona, cbxTipo, fldUsername, fldPassword,
                bottoni);

        root.getChildren().add(formBox);

        // --- LOGICA EVENTI ---

        // Navigazione a Login
        btnAccedi.setOnAction(e -> {
            LoginView login = new LoginView(stage);
            stage.getScene().setRoot(login.getView());
        });

        // Selezione foto profilo
        btnFoto.setOnAction(e -> {
            File fotoSelezionata = openFileChooser(stage);

            if (fotoSelezionata != null) {
                MediaManager.initMediaFolder();
                String percorsoMedia = MediaManager.copyMediaToFolder(fotoSelezionata);

                if (percorsoMedia != null) {
                    immagine = percorsoMedia;
                    lblFile.setText(fotoSelezionata.getName());
                } else {
                    lblFile.setText("Errore caricamento");
                }
            }
        });

        // Submit Registrazione
        btnRegistrati.setOnAction(e -> {
            lblErrore.setVisible(false);

            String nome = fldNome.getText();
            String cognome = fldCognome.getText();
            TipoUtente tipo = cbxTipo.getValue();
            String user = fldUsername.getText();
            String pw = fldPassword.getText();

            // Validazione
            if (nome.isBlank() || cognome.isBlank() || user.isBlank() || pw.isBlank() || immagine == null) {
                lblErrore.setText("\u26A0 Completa tutti i campi e carica una foto");
                lblErrore.setVisible(true);
            } else if (pw.length() < 8) {
                lblErrore.setText("\u2717 Password troppo corta (minimo 8 caratteri)");
                lblErrore.setVisible(true);
            } else {
                try {
                    // Creazione e salvataggio utente tramite DAO
                    Utente nuovoUtente = new Utente(nome, cognome, user, pw, tipo, immagine);
                    UtenteDAOImpl dao = new UtenteDAOImpl();
                    dao.create(nuovoUtente);

                    // Login automatico post-registrazione in Sessione
                    SessionManager.getInstance().login(nuovoUtente);

                    // Navigazione
                    HomeView home = new HomeView(stage, nuovoUtente);
                    stage.getScene().setRoot(home.getView());
                } catch (SQLException ex) {
                    if (ex.getMessage().contains("Username già registrato")
                            || ex.getMessage().contains("PRIMARY KEY")) {
                        lblErrore.setText("\u2717 Username già esistente!");
                    } else {
                        lblErrore.setText("\u2717 Errore Database: " + ex.getMessage());
                    }
                    lblErrore.setVisible(true);
                    Log.error("Errore durante il login automatico", ex);
                } catch (Exception ex) {
                    lblErrore.setText("\u2717 Errore generico: " + ex.getMessage());
                    lblErrore.setVisible(true);
                    Log.error("Errore durante la registrazione", ex);
                }
            }
        });

        // Accesso come ospite
        linkOspite.setOnAction(e -> {
            Utente profilo = new Utente("guest");
            HomeView home = new HomeView(stage, profilo);
            stage.getScene().setRoot(home.getView());
        });

        return root;
    }

    protected File openFileChooser(Stage stage) {
        FileChooser fileFoto = new FileChooser();
        fileFoto.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.gif", "*.jpeg"));
        fileFoto.setTitle("Seleziona una foto");
        return fileFoto.showOpenDialog(stage);
    }
}