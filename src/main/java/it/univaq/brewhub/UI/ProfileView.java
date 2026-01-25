package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;

import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.UI.components.PasswordFieldWithToggler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    // DAO specifici
    private final it.univaq.brewhub.dao.TorrefattoreDAO torrefattoreDAO = new it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl();
    private it.univaq.brewhub.Torrefattore torrefattoreDetails = null; // Popolato se l'utente è torrefattore

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

        // Se è Torrefattore, recuperiamo i dettagli aggiornati
        if (utente.getTipo() == it.univaq.brewhub.Utente.TipoUtente.TORREFATTORE) {
            try {
                torrefattoreDetails = torrefattoreDAO.findByUsername(utente.getUsername());
                // Se per qualche motivo è null (inconsistenza), usiamo un oggetto vuoto o
                // gestiamo l'errore
                if (torrefattoreDetails == null) {
                    // Fallback: proviamo a castare se possibile o creiamo placeholder
                    torrefattoreDetails = new it.univaq.brewhub.Torrefattore();
                    torrefattoreDetails.setUsername(utente.getUsername());
                    // copy base fields
                    torrefattoreDetails.setNome(utente.getNome());
                    torrefattoreDetails.setCognome(utente.getCognome());
                    torrefattoreDetails.setFotoProfilo(utente.getFotoProfilo());
                    torrefattoreDetails.setPasswordCrypto(utente.getPasswordCrypto());
                }
            } catch (SQLException e) {
                Log.error("Errore recupero dettagli torrefattore", e);
            }
        }

        // Contenitore principale

        // Contenitore principale
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        root.getStyleClass().add("profile-view");

        // ScrollPane per flessibilità
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Layout principale orizzontale (separazione Foto / Dati)
        HBox mainLayout = new HBox(40);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new javafx.geometry.Insets(40));
        mainLayout.setStyle("-fx-background-color: transparent;");

        // --- COLONNA SINISTRA: FOTO ---
        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setMinWidth(250);
        leftColumn.setMaxWidth(300);
        leftColumn.getStyleClass().add("profile-photo-container");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(200);
        imgView.setFitHeight(200);
        imgView.setPreserveRatio(false);

        // Caricamento anteprima foto attuale
        caricaAnteprimaFoto(imgView, utente.getFotoProfilo());

        // Maschera ritaglio circolare E Wrapper per ombra
        Circle clip = new Circle(100, 100, 100);
        imgView.setClip(clip);

        StackPane imgContainer = new StackPane(imgView);
        imgContainer.setMaxSize(200, 200);
        imgContainer.getStyleClass().add("profile-photo-wrapper");

        Circle border = new Circle(100);
        border.setStroke(javafx.scene.paint.Color.web("#D4A574"));
        border.setStrokeWidth(4);
        border.setFill(javafx.scene.paint.Color.TRANSPARENT);
        border.setMouseTransparent(true);
        imgContainer.getChildren().add(border);

        Button btnCambiaFoto = new Button("📷 Modifica Foto");
        btnCambiaFoto.getStyleClass().add("button-secondary");

        btnCambiaFoto.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Scegli nuova foto profilo");
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg"));

            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                MediaManager.initMediaFolder();
                String path = MediaManager.copyMediaToFolder(file);

                if (path != null) {
                    nuovoPercorsoFoto = path;
                    caricaAnteprimaFoto(imgView, file.getAbsolutePath());
                }
            }
        });

        leftColumn.getChildren().addAll(imgContainer, btnCambiaFoto);

        // --- COLONNA DESTRA: DATI ---
        VBox rightColumn = new VBox(25);
        rightColumn.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(rightColumn, javafx.scene.layout.Priority.ALWAYS);

        // Titolo pagina
        Label lblTitolo = new Label("Il mio Profilo");
        lblTitolo.getStyleClass().add("title-label");
        // Sottotitolo / Ruolo
        Label lblRuolo = new Label(utente.getTipo() != null ? utente.getTipo().toString() : "Utente");
        lblRuolo.getStyleClass().add("role-label");

        VBox headerBox = new VBox(5, lblTitolo, lblRuolo);

        // Sezione Dati Personali
        Label lblDati = new Label("Dati Personali");
        lblDati.getStyleClass().add("section-header");
        lblDati.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Username
        // Username
        Label lblUser = new Label("Username");
        lblUser.getStyleClass().add("subtitle-label");

        HBox usernameLabelBox = new HBox(10, lblUser);
        usernameLabelBox.setAlignment(Pos.CENTER_LEFT);

        if (utente.getTipo() == Utente.TipoUtente.TORREFATTORE) {
            it.univaq.brewhub.UI.components.VerificationBadge badge = new it.univaq.brewhub.UI.components.VerificationBadge(
                    14);
            usernameLabelBox.getChildren().add(badge);
        }

        TextField fldUsername = new TextField(utente.getUsername());
        fldUsername.setEditable(false);
        fldUsername.getStyleClass().addAll("text-field", "text-field-readonly");
        fldUsername.setMaxWidth(Double.MAX_VALUE);

        // Campi dinamici: Azienda+Descrizione OR Nome+Cognome
        TextField fldNome = null;
        TextField fldCognome = null;

        TextField fldAzienda = null;
        javafx.scene.control.TextArea areaDesc = null;

        if (utente.getTipo() == it.univaq.brewhub.Utente.TipoUtente.TORREFATTORE && torrefattoreDetails != null) {
            // --- FIELDS TORREFATTORE ---
            Label lblAzienda = new Label("Nome Azienda");
            lblAzienda.getStyleClass().add("subtitle-label");
            fldAzienda = new TextField(torrefattoreDetails.getNomeAzienda());
            fldAzienda.getStyleClass().add("text-field");
            fldAzienda.setMaxWidth(Double.MAX_VALUE);

            Label lblDesc = new Label("Descrizione");
            lblDesc.getStyleClass().add("subtitle-label");
            areaDesc = new javafx.scene.control.TextArea(torrefattoreDetails.getDescrizione());
            areaDesc.getStyleClass().add("text-area");
            areaDesc.setWrapText(true);
            areaDesc.setPrefRowCount(3);
            areaDesc.setMaxWidth(Double.MAX_VALUE);

            // Aggiunta alla griglia
            // Row 1: Azienda (span 2 cols)
            VBox aziendaBox = new VBox(5, lblAzienda, fldAzienda);
            grid.add(aziendaBox, 0, 1, 2, 1);

            // Row 2: Descrizione (span 2 cols)
            VBox descBox = new VBox(5, lblDesc, areaDesc);
            grid.add(descBox, 0, 2, 2, 1);

        } else {
            // --- FIELDS UTENTE STANDARD ---
            Label lblNome = new Label("Nome");
            lblNome.getStyleClass().add("subtitle-label");
            fldNome = new TextField(utente.getNome());
            fldNome.getStyleClass().add("text-field");
            fldNome.setMaxWidth(Double.MAX_VALUE);

            Label lblCognome = new Label("Cognome");
            lblCognome.getStyleClass().add("subtitle-label");
            fldCognome = new TextField(utente.getCognome());
            fldCognome.getStyleClass().add("text-field");
            fldCognome.setMaxWidth(Double.MAX_VALUE);

            // Row 1: Nome | Cognome
            VBox nomeBox = new VBox(5, lblNome, fldNome);
            VBox cognomeBox = new VBox(5, lblCognome, fldCognome);
            grid.add(nomeBox, 0, 1);
            grid.add(cognomeBox, 1, 1);
        }

        // Constraint colonne griglia: 50% e 50%
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setPercentWidth(50);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Layout Griglia
        // Row 0: Username (spanning 2 cols? Or just left?) Let's span 2 for separation
        VBox userBox = new VBox(5, usernameLabelBox, fldUsername);
        grid.add(userBox, 0, 0, 2, 1);

        // Grid layout logic already handled above in conditional block

        // Sezione Sicurezza
        Label lblSicurezza = new Label("Sicurezza");
        lblSicurezza.getStyleClass().add("section-header");
        lblSicurezza.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(lblSicurezza, new javafx.geometry.Insets(10, 0, 0, 0));

        Label lblPw = new Label("Nuova Password");
        lblPw.getStyleClass().add("subtitle-label");

        // Using custom component
        PasswordFieldWithToggler fldNuovaPass = new PasswordFieldWithToggler("Inserisci per cambiare password...");
        fldNuovaPass.setMaxWidth(Double.MAX_VALUE);

        VBox securityBox = new VBox(5, lblPw, fldNuovaPass);

        // Actions
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new javafx.geometry.Insets(30, 0, 0, 0));

        Button btnAnnulla = new Button("Indietro");
        btnAnnulla.getStyleClass().add("button-secondary");

        Button btnElimina = new Button("Elimina Account");
        btnElimina.getStyleClass().add("button-danger");

        Button btnSalva = new Button("Salva Modifiche");
        btnSalva.getStyleClass().add("button-success");
        btnSalva.setDefaultButton(true);

        actionBox.getChildren().addAll(btnElimina, btnAnnulla, btnSalva);

        // Configurazione azioni
        btnElimina.setOnAction(e -> {
            boolean confirmed = DialogUtils.showConfirmation("Elimina Profilo",
                    "Sei sicuro di voler eliminare definitivamente il tuo account?\nTutti i dati verranno persi.",
                    stage);
            if (confirmed) {
                try {
                    new UtenteDAOImpl().delete(utente.getUsername());
                    DialogUtils.showInfo("Account Eliminato", "Il tuo account è stato eliminato.", stage);
                    LoginView login = new LoginView(stage);
                    stage.getScene().setRoot(login.getView());
                } catch (SQLException ex) {
                    DialogUtils.showError("Errore", "Impossibile eliminare l'account: " + ex.getMessage(), stage);
                }
            }
        });

        btnAnnulla.setOnAction(e -> tornaAllaHome());

        // Final refs for variables in lambda
        final TextField finalFldNome = fldNome;
        final TextField finalFldCognome = fldCognome;
        final TextField finalFldAzienda = fldAzienda;
        final javafx.scene.control.TextArea finalAreaDesc = areaDesc;

        btnSalva.setOnAction(e -> {
            String nuovaPw = fldNuovaPass.getText();

            // Logica Save Differenziata
            if (utente.getTipo() == it.univaq.brewhub.Utente.TipoUtente.TORREFATTORE && torrefattoreDetails != null) {
                if (finalFldAzienda != null) {
                    String azienda = finalFldAzienda.getText().trim();
                    if (azienda.isEmpty()) {
                        DialogUtils.showError("Errore", "Nome Azienda obbligatorio.", stage);
                        return;
                    }
                    torrefattoreDetails.setNomeAzienda(azienda);
                }

                if (finalAreaDesc != null) {
                    torrefattoreDetails.setDescrizione(finalAreaDesc.getText());
                }
                torrefattoreDetails.setFotoProfilo(nuovoPercorsoFoto);

                // Keep base fields consistent if needed, but we rely on nomeAzienda mainly
                // Maybe update nome/cognome to match azienda/empty? Or keep them as is in DB?
                // For simplicity, let's keep nome/cognome as is or ignored for Roasters display

                if (!nuovaPw.isEmpty()) {
                    if (nuovaPw.length() < 8) {
                        DialogUtils.showError("Errore", "Password min. 8 caratteri.", stage);
                        return;
                    }
                    torrefattoreDetails.setPassword(nuovaPw);
                }

                try {
                    torrefattoreDAO.update(torrefattoreDetails);
                    // Update session object too
                    utente.setFotoProfilo(nuovoPercorsoFoto); // reflect changes immediately
                    DialogUtils.showInfo("Successo", "Profilo Aziendale aggiornato!", stage);
                    tornaAllaHome();
                } catch (SQLException ex) {
                    DialogUtils.showError("Errore DB", ex.getMessage(), stage);
                }

            } else {
                // STANDARD USER
                String nuovoNome = "";
                String nuovoCognome = "";

                if (finalFldNome != null)
                    nuovoNome = finalFldNome.getText().trim();

                if (finalFldCognome != null)
                    nuovoCognome = finalFldCognome.getText().trim();

                if (nuovoNome.isEmpty() || nuovoCognome.isEmpty()) {
                    DialogUtils.showError("Errore", "Nome e Cognome obbligatori.", stage);
                    return;
                }

                utente.setNome(nuovoNome);
                utente.setCognome(nuovoCognome);
                utente.setFotoProfilo(nuovoPercorsoFoto);

                if (!nuovaPw.isEmpty()) {
                    if (nuovaPw.length() < 8) {
                        DialogUtils.showError("Errore", "Password min. 8 caratteri.", stage);
                        return;
                    }
                    utente.setPassword(nuovaPw);
                }

                try {
                    new UtenteDAOImpl().update(utente);
                    DialogUtils.showInfo("Successo", "Profilo aggiornato!", stage);
                    tornaAllaHome();
                } catch (SQLException ex) {
                    DialogUtils.showError("Errore DB", ex.getMessage(), stage);
                }
            } // End else Standard User
        });

        // Assemblaggio Right Column
        rightColumn.getChildren().addAll(headerBox, lblDati, grid, lblSicurezza, securityBox, actionBox);

        // Assemblaggio Main Layout
        mainLayout.getChildren().addAll(leftColumn, rightColumn);

        scrollPane.setContent(mainLayout);
        root.setCenter(scrollPane);

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

}