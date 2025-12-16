package it.univaq.brewhub.UI;

// Importazioni JavaFX e classi del progetto
import java.io.File;
import java.sql.SQLException;
import java.util.Optional;
import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.Utente;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Vista per il Profilo Utente
public class ProfileView {

    // Riferimento allo stage principale e all'utente
    private final Stage stage;
    private final Utente utente;

    // Percorso della nuova foto selezionata
    private String nuovoPercorsoFoto = null;

    // Costruttore
    public ProfileView(Stage stage, Utente utente) {
        this.stage = stage;
        this.utente = utente;
        this.nuovoPercorsoFoto = utente.getFotoProfilo();
    }

    // Metodo per ottenere la vista del Profilo
    public Parent getView() {

        // Contenitore principale
        StackPane root = new StackPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Box del form
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(450);
        formBox.getStyleClass().add("form-box");

        // Titolo
        Label lblTitolo = new Label("Il mio Profilo");
        lblTitolo.getStyleClass().add("title-label");

        // Foto profilo
        ImageView imgView = new ImageView();
        imgView.setFitWidth(120);
        imgView.setFitHeight(120);

        // Carica l'anteprima della foto profilo attuale
        caricaAnteprimaFoto(imgView, utente.getFotoProfilo());

        // Ritaglia l'immagine in un cerchio
        Circle clip = new Circle(60, 60, 60);
        imgView.setClip(clip);

        // Bottone per cambiare foto
        Button btnCambiaFoto = new Button("📷 Cambia Foto");
        btnCambiaFoto.getStyleClass().add("button-secondary");

        // Azione bottone cambia foto
        btnCambiaFoto.setOnAction(e -> {

            // Apri file chooser per selezionare nuova immagine
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Scegli nuova foto profilo");
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg"));
            
            // Mostra la finestra di dialogo
            File file = fileChooser.showOpenDialog(stage);

            // Se è stata selezionata un'immagine, copiala nella cartella media e aggiorna l'anteprima
            if (file != null) {

                // Copia l'immagine nella cartella media del progetto
                MediaManager.initMediaFolder();
                String path = MediaManager.copyMediaToFolder(file);

                // Aggiorna il percorso della nuova foto e l'anteprima
                if (path != null) {
                    nuovoPercorsoFoto = path;
                    caricaAnteprimaFoto(imgView, file.getAbsolutePath());
                }
            }
        });

        // Box foto e bottone
        VBox fotoBox = new VBox(10, imgView, btnCambiaFoto);
        fotoBox.setAlignment(Pos.CENTER);

        // Campi di input
        VBox inputs = new VBox(8);
        inputs.setAlignment(Pos.CENTER_LEFT);

        // Campo username (non modificabile)
        Label lblUser = new Label("Username:");
        lblUser.getStyleClass().add("label");
        TextField fldUsername = new TextField(utente.getUsername());
        fldUsername.setEditable(false);
        fldUsername.getStyleClass().add("text-field");
        fldUsername.setStyle("-fx-opacity: 0.7;");

        // Campo nome
        Label lblNome = new Label("Nome:");
        lblNome.getStyleClass().add("label");
        TextField fldNome = new TextField(utente.getNome());
        fldNome.getStyleClass().add("text-field");

        // Campo cognome
        Label lblCognome = new Label("Cognome:");
        lblCognome.getStyleClass().add("label");
        TextField fldCognome = new TextField(utente.getCognome());
        fldCognome.getStyleClass().add("text-field");

        // Campo nuova password
        Label lblPw = new Label("Sicurezza:");
        lblPw.getStyleClass().add("label");
        PasswordField fldNuovaPass = new PasswordField();
        fldNuovaPass.setPromptText("Nuova Password (lascia vuoto per mantenere)");
        fldNuovaPass.getStyleClass().add("password-field");

        // Aggiunta campi al contenitore
        inputs.getChildren().addAll(lblUser, fldUsername, lblNome, fldNome, lblCognome, fldCognome, lblPw,
                fldNuovaPass);

        // Box azioni
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);

        // Bottone annulla
        Button btnAnnulla = new Button("Indietro");
        btnAnnulla.getStyleClass().add("button-secondary");

        // Bottone salva
        Button btnSalva = new Button("Salva");
        btnSalva.getStyleClass().add("button-success");

