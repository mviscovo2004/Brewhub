package it.univaq.brewhub.view;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Torrefattore;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.utility.MediaManager;
import it.univaq.brewhub.view.components.PasswordFieldWithToggler;
import it.univaq.brewhub.view.components.VerificationBadge;
import it.univaq.brewhub.view.utils.UiUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;

/**
 * Gestisce la vista del profilo personale dell'utente loggato.
 * Permette di visualizzare e aggiornare i propri dati, cambiare la foto profilo
 * o eliminare l'account.
 */
public class ProfileView {

    private final Stage stage;
    private final Utente utente;
    private String nuovoPercorsoFoto = null;
    private final UserService userService = UserService.getInstance();
    private Torrefattore torrefattoreDetails = null;

    /**
     * Costruisce la ProfileView.
     *
     * @param stage  Lo stage principale.
     * @param utente L'utente di cui visualizzare il profilo.
     */
    public ProfileView(Stage stage, Utente utente) {
        this.stage = stage;
        this.utente = utente;
        this.nuovoPercorsoFoto = utente.getFotoProfilo();
    }

    /**
     * Costruisce e restituisce l'interfaccia di gestione profilo.
     *
     * @return Il nodo {@link Parent} root.
     */
    public Parent getView() {
        if (utente.getTipo() == Utente.TipoUtente.TORREFATTORE) {
            torrefattoreDetails = userService.getTorrefattoreDetails(utente.getUsername());
            if (torrefattoreDetails == null) {
                // Fallback se dettaglio non trovato, costruiamo un oggetto parziale dai dati
                // utente
                torrefattoreDetails = new Torrefattore();
                torrefattoreDetails.setUsername(utente.getUsername());
                torrefattoreDetails.setNome(utente.getNome());
                torrefattoreDetails.setCognome(utente.getCognome());
                torrefattoreDetails.setFotoProfilo(utente.getFotoProfilo());
                torrefattoreDetails.setPasswordCrypto(utente.getPasswordCrypto());
            }
        }

        BorderPane root = new BorderPane();
        try {
            root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            // Ignora
        }
        root.getStyleClass().add("profile-view");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox mainLayout = new HBox(40);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.setStyle("-fx-background-color: transparent;");

        // Colonna Sinistra (Foto)
        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setMinWidth(250);
        leftColumn.setMaxWidth(300);
        leftColumn.getStyleClass().add("profile-photo-container");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(200);
        imgView.setFitHeight(200);
        imgView.setPreserveRatio(false);
        caricaAnteprimaFoto(imgView, utente.getFotoProfilo());

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
            File file = UiUtils.chooseImage(stage);
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

        // Colonna Destra (Dati)
        VBox rightColumn = new VBox(25);
        rightColumn.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        Label lblTitolo = new Label("Il mio Profilo");
        lblTitolo.getStyleClass().add("title-label");

        Label lblRuolo = new Label(utente.getTipo() != null ? utente.getTipo().toString() : "Utente");
        lblRuolo.getStyleClass().add("role-label");

        VBox headerBox = new VBox(5, lblTitolo, lblRuolo);

        Label lblDati = new Label("Dati Personali");
        lblDati.getStyleClass().add("section-header");
        lblDati.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        Label lblUser = new Label("Username");
        lblUser.getStyleClass().add("subtitle-label");
        HBox usernameLabelBox = new HBox(10, lblUser);
        usernameLabelBox.setAlignment(Pos.CENTER_LEFT);
        if (utente.getTipo() == Utente.TipoUtente.TORREFATTORE) {
            VerificationBadge badge = new VerificationBadge(14);
            usernameLabelBox.getChildren().add(badge);
        }

        TextField fldUsername = new TextField(utente.getUsername());
        fldUsername.setEditable(false);
        fldUsername.getStyleClass().addAll("text-field", "text-field-readonly");
        fldUsername.setMaxWidth(Double.MAX_VALUE);

        TextField fldNome = null;
        TextField fldCognome = null;
        TextField fldAzienda = null;
        TextArea areaDesc = null;

        if (utente.getTipo() == Utente.TipoUtente.TORREFATTORE && torrefattoreDetails != null) {
            Label lblAzienda = new Label("Nome Azienda");
            lblAzienda.getStyleClass().add("subtitle-label");
            fldAzienda = new TextField(torrefattoreDetails.getNomeAzienda());
            fldAzienda.getStyleClass().add("text-field");
            fldAzienda.setMaxWidth(Double.MAX_VALUE);

            Label lblDesc = new Label("Descrizione");
            lblDesc.getStyleClass().add("subtitle-label");
            areaDesc = new TextArea(torrefattoreDetails.getDescrizione());
            areaDesc.getStyleClass().add("text-area");
            areaDesc.setWrapText(true);
            areaDesc.setPrefRowCount(3);
            areaDesc.setMaxWidth(Double.MAX_VALUE);

            VBox aziendaBox = new VBox(5, lblAzienda, fldAzienda);
            grid.add(aziendaBox, 0, 1, 2, 1);

            VBox descBox = new VBox(5, lblDesc, areaDesc);
            grid.add(descBox, 0, 2, 2, 1);
        } else {
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

            VBox nomeBox = new VBox(5, lblNome, fldNome);
            VBox cognomeBox = new VBox(5, lblCognome, fldCognome);
            grid.add(nomeBox, 0, 1);
            grid.add(cognomeBox, 1, 1);
        }

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        VBox userBox = new VBox(5, usernameLabelBox, fldUsername);
        grid.add(userBox, 0, 0, 2, 1);

        Label lblSicurezza = new Label("Sicurezza");
        lblSicurezza.getStyleClass().add("section-header");
        lblSicurezza.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(lblSicurezza, new Insets(10, 0, 0, 0));

        Label lblPw = new Label("Nuova Password");
        lblPw.getStyleClass().add("subtitle-label");
        PasswordFieldWithToggler fldNuovaPass = new PasswordFieldWithToggler("Inserisci per cambiare password...");
        fldNuovaPass.setMaxWidth(Double.MAX_VALUE);

        VBox securityBox = new VBox(5, lblPw, fldNuovaPass);

        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(30, 0, 0, 0));

