package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.business.SessionManager;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.Torrefattore;
import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.UI.components.PasswordFieldWithToggler;
import it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl;
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
 * Gestisce l'interfaccia grafica per la Registrazione di nuovi utenti.
 *
 * Supporta la registrazione di utenti standard e di Torrefattori (con campi
 * aggiuntivi).
 * Gestisce la validazione dei campi e il caricamento della foto profilo.
 *
 */
public class RegisterView {

    private final Stage stage;
    private String immagine = null;

    /**
     * Costruttore.
     * 
     * @param stage Lo stage principale.
     */
    public RegisterView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Costruisce la vista di registrazione.
     * 
     * @return Il nodo root della vista.
     */
    public Parent getView() {
        stage.setResizable(false);
        stage.setTitle("Brewhub - Registrazione");
        stage.setWidth(1000);
        stage.setHeight(800);

        StackPane root = new StackPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        root.getStyleClass().add("login-root");

        VBox formBox = new VBox(8);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(650);
        formBox.setMaxHeight(600);
        formBox.getStyleClass().add("form-box");

        Label lblTitolo = new Label("Crea un account");
        lblTitolo.getStyleClass().add("title-label");

        Label lblSottotitolo = new Label("Registrati per iniziare");
        lblSottotitolo.getStyleClass().add("subtitle-label");

        Label lblErrore = new Label();
        lblErrore.setVisible(false);
        lblErrore.getStyleClass().add("error-label");
        lblErrore.setWrapText(true);
        lblErrore.setId("lblErrore");

        HBox persona = new HBox(10);
        persona.setAlignment(Pos.CENTER);

        TextField fldNome = new TextField();
        fldNome.setPromptText("Nome");
        fldNome.getStyleClass().add("text-field");
        fldNome.setPrefWidth(315);
        fldNome.setId("fldNome");

        TextField fldCognome = new TextField();
        fldCognome.setPromptText("Cognome");
        fldCognome.getStyleClass().add("text-field");
        fldCognome.setPrefWidth(315);
        fldCognome.setId("fldCognome");

        persona.getChildren().addAll(fldNome, fldCognome);

        HBox credenziali = new HBox(10);
        credenziali.setAlignment(Pos.CENTER);

        TextField fldUsername = new TextField();
        fldUsername.setPromptText("Username");
        fldUsername.getStyleClass().add("text-field");
        fldUsername.setPrefWidth(315);
        fldUsername.setId("fldUsername");

        PasswordFieldWithToggler fldPassword = new PasswordFieldWithToggler("Password (min 8 car.)");
        fldPassword.setId("fldPasswordContainer");
        fldPassword.setPrefWidth(315);

        credenziali.getChildren().addAll(fldUsername, fldPassword);

        HBox tipoFotoBox = new HBox(10);
        tipoFotoBox.setAlignment(Pos.CENTER);

        ChoiceBox<TipoUtente> cbxTipo = new ChoiceBox<>();
        cbxTipo.getStyleClass().add("choice-box");
        cbxTipo.setPrefWidth(315);
        cbxTipo.setId("cbxTipo");
        cbxTipo.getItems().setAll(TipoUtente.values());
        cbxTipo.getItems().remove(TipoUtente.ADMIN);
        cbxTipo.getItems().remove(TipoUtente.OSPITE);
        cbxTipo.setValue(TipoUtente.CURIOSO);

        HBox fotoBox = new HBox(15);
        fotoBox.setAlignment(Pos.CENTER_LEFT);
        fotoBox.setPrefWidth(315);

        Button btnFoto = new Button("\uD83D\uDCF7 Scegli foto");
        btnFoto.getStyleClass().add("button-accent");
        btnFoto.setId("btnFoto");

        Label lblFile = new Label("Nessuna foto");
        lblFile.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");

        fotoBox.getChildren().addAll(btnFoto, lblFile);

        tipoFotoBox.getChildren().addAll(cbxTipo, fotoBox);

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

        // Assemblaggio Card Base
        formBox.getChildren().addAll(
                lblTitolo, lblSottotitolo, lblErrore,
                persona, credenziali, tipoFotoBox);

        // --- CAMPI TORREFATTORE (Dinamici) ---
        VBox torrefattoreBox = new VBox(10);
        torrefattoreBox.setAlignment(Pos.CENTER);
        torrefattoreBox.setVisible(false);
        torrefattoreBox.setManaged(false);

        // Dati Azienda in Riga
        HBox aziendaRow = new HBox(10);
        aziendaRow.setAlignment(Pos.CENTER);

        TextField fldNomeAzienda = new TextField();
        fldNomeAzienda.setPromptText("Nome Azienda");
        fldNomeAzienda.getStyleClass().add("text-field");
        fldNomeAzienda.setPrefWidth(315);
        fldNomeAzienda.setId("fldNomeAzienda");

        TextField fldPartitaIva = new TextField();
        fldPartitaIva.setPromptText("Partita IVA");
        fldPartitaIva.getStyleClass().add("text-field");
        fldPartitaIva.setPrefWidth(315);
        fldPartitaIva.setId("fldPartitaIva");

        aziendaRow.getChildren().addAll(fldNomeAzienda, fldPartitaIva);

        TextField fldIndirizzo = new TextField();
        fldIndirizzo.setPromptText("Indirizzo");
        fldIndirizzo.getStyleClass().add("text-field");
        fldIndirizzo.setMaxWidth(Double.MAX_VALUE);
        fldIndirizzo.setId("fldIndirizzo");

        TextField fldDescrizione = new TextField();
        fldDescrizione.setPromptText("Descrizione");
        fldDescrizione.getStyleClass().add("text-field");
        fldDescrizione.setMaxWidth(Double.MAX_VALUE);
        fldDescrizione.setId("fldDescrizione");

        torrefattoreBox.getChildren().addAll(aziendaRow, fldIndirizzo, fldDescrizione);
        formBox.getChildren().add(torrefattoreBox);

        // Aggiungo bottoni alla fine
        formBox.getChildren().add(bottoni);

        root.getChildren().add(formBox);

        // --- LOGICA EVENTI ---

        // Listener Cambio Tipo Utente
        cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isTorrefattore = (newVal == TipoUtente.TORREFATTORE);
            torrefattoreBox.setVisible(isTorrefattore);
            torrefattoreBox.setManaged(isTorrefattore);
        });

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

            // Validazione Base
            boolean baseValid = !nome.isBlank() && !cognome.isBlank() && !user.isBlank() && !pw.isBlank()
                    && immagine != null;
            if (!baseValid) {
                lblErrore.setText("\u26A0 Completa tutti i campi standard e carica una foto");
                lblErrore.setVisible(true);
                return; // Stop
            }
            if (pw.length() < 8) {
                lblErrore.setText("\u2717 Password troppo corta (minimo 8 caratteri)");
                lblErrore.setVisible(true);
                return; // Stop
            }

            try {
                if (tipo == TipoUtente.TORREFATTORE) {
                    // Validazione Torrefattore
                    String nomeAz = fldNomeAzienda.getText();
                    String piva = fldPartitaIva.getText();
                    String ind = fldIndirizzo.getText();
                    String desc = fldDescrizione.getText();

                    if (nomeAz.isBlank() || piva.isBlank() || ind.isBlank() || desc.isBlank()) {
                        lblErrore.setText("\u26A0 Completa tutti i campi del Torrefattore");
                        lblErrore.setVisible(true);
                        return;
                    }

                    Torrefattore t = new Torrefattore(nome, cognome, user, pw, immagine, piva, ind, desc, nomeAz);
                    TorrefattoreDAOImpl daoT = new TorrefattoreDAOImpl();
                    daoT.create(t);

                    // Login automatico
                    SessionManager.getInstance().login(t);
                    HomeView home = new HomeView(stage, t);
                    stage.getScene().setRoot(home.getView());

                } else {
                    // Utente Standard
                    Utente nuovoUtente = new Utente(nome, cognome, user, pw, tipo, immagine);
                    UtenteDAOImpl dao = new UtenteDAOImpl();
                    dao.create(nuovoUtente);

                    // Login automatico post-registrazione in Sessione
                    SessionManager.getInstance().login(nuovoUtente);

                    // Navigazione
                    HomeView home = new HomeView(stage, nuovoUtente);
                    stage.getScene().setRoot(home.getView());
                }
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Username già registrato")
                        || ex.getMessage().contains("PRIMARY KEY")
                        || ex.getMessage().contains("Username esistente")) {
                    lblErrore.setText("\u2717 Username già esistente!");
                } else {
                    lblErrore.setText("\u2717 Errore Database: " + ex.getMessage());
                }
                lblErrore.setVisible(true);
                Log.error("Errore durante la registrazione", ex);
            } catch (Exception ex) {
                lblErrore.setText("\u2717 Errore generico: " + ex.getMessage());
                lblErrore.setVisible(true);
                Log.error("Errore durante la registrazione", ex);
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

    /**
     * Apre il selettore file per l'immagine del profilo.
     * Metodo protected per permettere l'override nei test.
     * 
     * @param stage Lo stage principale per la modale.
     * @return Il file selezionato o null.
     */
    protected File openFileChooser(Stage stage) {
        FileChooser fileFoto = new FileChooser();
        fileFoto.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.gif", "*.jpeg"));
        fileFoto.setTitle("Seleziona una foto");
        return fileFoto.showOpenDialog(stage);
    }
}