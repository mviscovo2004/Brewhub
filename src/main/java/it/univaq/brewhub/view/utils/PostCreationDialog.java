package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Post.TipoPost;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.MediaManager;
import it.univaq.brewhub.view.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Dialogo per la creazione di un nuovo post.
 * Supporta testo, caricamento immagini/video e selezione opzionale di una
 * categoria.
 */
public class PostCreationDialog {

    private static final PostService postService = PostService.getInstance();
    private static File selectedFile = null;
    private static TipoPost selectedType = TipoPost.TESTO;

    /**
     * Mostra il dialogo per la creazione di un post.
     *
     * @param owner         Lo stage proprietario.
     * @param utente        L'utente autore del post.
     * @param categorie     Lista delle categorie disponibili per il post (può
     *                      essere null o vuota).
     * @param onPostCreated Callback eseguita dopo la creazione con successo.
     */
    public static void show(Stage owner, Utente utente, List<Categoria> categorie, Runnable onPostCreated) {
        selectedFile = null;
        selectedType = TipoPost.TESTO;

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Crea Nuovo Post");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(600);
        root.setPrefHeight(500);

        try {
            root.getStylesheets().add(PostCreationDialog.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }

        // --- TOP: Header ---
        Label lblTitle = new Label("Nuovo Post");
        lblTitle.getStyleClass().add("modal-title");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        lblTitle.setAlignment(Pos.CENTER);
        root.setTop(lblTitle);

        // --- CENTER: Content Input ---
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20, 0, 20, 0));

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Dai un titolo al tuo post...");
        txtTitle.setId("fldTitolo");
        txtTitle.getStyleClass().add("input-large");

        Label noMediaLbl = new Label("Clicca per caricare una foto o un video");
        noMediaLbl.setStyle("-fx-text-fill: #8D6E63; -fx-font-weight: bold;");

        // Options Row (Type & Category)
        HBox optionsBox = new HBox(15);
        optionsBox.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<TipoPost> choiceType = new ChoiceBox<>();
        choiceType.getItems().addAll(TipoPost.values());
        choiceType.setValue(TipoPost.TESTO);
        choiceType.getStyleClass().add("choice-box");
        choiceType.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(choiceType, Priority.ALWAYS);
        // Type listener moved down