        Button btnAnnulla = new Button("Indietro");
        btnAnnulla.getStyleClass().add("button-secondary");

        Button btnElimina = new Button("Elimina Account");
        btnElimina.getStyleClass().add("button-danger");

        Button btnSalva = new Button("Salva Modifiche");
        btnSalva.getStyleClass().add("button-success");
        btnSalva.setDefaultButton(true);

        actionBox.getChildren().addAll(btnElimina, btnAnnulla, btnSalva);

        btnElimina.setOnAction(e -> {
            boolean confirmed = DialogUtils.showConfirmation("Elimina Profilo",
                    "Sei sicuro di voler eliminare definitivamente il tuo account?\nTutti i dati verranno persi.",
                    stage);
            if (confirmed) {
                try {
                    userService.deleteUser(utente.getUsername());
                    DialogUtils.showInfo("Account Eliminato", "Il tuo account è stato eliminato.", stage);
                    LoginView login = new LoginView(stage);
                    stage.getScene().setRoot(login.getView());
                } catch (BusinessException ex) {
                    DialogUtils.showError("Errore", "Impossibile eliminare l'account: " + ex.getMessage(), stage);
                }
            }
        });

        btnAnnulla.setOnAction(e -> tornaAllaHome());

        // Riferimenti finali per lambda
        final TextField finalFldNome = fldNome;
        final TextField finalFldCognome = fldCognome;
        final TextField finalFldAzienda = fldAzienda;
        final TextArea finalAreaDesc = areaDesc;

        btnSalva.setOnAction(e -> {
            String nuovaPw = fldNuovaPass.getText();

            // Logica Save Differenziata
            if (utente.getTipo() == Utente.TipoUtente.TORREFATTORE && torrefattoreDetails != null) {
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

                if (!nuovaPw.isEmpty()) {
                    if (nuovaPw.length() < 8) {
                        DialogUtils.showError("Errore", "Password min. 8 caratteri.", stage);
                        return;
                    }
                    torrefattoreDetails.setPassword(nuovaPw);
                }

                try {
                    userService.updateTorrefattore(torrefattoreDetails);
                    // Aggiorna anche oggetto sessione
                    utente.setFotoProfilo(nuovoPercorsoFoto);
                    DialogUtils.showInfo("Successo", "Profilo Aziendale aggiornato!", stage);
                    tornaAllaHome();
                } catch (BusinessException ex) {
                    DialogUtils.showError("Errore DB", ex.getMessage(), stage);
                }
            } else {
                // UTENTE STANDARD
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
                    userService.updateUser(utente);
                    DialogUtils.showInfo("Successo", "Profilo aggiornato!", stage);
                    tornaAllaHome();
                } catch (BusinessException ex) {
                    DialogUtils.showError("Errore DB", ex.getMessage(), stage);
                }
            }
        });

        rightColumn.getChildren().addAll(headerBox, lblDati, grid, lblSicurezza, securityBox, actionBox);
        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        scrollPane.setContent(mainLayout);
        root.setCenter(scrollPane);
        return root;
    }

    private void tornaAllaHome() {
        HomeView home = new HomeView(stage, utente);
        stage.getScene().setRoot(home.getView());
    }

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
