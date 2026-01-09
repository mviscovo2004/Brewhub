package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;
import java.util.Optional;
import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;
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

/**
 * Gestisce la vista del profilo utente.
 * Permette di visualizzare e modificare le informazioni personali (nome,
 * cognome, foto, password)
 * e di eliminare l'account.
 */
public class ProfileView {

    /** Riferimento allo stage principale dell'applicazione. */
    private final Stage stage;
    /** Oggetto utente di cui si sta visualizzando/modificando il profilo. */
    private final Utente utente;

    /**
     * Percorso della nuova foto profilo selezionata (opzionale, null se non
     * cambiata).
     */
    private String nuovoPercorsoFoto = null;

    /**
     * Costruttore della vista profilo.
     * 
     * @param stage  Lo stage principale.
     * @param utente L'utente loggato di cui gestire il profilo.
     */
    public ProfileView(Stage stage, Utente utente) {
        this.stage = stage;
        this.utente = utente;
        this.nuovoPercorsoFoto = utente.getFotoProfilo();
    }

    /**
     * Crea e restituisce l'interfaccia grafica per la gestione del profilo.
     * Include form di modifica dati, upload foto e azioni di
     * salvataggio/eliminazione.
     *
     * @return Parent Il nodo radice della vista.
     */
    public Parent getView() {

        // Contenitore principale
        StackPane root = new StackPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Box del form centrale
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(450);
        formBox.getStyleClass().add("form-box");

        // Titolo
        Label lblTitolo = new Label("Il mio Profilo");
        lblTitolo.getStyleClass().add("title-label");

        // Componente Foto profilo
        ImageView imgView = new ImageView();
        imgView.setFitWidth(120);
        imgView.setFitHeight(120);

        // Caricamento anteprima foto attuale
        caricaAnteprimaFoto(imgView, utente.getFotoProfilo());

        // Maschera ritaglio circolare per la foto
        Circle clip = new Circle(60, 60, 60);
        imgView.setClip(clip);

        // Bottone per caricamento nuova foto
        Button btnCambiaFoto = new Button("📷 Cambia Foto");
        btnCambiaFoto.getStyleClass().add("button-secondary");

        // Azione cambio foto
        btnCambiaFoto.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Scegli nuova foto profilo");
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg"));

            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                // Copia la foto nelle risorse e aggiorna anteprima
                MediaManager.initMediaFolder();
                String path = MediaManager.copyMediaToFolder(file);

                if (path != null) {
                    nuovoPercorsoFoto = path;
                    caricaAnteprimaFoto(imgView, file.getAbsolutePath());
                }
            }
        });

        // Box contenitore foto
        VBox fotoBox = new VBox(10, imgView, btnCambiaFoto);
        fotoBox.setAlignment(Pos.CENTER);

        // Campi di input dettaglio utente
        VBox inputs = new VBox(8);
        inputs.setAlignment(Pos.CENTER_LEFT);

        // Username (read-only)
        Label lblUser = new Label("Username:");
        lblUser.getStyleClass().add("label");
        TextField fldUsername = new TextField(utente.getUsername());
        fldUsername.setEditable(false);
        fldUsername.getStyleClass().add("text-field");
        fldUsername.getStyleClass().add("text-field-readonly");

        // Nome
        Label lblNome = new Label("Nome:");
        lblNome.getStyleClass().add("label");
        TextField fldNome = new TextField(utente.getNome());
        fldNome.getStyleClass().add("text-field");

        // Cognome
        Label lblCognome = new Label("Cognome:");
        lblCognome.getStyleClass().add("label");
        TextField fldCognome = new TextField(utente.getCognome());
        fldCognome.getStyleClass().add("text-field");

        // Password
        Label lblPw = new Label("Sicurezza:");
        lblPw.getStyleClass().add("label");
        PasswordField fldNuovaPass = new PasswordField();
        fldNuovaPass.setPromptText("Nuova Password (lascia vuoto per mantenere)");
        fldNuovaPass.getStyleClass().add("password-field");

        inputs.getChildren().addAll(lblUser, fldUsername, lblNome, fldNome, lblCognome, fldCognome, lblPw,
                fldNuovaPass);

        // Bottoni azioni
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);

        Button btnAnnulla = new Button("Indietro");
        btnAnnulla.getStyleClass().add("button-secondary");

        Button btnSalva = new Button("Salva");
        btnSalva.getStyleClass().add("button-success");

        Button btnElimina = new Button("🗑 Elimina");
        btnElimina.getStyleClass().add("button-danger");

        // Azione Elimina Account
        btnElimina.setOnAction(e -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Elimina Profilo");
            alert.setHeaderText("Attenzione: Azione Irreversibile!");
            alert.setContentText(
                    "Sei sicuro di voler eliminare definitivamente il tuo account?\nTutti i dati verranno persi.");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    new UtenteDAOImpl().delete(utente.getUsername());
                    showAlert(AlertType.INFORMATION, "Account Eliminato", "Il tuo account è stato eliminato.");
                    LoginView login = new LoginView(stage);
                    stage.getScene().setRoot(login.getView());
                } catch (SQLException ex) {
                    showAlert(AlertType.ERROR, "Errore", "Impossibile eliminare l'account: " + ex.getMessage());
                }
            }
        });

        // Azione Annulla
        btnAnnulla.setOnAction(e -> tornaAllaHome());

        // Azione Salva Modifiche
        btnSalva.setOnAction(e -> {
            String nuovoNome = fldNome.getText().trim();
            String nuovoCognome = fldCognome.getText().trim();
            String nuovaPw = fldNuovaPass.getText();

            if (nuovoNome.isEmpty() || nuovoCognome.isEmpty()) {
                showAlert(AlertType.ERROR, "Errore", "Nome e Cognome obbligatori.");
                return;
            }

            // Aggiornamento oggetto utente locale
            utente.setNome(nuovoNome);
            utente.setCognome(nuovoCognome);
            utente.setFotoProfilo(nuovoPercorsoFoto);

            // Aggiornamento password opzionale
            if (!nuovaPw.isEmpty()) {
                if (nuovaPw.length() < 8) {
                    showAlert(AlertType.ERROR, "Errore", "Password min. 8 caratteri.");
                    return;
                }
                utente.setPassword(nuovaPw);
            }

            // Persistenza su DB tramite DAO
            try {
                new UtenteDAOImpl().update(utente);
                showAlert(AlertType.INFORMATION, "Successo", "Profilo aggiornato!");
                tornaAllaHome();
            } catch (SQLException ex) {
                showAlert(AlertType.ERROR, "Errore DB", ex.getMessage());
            }
        });

        actionBox.getChildren().addAll(btnAnnulla, btnElimina, btnSalva);
        formBox.getChildren().addAll(lblTitolo, fotoBox, inputs, actionBox);
        root.getChildren().add(formBox);

        return root;
    }

    /**
     * Naviga verso la vista Home passando l'utente aggiornato.
     */
    private void tornaAllaHome() {
        HomeView home = new HomeView(stage, utente);
        stage.getScene().setRoot(home.getView());
    }

    /**
     * Helper per caricare e impostare l'immagine nell'ImageView.
     * Gestisce i path relativi e assoluti.
     *
     * @param view L'imageView di destinazione.
     * @param path Il percorso del file immagine.
     */
    private void caricaAnteprimaFoto(ImageView view, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                File file = path.startsWith("/") || path.startsWith("media") ? MediaManager.getMediaFile(path)
                        : new File(path);

                if (file != null && file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    view.setImage(img);
                } else {
                    view.setImage(null);
                }
            }
        } catch (Exception e) {
            Log.error("Errore eliminazione account", e);
        }
    }

    /**
     * Mostra un alert di sistema generico.
     * 
     * @param type  Il tipo di alert (es. ERROR, INFORMATION).
     * @param title Il titolo della finestra di alert.
     * @param msg   Il messaggio contenuto nell'alert.
     */
    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}