        // Bottone elimina account
        Button btnElimina = new Button("🗑 Elimina");
        btnElimina.getStyleClass().add("button-danger");

        // Azioni bottone elimina
        btnElimina.setOnAction(e -> {

            // Conferma eliminazione account
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Elimina Profilo");
            alert.setHeaderText("Attenzione: Azione Irreversibile!");
            alert.setContentText(
                    "Sei sicuro di voler eliminare definitivamente il tuo account?\nTutti i dati verranno persi.");

            // Gestione risposta
            Optional<ButtonType> result = alert.showAndWait();

            // Se confermato, elimina l'account
            if (result.isPresent() && result.get() == ButtonType.OK) {

                // Elimina account dal database
                try {

                    // Chiamata al metodo di eliminazione account
                    utente.eliminaAccount();

                    // Notifica eliminazione avvenuta e torna al login
                    showAlert(AlertType.INFORMATION, "Account Eliminato", "Il tuo account è stato eliminato.");
                    LoginView login = new LoginView(stage);
                    stage.getScene().setRoot(login.getView());
                } catch (SQLException ex) {

                    // Mostra errore in caso di problemi
                    showAlert(AlertType.ERROR, "Errore", "Impossibile eliminare l'account: " + ex.getMessage());
                }
            }
        });

        // Azioni bottone annulla
        btnAnnulla.setOnAction(e -> tornaAllaHome());

        // Azioni bottone salva
        btnSalva.setOnAction(e -> {

            // Recupera i nuovi dati
            String nuovoNome = fldNome.getText().trim();
            String nuovoCognome = fldCognome.getText().trim();
            String nuovaPw = fldNuovaPass.getText();

            // Controllo campi obbligatori
            if (nuovoNome.isEmpty() || nuovoCognome.isEmpty()) {

                // Mostra errore
                showAlert(AlertType.ERROR, "Errore", "Nome e Cognome obbligatori.");
                return;
            }

            // Aggiorna i dati dell'utente
            utente.setNome(nuovoNome);
            utente.setCognome(nuovoCognome);
            utente.setFotoProfilo(nuovoPercorsoFoto);

            // Aggiorna la password solo se è stata inserita
            if (!nuovaPw.isEmpty()) {

                // Controllo lunghezza minima password
                if (nuovaPw.length() < 8) {

                    //  Mostra errore
                    showAlert(AlertType.ERROR, "Errore", "Password min. 8 caratteri.");
                    return;
                }
                utente.setPassword(nuovaPw);
            }

            // Salva le modifiche nel database
            try {

                // Chiamata al metodo di aggiornamento profilo
                utente.aggiornaProfilo();

                // Notifica successo e torna alla home
                showAlert(AlertType.INFORMATION, "Successo", "Profilo aggiornato!");
                tornaAllaHome();
            } catch (SQLException ex) {

                // Mostra errore in caso di problemi
                showAlert(AlertType.ERROR, "Errore DB", ex.getMessage());
            }
        });

        // Aggiunta bottoni al box azioni
        actionBox.getChildren().addAll(btnAnnulla, btnElimina, btnSalva);

        // Aggiunta componenti al form box
        formBox.getChildren().addAll(lblTitolo, fotoBox, inputs, actionBox);

        // Aggiunta form box al root
        root.getChildren().add(formBox);

        // Ritorna il root della vista
        return root;
    }

    // Metodo per tornare alla Home
    private void tornaAllaHome() {

        // Torna alla vista Home con l'utente aggiornato
        HomeView home = new HomeView(stage, utente);
        stage.getScene().setRoot(home.getView());
    }

    // Metodo per caricare l'anteprima della foto profilo
    private void caricaAnteprimaFoto(ImageView view, String path) {

        // Carica l'immagine dal percorso specificato e la imposta nell'ImageView
        try {

            // Controlla se il percorso è valido
            if (path != null && !path.isEmpty()) {

                // Carica l'immagine
                File file = path.startsWith("/") || path.startsWith("media") ? MediaManager.getMediaFile(path)
                        : new File(path);
                
                // Imposta l'immagine nell'ImageView
                if (file != null && file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    view.setImage(img);
                } else {
                    view.setImage(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo di utilità per mostrare alert
    private void showAlert(AlertType type, String title, String msg) {
        
        // Mostra un alert con il messaggio specificato
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}