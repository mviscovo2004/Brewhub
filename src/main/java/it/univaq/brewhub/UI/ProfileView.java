package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;
import java.util.Optional;

import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.Utente;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ProfileView {

    private final Stage stage;
    private final Utente utente;
    private String nuovoPercorsoFoto = null;

    public ProfileView(Stage stage, Utente utente) {
        this.stage = stage;
        this.utente = utente;
        this.nuovoPercorsoFoto = utente.getFotoProfilo();
    }

    public Parent getView() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new javafx.geometry.Insets(30));
        layout.setStyle("-fx-background-color: " + ThemeManager.Colors.WHITE_CREAM + ";");

        // --- TITOLO ---
        Label lblTitolo = new Label("Il mio Profilo");
        lblTitolo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + ThemeManager.Colors.DARK_COFFEE + ";");

        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(400);
        formBox.getStyleClass().add("form-box");

        // --- FOTO PROFILO ---
        ImageView imgView = new ImageView();
        imgView.setFitWidth(120);
        imgView.setFitHeight(120);
        
        // Carica foto attuale o placeholder
        caricaAnteprimaFoto(imgView, utente.getFotoProfilo());

        // Ritaglio circolare per la foto
        Circle clip = new Circle(60, 60, 60);
        imgView.setClip(clip);

        Button btnCambiaFoto = new Button("📷 Cambia Foto");
        btnCambiaFoto.setStyle(ThemeManager.Styles.buttonSecondary());
        
        btnCambiaFoto.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Scegli nuova foto profilo");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg"));
            File file = fileChooser.showOpenDialog(stage);
            
            if (file != null) {
                MediaManager.initMediaFolder();
                String path = MediaManager.copyMediaToFolder(file);
                if (path != null) {
                    nuovoPercorsoFoto = path;
                    caricaAnteprimaFoto(imgView, file.getAbsolutePath()); // Carica da disco per anteprima immediata
                }
            }
        });

        VBox fotoBox = new VBox(10, imgView, btnCambiaFoto);
        fotoBox.setAlignment(Pos.CENTER);

        // --- CAMPI DATI ---
        TextField fldUsername = new TextField(utente.getUsername());
        fldUsername.setEditable(false); // Username non modificabile (è Primary Key)
        fldUsername.setStyle(ThemeManager.Styles.textField() + "-fx-opacity: 0.7;");
        fldUsername.setPromptText("Username (non modificabile)");

        TextField fldNome = new TextField(utente.getNome());
        fldNome.setPromptText("Nome");
        fldNome.setStyle(ThemeManager.Styles.textField());

        TextField fldCognome = new TextField(utente.getCognome());
        fldCognome.setPromptText("Cognome");
        fldCognome.setStyle(ThemeManager.Styles.textField());

        // Campo password opzionale
        PasswordField fldNuovaPass = new PasswordField();
        fldNuovaPass.setPromptText("Nuova Password (lascia vuoto per mantenere attuale)");
        fldNuovaPass.setStyle(ThemeManager.Styles.textField());

        // --- BOTTONI AZIONE ---
        HBox actionBox = new HBox(15);
    actionBox.setAlignment(Pos.CENTER);

    Button btnAnnulla = new Button("Indietro");
    btnAnnulla.setStyle("-fx-background-color: " + ThemeManager.Colors.PALE_COFFEE + "; -fx-text-fill: " + ThemeManager.Colors.WHITE_CREAM + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
    
    Button btnSalva = new Button("Salva Modifiche");
    btnSalva.setStyle(ThemeManager.Styles.buttonSuccess());

    // --- NUOVO TASTO ELIMINA ---
    Button btnElimina = new Button("🗑 Elimina Profilo");
    btnElimina.setStyle(ThemeManager.Styles.buttonDanger()); // Stile rosso definito nel ThemeManager

    // --- LOGICA ELIMINAZIONE CON DOPPIA CONFERMA ---
    btnElimina.setOnAction(e -> {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Elimina Profilo");
        alert.setHeaderText("Attenzione: Azione Irreversibile!");
        alert.setContentText("Sei sicuro di voler eliminare definitivamente il tuo account?\nTutti i tuoi post, commenti e dati verranno persi per sempre.");

        // Mostra il dialog e attendi la risposta
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Esegue l'eliminazione nel DB
                utente.eliminaAccount();
                
                // Messaggio di successo
                showAlert(AlertType.INFORMATION, "Account Eliminato", "Il tuo account è stato eliminato correttamente. Arrivederci!");
                
                // Reindirizza al Login
                LoginView login = new LoginView(stage);
                stage.getScene().setRoot(login.getView());
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(AlertType.ERROR, "Errore", "Impossibile eliminare l'account: " + ex.getMessage());
            }
        }
    });

    // Aggiungi i tre bottoni al box (Indietro - Elimina - Salva)
    actionBox.getChildren().addAll(btnAnnulla, btnElimina, btnSalva);

        formBox.getChildren().addAll(fotoBox, new Label("Username:"), fldUsername, new Label("Nome:"), fldNome, new Label("Cognome:"), fldCognome, new Label("Sicurezza:"), fldNuovaPass, actionBox);
        layout.getChildren().addAll(lblTitolo, formBox);

        // --- LOGICA BOTTONI ---
        
        btnAnnulla.setOnAction(e -> tornaAllaHome());

        btnSalva.setOnAction(e -> {
            String nuovoNome = fldNome.getText().trim();
            String nuovoCognome = fldCognome.getText().trim();
            String nuovaPw = fldNuovaPass.getText();

            if (nuovoNome.isEmpty() || nuovoCognome.isEmpty()) {
                showAlert(AlertType.ERROR, "Errore", "Nome e Cognome non possono essere vuoti.");
                return;
            }

            // Aggiorna l'oggetto utente in memoria
            utente.setNome(nuovoNome);
            utente.setCognome(nuovoCognome);
            utente.setFotoProfilo(nuovoPercorsoFoto);

            if (!nuovaPw.isEmpty()) {
                if (nuovaPw.length() < 8) {
                    showAlert(AlertType.ERROR, "Errore", "La password deve essere di almeno 8 caratteri.");
                    return;
                }
                utente.setPassword(nuovaPw); // Verrà hashata nel metodo aggiornaProfilo
            }

            try {
                utente.aggiornaProfilo();
                showAlert(AlertType.INFORMATION, "Successo", "Profilo aggiornato correttamente!");
                tornaAllaHome();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(AlertType.ERROR, "Errore Database", "Impossibile salvare le modifiche: " + ex.getMessage());
            }
        });

        return layout;
    }

    private void tornaAllaHome() {
        HomeView home = new HomeView(stage, utente);
        stage.getScene().setRoot(home.getView());
    }

    private void caricaAnteprimaFoto(ImageView view, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                // Se è un percorso relativo (dal DB), usiamo MediaManager, altrimenti (dal FileChooser) carichiamo diretto
                File file = path.startsWith("/") || path.startsWith("media") ? MediaManager.getMediaFile(path) : new File(path);
                
                if (file != null && file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    view.setImage(img);
                } else {
                    // Placeholder se file non trovato
                    view.setImage(null); 
                    view.setStyle("-fx-background-color: #ccc;");
                }
            }
        } catch (Exception e) {
            System.out.println("Errore caricamento anteprima: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}