        ComboBox<Categoria> comboCat = new ComboBox<>();
        comboCat.getItems().add(null); // Nessuna
        if (categorie != null) {
            comboCat.getItems().addAll(categorie);
        }
        comboCat.getSelectionModel().selectFirst();
        comboCat.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboCat, Priority.ALWAYS);
        comboCat.getStyleClass().add("choice-box");

        comboCat.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Nessuna 🚫");
                } else {
                    setText(getEmojiForCategory(item.getNome()) + " " + item.getNome());
                }
            }
        });
        comboCat.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Nessuna 🚫");
                } else {
                    setText(getEmojiForCategory(item.getNome()) + " " + item.getNome());
                }
            }
        });

        optionsBox.getChildren().addAll(choiceType, comboCat);

        TextArea txtContent = new TextArea();
        txtContent.setId("postArea");
        txtContent.setPromptText("Racconta la tua esperienza...");
        txtContent.setWrapText(true);
        txtContent.setPrefRowCount(6);
        txtContent.getStyleClass().add("text-area");
        VBox.setVgrow(txtContent, Priority.ALWAYS);

        // Media Preview Area (Original upload-container style)
        StackPane mediaPreview = new StackPane();
        mediaPreview.getStyleClass().add("upload-container");
        mediaPreview.setPrefHeight(120);
        mediaPreview.setAlignment(Pos.CENTER);

        // Dinamicamente visibile solo se non è un post di testo
        mediaPreview.visibleProperty().bind(choiceType.valueProperty().isNotEqualTo(TipoPost.TESTO));
        mediaPreview.managedProperty().bind(mediaPreview.visibleProperty());

        mediaPreview.getChildren().add(noMediaLbl);

        choiceType.setConverter(new javafx.util.StringConverter<TipoPost>() {
            @Override
            public String toString(TipoPost object) {
                if (object == null)
                    return "";
                switch (object) {
                    case TESTO:
                        return "📝 Testo";
                    case FOTO:
                        return "📸 Foto";
                    case VIDEO:
                        return "🎬 Video";
                    default:
                        return object.toString();
                }
            }

            @Override
            public TipoPost fromString(String string) {
                return null;
            }
        });

        choiceType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedType = newVal;
                if (newVal == TipoPost.TESTO) {
                    selectedFile = null;
                    mediaPreview.getChildren().clear();
                    mediaPreview.getChildren().add(noMediaLbl);
                    mediaPreview.setStyle("");
                } else if (newVal == TipoPost.FOTO) {
                    noMediaLbl.setText("Clicca per caricare una foto 📸");
                } else if (newVal == TipoPost.VIDEO) {
                    noMediaLbl.setText("Clicca per caricare un video 🎬");
                }
            }
        });

        // Rendiamo l'intera area cliccabile per il caricamento
        mediaPreview.setOnMouseClicked(e -> {
            TipoPost currentType = choiceType.getValue();
            if (currentType == TipoPost.TESTO)
                return;

            FileChooser fc = new FileChooser();
            if (currentType == TipoPost.FOTO) {
                fc.getExtensionFilters()
                        .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            } else {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.m4v"));
            }

            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                selectedFile = f;
                mediaPreview.getChildren().clear();
                if (currentType == TipoPost.FOTO) {
                    ImageView iv = new ImageView(new Image(f.toURI().toString()));
                    iv.setPreserveRatio(true);
                    iv.setFitHeight(110);
                    mediaPreview.getChildren().add(iv);
                } else {
                    Label vLbl = new Label("🎬 " + f.getName());
                    vLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");
                    mediaPreview.getChildren().add(vLbl);
                }
                mediaPreview.setStyle("-fx-border-color: #6B8E23; -fx-background-color: #F1F8E9;");
            }
        });

        centerBox.getChildren().addAll(txtTitle, optionsBox, txtContent, mediaPreview);
        root.setCenter(centerBox);

        // --- BOTTOM: Actions ---
        HBox actionsBar = new HBox(15);
        actionsBar.getStyleClass().add("dialog-actions");

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("header-action-btn");
        btnCancel.setStyle("-fx-text-fill: #8D6E63; -fx-font-weight: normal;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnPost = new Button("Pubblica Post");
        btnPost.setId("publishBtn");
        btnPost.getStyleClass().add("button-primary");
        btnPost.setMinWidth(150);

        btnPost.setOnAction(e -> {
            String txt = txtContent.getText().trim();
            if (txt.isEmpty() && selectedFile == null) {
                DialogUtils.showWarning("Attenzione", "Inserisci del testo o un media.", dialog);
                return;
            }

            String mediaPath = null;
            if (selectedFile != null) {
                mediaPath = MediaManager.copyMediaToFolder(selectedFile);
                if (mediaPath == null) {
                    DialogUtils.showError("Errore File", "Impossibile salvare il media.", dialog);
                    return;
                }
            }

            Post newPost = new Post();
            newPost.setContenuto(txt);
            newPost.setAutore(utente);
            newPost.setTipo(selectedType);

            if (mediaPath != null) {
                newPost.setMedia(mediaPath);
            }

            if (!comboCat.getSelectionModel().isEmpty()) {
                newPost.setCategoria(comboCat.getSelectionModel().getSelectedItem());
            }

            try {
                String title = txtTitle.getText().trim();
                if (title.isEmpty()) {
                    DialogUtils.showWarning("Manca qualcosa!", "Il titolo è obbligatorio per pubblicare.", dialog);
                    return;
                }
                newPost.setTitolo(title);

                postService.createPost(newPost);
                onPostCreated.run();
                dialog.close();
            } catch (BusinessException ex) {
                DialogUtils.showError("Errore Pubblicazione", ex.getMessage(), dialog);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionsBar.getChildren().addAll(spacer, btnCancel, btnPost);
        root.setBottom(actionsBar);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static String getEmojiForCategory(String nome) {
        if (nome == null)
            return "📌";
        switch (nome.toLowerCase()) {
            case "torrefattori":
                return "☕";
            case "miscele":
                return "☕";
            case "guide":
                return "📖";
            default:
                return "📌";
        }
    }
}
