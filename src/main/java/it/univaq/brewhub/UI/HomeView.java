package it.univaq.brewhub.UI;

import it.univaq.brewhub.UI.components.PostCard;

import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.dao.UtenteDAO;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;

import java.util.List;
import java.sql.SQLException;

import it.univaq.brewhub.Post;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.Categoria;
import java.io.File;
import java.time.format.DateTimeFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Gestisce la schermata principale dell'applicazione (Home).
 * Mostra il feed dei post, la sidebar di navigazione e il modulo per creare
 * nuovi post.
 */
public class HomeView {

    /** Riferimento allo stage principale dell'applicazione. */
    private final Stage stage;
    /** Oggetto utente attualmente loggato. */
    private final Utente utenteLoggato;

    /** DAO per gestione Post. */
    private final PostDAOImpl postDAO = new PostDAOImpl();

    /** DAO per gestione Utenti. */
    private final it.univaq.brewhub.dao.impl.UtenteDAOImpl utenteDAO = new it.univaq.brewhub.dao.impl.UtenteDAOImpl();
    /** DAO per gestione Notifiche. */
    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();
    /** DAO per gestione Categorie. */
    private final it.univaq.brewhub.dao.impl.CategoriaDAOImpl categoriaDAO = new it.univaq.brewhub.dao.impl.CategoriaDAOImpl();

    /** Contenitore VBox per il layout del feed dei post. */
    private VBox feedLayout;

    /** Lista di PostCard attivi per gestire il ciclo di vita (es. stop video). */
    private final java.util.List<PostCard> activeCards = new java.util.ArrayList<>();

    private String currentSection = "Home"; // Default active section

    private Button btnSavedPosts;
    private VBox sidebarContent;

    /**
     * 
     * /**
     * Costruttore della HomeView.
     *
     * @param stage         Lo stage principale dell'applicazione.
     * @param utenteLoggato L'utente attualmente loggato.
     */
    public HomeView(Stage stage, Utente utenteLoggato) {
        this.stage = stage;
        this.utenteLoggato = utenteLoggato;
    }

    /**
     * Costruisce e restituisce l'interfaccia grafica della Home.
     * Configura header, sidebar e area centrale del feed.
     *
     * @return Il nodo radice della vista (BorderPane).
     */
    public Parent getView() {
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setTitle("BrewHub - Home");
        stage.centerOnScreen();

        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("\u2615 BrewHub");
        logo.getStyleClass().add("header-logo");

        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Cerca post o utenti...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("text-field");

        ContextMenu searchDropdown = new ContextMenu();
        searchDropdown.setStyle(
                "-fx-background-color: #FFFBF5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                searchDropdown.hide();
                return;
            }
            try {
                // Ricerca "live" (potrebbe essere ottimizzata con debounce in futuro)
                List<Utente> results = utenteDAO.searchByUsername(newVal);
                searchDropdown.getItems().clear();

                if (results.isEmpty()) {
                    searchDropdown.hide();
                } else {
                    for (Utente u : results) {
                        HBox itemBox = new HBox(10);
                        itemBox.setAlignment(Pos.CENTER_LEFT);
                        itemBox.setPadding(new Insets(5));

                        Circle avatar = new Circle(15);

                        avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));

                        Label lblUser = new Label(u.getUsername());
                        lblUser.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");

                        itemBox.getChildren().addAll(avatar, lblUser);

                        CustomMenuItem item = new CustomMenuItem(itemBox);
                        item.setHideOnClick(true);
                        item.setOnAction(ev -> {
                            stopAllPlayers(); // Clean up before navigation
                            UserProfileView upv = new UserProfileView(stage, utenteLoggato, u);
                            stage.getScene().setRoot(upv.getView());
                        });
                        searchDropdown.getItems().add(item);
                    }
                    if (!searchDropdown.isShowing()) {
                        searchDropdown.show(searchField, Side.BOTTOM, 0, 0);
                    }
                }
            } catch (SQLException ex) {
                Log.error("Errore ricerca live", ex);
            }
        });

        searchField.setOnAction(e -> {
            searchDropdown.hide();
            performSearch(searchField.getText());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNewPost = new Button("\u2795 Nuovo Post");
        btnNewPost.getStyleClass().addAll("button", "header-action-btn");
        btnNewPost.setOnAction(e -> openCreatePostWindow());

        // --- Notifiche ---
        Button btnNotifiche = new Button("\uD83D\uDD14");
        btnNotifiche.getStyleClass().addAll("button", "notification-btn");

        ContextMenu notifDropdown = new ContextMenu();
        notifDropdown.getStyleClass().add("notification-context-menu");

        // Aggiorna badge notifiche
        Runnable refreshBadge = () -> {
            try {
                int count = notificaDAO.getUnreadCount(utenteLoggato.getUsername());
                if (count > 0) {
                    btnNotifiche.setText("\uD83D\uDD14 " + count);
                    if (!btnNotifiche.getStyleClass().contains("has-notifications")) {
                        btnNotifiche.getStyleClass().add("has-notifications");
                    }
                } else {
                    btnNotifiche.setText("\uD83D\uDD14");
                    btnNotifiche.getStyleClass().remove("has-notifications");
                }
            } catch (SQLException ex) {
                // Log.error("Errore badge notifiche", ex);
            }
        };
        refreshBadge.run();

        btnNotifiche.setOnAction(e -> {
            try {
                // Ricarica notifiche dal DB
                List<it.univaq.brewhub.Notifica> notifiche = notificaDAO.findByUser(utenteLoggato.getUsername());
                notifDropdown.getItems().clear();

                if (notifiche.isEmpty()) {
                    Label emptyLbl = new Label("Nessuna notifica");
                    emptyLbl.getStyleClass().add("notification-empty");
                    CustomMenuItem emptyItem = new CustomMenuItem(emptyLbl);
                    emptyItem.setHideOnClick(false);
                    notifDropdown.getItems().add(emptyItem);
                } else {
                    for (it.univaq.brewhub.Notifica n : notifiche) {
                        VBox itemBox = new VBox(5);
                        itemBox.getStyleClass().add("notification-box");
                        itemBox.setPrefWidth(280);

                        if (!n.isLetto()) {
                            itemBox.getStyleClass().add("unread");
                        }

                        Label dateLbl = new Label(
                                n.getDataCreazione().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
                        dateLbl.getStyleClass().add("notification-date");

                        Label msgLbl = new Label(n.getMessaggio());
                        msgLbl.getStyleClass().add("notification-message");
                        msgLbl.setWrapText(true);

                        itemBox.getChildren().addAll(dateLbl, msgLbl);

                        CustomMenuItem item = new CustomMenuItem(itemBox);
                        item.setHideOnClick(false);

                        // Click su notifica -> Segna come letto
                        itemBox.setOnMouseClicked(ev -> {
                            if (!n.isLetto()) {
                                try {
                                    notificaDAO.markAsRead(n.getId());
                                    n.setLetto(true);
                                    itemBox.getStyleClass().remove("unread");
                                    refreshBadge.run();
                                } catch (SQLException ex) {
                                    Log.error("Errore markAsRead", ex);
                                }
                            }
                        });

                        notifDropdown.getItems().add(item);
                    }
                }
                notifDropdown.show(btnNotifiche, Side.BOTTOM, 0, 0);
            } catch (SQLException ex) {
                Log.error("Errore loading notifiche", ex);
            }
        });

        Button profileBtn = new Button("\uD83D\uDC64 " + utenteLoggato.getUsername());
        profileBtn.getStyleClass().addAll("button", "header-profile-btn");
        profileBtn.setOnAction(e -> {
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                showAlert(AlertType.WARNING, "Accesso Limitato", "Devi registrarti per personalizzare il tuo profilo.");
                return;
            }
            stopAllPlayers(); // Clean up before navigation
            UserProfileView profileView = new UserProfileView(stage, utenteLoggato, utenteLoggato);
            stage.getScene().setRoot(profileView.getView());
        });

        Button logoutBtn = new Button("\uD83D\uDEAA Logout");
        logoutBtn.getStyleClass().addAll("button", "header-action-btn", "logout-btn");
        logoutBtn.setOnAction(e -> {
            stopAllPlayers(); // Clean up before navigation
            LoginView login = new LoginView(stage);
            stage.getScene().setRoot(login.getView());
        });

        if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
            header.getChildren().addAll(logo, searchField, spacer, profileBtn, logoutBtn);
        } else {
            header.getChildren().addAll(logo, searchField, spacer, btnNewPost, btnNotifiche, profileBtn, logoutBtn);
        }

        sidebarContent = new VBox(0);
        sidebarContent.setPrefWidth(260);
        sidebarContent.getStyleClass().add("sidebar");
        refreshSidebar();

        ScrollPane sidebarScroll = new ScrollPane(sidebarContent);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.getStyleClass().add("sidebar-scroll");

        feedLayout = new VBox(20);
        feedLayout.setPadding(new Insets(20));
        feedLayout.setAlignment(Pos.TOP_CENTER);

        // Dashboard rimossa dal layout fisso

        loadFeed();

        ScrollPane feedScroll = new ScrollPane(feedLayout);
        feedScroll.setFitToWidth(true);
        feedScroll.getStyleClass().add("scroll-pane");

        root.setTop(header);
        root.setLeft(sidebarScroll);
        root.setCenter(feedScroll);

        return root;
    }

    private void openCreatePostWindow() {
        if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
            showAlert(AlertType.WARNING, "Accesso Limitato", "Gli ospiti non possono pubblicare.");
            return;
        }

        Stage postStage = new Stage();
        postStage.setTitle("Crea Nuovo Post");
        postStage.initOwner(stage);
        postStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #FFFBF5;");
        layout.setPrefWidth(500);

        Label titleLbl = new Label("Scrivi qualcosa...");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");

        TextField fldTitolo = new TextField();
        fldTitolo.setPromptText("Titolo del post");
        fldTitolo.getStyleClass().add("text-field");

        TextArea postArea = new TextArea();
        postArea.setPromptText("Cosa stai pensando?");
        postArea.setPrefRowCount(5);
        postArea.getStyleClass().add("text-area");

        ChoiceBox<Post.TipoPost> cbxTipo = new ChoiceBox<>();
        cbxTipo.getItems().setAll(TipoPost.values());
        cbxTipo.setValue(TipoPost.TESTO);
        cbxTipo.getStyleClass().add("choice-box");

        // Categoria Selector - Defined early for usage in publishAction
        ChoiceBox<Categoria> cbxCategoria = new ChoiceBox<>();
        try {
            cbxCategoria.getItems().setAll(categoriaDAO.findAll());
        } catch (SQLException ex) {
            Log.error("Errore caricamento categorie", ex);
        }
        cbxCategoria.getStyleClass().add("choice-box");
        cbxCategoria.setPrefWidth(200);

        HBox mediaInfoBox = new HBox(10);
        mediaInfoBox.setAlignment(Pos.CENTER_LEFT);

        cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            mediaInfoBox.getChildren().clear();
            mediaInfoBox.setUserData(null);
            if (newVal == TipoPost.FOTO || newVal == TipoPost.VIDEO) {
                Button btnUpload = new Button(
                        newVal == TipoPost.FOTO ? "\uD83D\uDCF7 Carica Foto" : "\uD83C\uDFA5 Carica Video");
                btnUpload.getStyleClass().add("button-secondary");
                Label lblFile = new Label("Nessun file selezionato");
                lblFile.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

                btnUpload.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    fc.getExtensionFilters()
                            .add(newVal == TipoPost.FOTO
                                    ? new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg")
                                    : new FileChooser.ExtensionFilter("Video", "*.mp4"));
                    File f = fc.showOpenDialog(postStage);
                    if (f != null) {
                        MediaManager.initMediaFolder();
                        String path = MediaManager.copyMediaToFolder(f);
                        if (path != null) {
                            mediaInfoBox.setUserData(path);
                            lblFile.setText(f.getName());
                        }
                    }
                });
                mediaInfoBox.getChildren().addAll(btnUpload, lblFile);
            }
        });

        Button btnPublish = new Button("Pubblica");
        btnPublish.getStyleClass().add("button-success");
        btnPublish.setMaxWidth(Double.MAX_VALUE);

        Runnable publishAction = () -> {
            String titolo = fldTitolo.getText();
            String content = postArea.getText();
            TipoPost tipo = cbxTipo.getValue();
            String mediaPath = (String) mediaInfoBox.getUserData();

            if (titolo.isBlank()) {
                showAlert(AlertType.ERROR, "Errore", "Il titolo è obbligatorio.");
                return;
            }
            if (tipo != TipoPost.TESTO && mediaPath == null) {
                showAlert(AlertType.ERROR, "Errore", "Devi caricare un file multimediale per questo tipo di post.");
                return;
            }

            try {
                Post p = new Post(titolo, content, utenteLoggato, tipo, mediaPath);
                p.setCategoria(cbxCategoria.getValue());

                postDAO.create(p);

                // Aggiorna feed principale
                loadFeed();
                postStage.close();
                showAlert(AlertType.INFORMATION, "Successo", "Post pubblicato!");

            } catch (SQLException ex) {
                showAlert(AlertType.ERROR, "Errore Database", ex.getMessage());
            }
        };

        btnPublish.setOnAction(e -> publishAction.run());

        postArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    postArea.insertText(postArea.getCaretPosition(), "\n");
                    event.consume();
                } else {
                    event.consume();
                    publishAction.run();
                }
            }
        });

        HBox catBox = new HBox(10, new Label("Categoria:"), cbxCategoria);
        catBox.setAlignment(Pos.CENTER_LEFT);

        layout.getChildren().addAll(titleLbl, fldTitolo, postArea, new Label("Tipo Post:"), cbxTipo, catBox,
                mediaInfoBox,
                btnPublish);

        javafx.scene.Scene scene = new javafx.scene.Scene(layout);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
            /* Ignora se non trova CSS */ }

        postStage.setScene(scene);
        postStage.showAndWait();
    }

    private void loadFeed() {
        if (feedLayout == null)
            return;

        setActiveSection("Home");
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("\uD83C\uDFE0 Home Feed");
        feedLayout.getChildren().add(title);

        try {
            List<Post> posts = postDAO.findAll();
            if (posts.isEmpty()) {
                VBox emptyState;
                if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
                    VBox content = new VBox(15);
                    content.setAlignment(Pos.CENTER);
                    content.setPadding(new Insets(40));

                    Label lblTitle = new Label("Il tuo feed è vuoto \uD83C\uDF43");
                    lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
                    Label lblSub = new Label("Sii il primo a rompere il ghiaccio! Condividi la tua esperienza.");
                    lblSub.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-text-alignment: center;");

                    Button btnCreate = new Button("\u2795 Crea il Primo Post");
                    btnCreate.getStyleClass().add("button-primary");
                    btnCreate.setStyle("-fx-font-size: 16px; -fx-padding: 10 20;");
                    btnCreate.setOnAction(e -> openCreatePostWindow());

                    content.getChildren().addAll(lblTitle, lblSub, btnCreate);
                    emptyState = content;
                } else {
                    emptyState = createEmptyStateNode("Nessun post trovato",
                            "Non ci sono post da mostrare al momento.");
                }
                feedLayout.getChildren().add(emptyState);
            } else {
                for (Post p : posts) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeed, this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            Log.error("Errore feed home", e);
        }
    }

    private void loadFeedPopular() {
        if (feedLayout == null)
            return;
        setActiveSection("Popolari");
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("\uD83D\uDD25 Post Popolari");
        feedLayout.getChildren().add(title);

        try {
            List<Post> posts = postDAO.findPopular();
            if (posts.isEmpty()) {
                feedLayout.getChildren().add(createEmptyStateNode(
                        "Brrr... che freddo! \u2744",
                        "Non ci sono ancora post 'Popolari'.\nMetti like per scaldare l'atmosfera!"));
            } else {
                for (Post p : posts) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeedPopular,
                            this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            Log.error("Errore feed popolari", e);
        }
    }

    private void loadFeedFollowed() {
        if (feedLayout == null)
            return;
        setActiveSection("Followed");
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("\uD83D\uDC65 Post Seguiti");
        feedLayout.getChildren().add(title);

        if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
            feedLayout.getChildren().add(new Label("Accedi per vedere i post delle persone che segui."));
            return;
        }

        try {
            List<Post> posts = postDAO.findFeedForUser(utenteLoggato.getUsername());
            if (posts.isEmpty()) {
                feedLayout.getChildren().add(createEmptyStateNode(
                        "Ancora vuoto \uD83C\uDF43",
                        "Segui altri utenti per vedere qui i loro post!"));
            } else {
                for (Post p : posts) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeedFollowed,
                            this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            Log.error("Errore feed seguiti", e);
        }
    }

    // loadFeedLiked uses existing DAO call renamed/wrapped
    private void loadFeedLiked() {
        if (feedLayout == null)
            return;
        setActiveSection("MiPiace");
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("\u2764 Post che ti piacciono");
        feedLayout.getChildren().add(title);

        try {
            List<Post> posts = postDAO.findLikedBy(utenteLoggato.getUsername());
            if (posts.isEmpty()) {
                feedLayout.getChildren().add(createEmptyStateNode(
                        "Nessun Like \uD83D\uDC94",
                        "Non hai ancora messo mi piace a nessun post.\nMostra un po' di amore!"));
            } else {
                for (Post p : posts) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeedLiked, this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            Log.error("Errore feed likes", e);
        }
    }

    private void loadFeedByCategory(Categoria c) {
        if (feedLayout == null)
            return;

        setActiveSection(c.getNome());
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("Categoria: " + c.getNome());
        feedLayout.getChildren().add(title);

        try {
            List<Post> posts = postDAO.findByCategory(c.getId());
            if (posts.isEmpty()) {
                feedLayout.getChildren().add(createEmptyStateNode(
                        "Categoria vuota \uD83D\uDCC2",
                        "Nessun post in questa categoria.\nSii il primo a pubblicare qui!"));
            } else {
                for (Post p : posts) {
                    PostCard card = new PostCard(p, utenteLoggato, () -> loadFeedByCategory(c),
                            this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Errore Feed", e.getMessage());
        }
    }

    private void loadFeedByTorrefattori() {
        if (feedLayout == null)
            return;

        setActiveSection("Torrefattori");
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("\u2615 Post dai Torrefattori");
        feedLayout.getChildren().add(title);

        try {
            // Try fetching by Enum name (e.g. "TORREFATTORE")
            List<Post> posts = postDAO.findByUserType(Utente.TipoUtente.TORREFATTORE.name());

            // Fallback to label (e.g. "Torrefattore") if empty (handling DB variability)
            if (posts.isEmpty()) {
                posts = postDAO.findByUserType(Utente.TipoUtente.TORREFATTORE.toString());
            }

            if (posts.isEmpty()) {
                feedLayout.getChildren().add(createEmptyStateNode(
                        "Silenzio in torrefazione \u2615",
                        "I nostri Torrefattori stanno tostando...\nNessun aggiornamento al momento."));
            } else {
                for (Post p : posts) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeedByTorrefattori,
                            this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Errore Feed", e.getMessage());
        }
    }

    private void loadFeedBySaved() {
        if (feedLayout == null)
            return;

        setActiveSection("Salvati");
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("\u2B50 Post Salvati");
        feedLayout.getChildren().add(title);

        try {
            List<Post> posts = utenteDAO.getArchive(utenteLoggato.getUsername());
            if (posts.isEmpty()) {
                feedLayout.getChildren().add(createEmptyStateNode(
                        "Nessun post salvato \u2B50",
                        "Salva i post che ti piacciono per ritrovarli qui!"));
            } else {
                for (Post p : posts) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeedBySaved,
                            this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Errore Feed", e.getMessage());
        }
    }

    private void updateSavedPostsCounter() {
        if (btnSavedPosts == null)
            return;
        try {
            int count = utenteDAO.getNumSavedPosts(utenteLoggato.getUsername());
            btnSavedPosts.setText("\u2B50  Salvati (" + count + ")");
        } catch (SQLException e) {
            Log.error("Errore aggiornamento counter salvati", e);
        }
    }

    private Button creaNavButton(String text, boolean isActive) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-btn");
        if (isActive)
            btn.getStyleClass().add("nav-btn-active");
        return btn;
    }

    private void addSeparator(VBox container) {
        Region sep = new Region();
        sep.getStyleClass().add("custom-separator");
        sep.setMinHeight(1);
        VBox box = new VBox(sep);
        box.setPadding(new Insets(10, 20, 10, 20));
        container.getChildren().add(box);
    }

    private VBox createEmptyStateNode(String title, String subtitle) {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(40));

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-text-alignment: center;");

        emptyState.getChildren().addAll(lblTitle, lblSub);
        return emptyState;
    }

    private void performSearch(String query) {
        if (query == null || query.isBlank()) {
            loadFeed();
            return;
        }

        // Clear feed layout
        feedLayout.getChildren().clear();
        stopAllPlayers();
        // Clear active section because searches are cross-cutting
        // Ideally we might want a visually "deselected" sidebar, or highlight "Home" if
        // we consider search global.
        // For now, let's keep current section or just not touch it, but visually it's a
        // new "page".

        Label title = createHeaderLabel("\uD83D\uDD0D Risultati per: \"" + query + "\"");
        feedLayout.getChildren().add(title);

        boolean foundSomething = false;

        try {
            // 1. Cerca Utenti
            List<Utente> userResults = utenteDAO.searchByUsername(query);
            if (!userResults.isEmpty()) {
                foundSomething = true;

                Label lblUsers = new Label("UTENTI");
                lblUsers.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: #6d4c41; -fx-padding: 10 0 5 0; -fx-font-size: 14px;");
                feedLayout.getChildren().add(lblUsers);

                VBox usersContainer = new VBox(10);
                for (Utente u : userResults) {
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    // More premium card style
                    row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4,0,0,2); -fx-cursor: hand;");
                    row.setMaxWidth(Double.MAX_VALUE);

                    // Interaction: hover effect
                    row.setOnMouseEntered(e -> row
                            .setStyle("-fx-background-color: #fafafa; -fx-padding: 15; -fx-background-radius: 12; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 6,0,0,3); -fx-cursor: hand;"));
                    row.setOnMouseExited(e -> row
                            .setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4,0,0,2); -fx-cursor: hand;"));

                    // Action: Clicking row opens profile
                    row.setOnMouseClicked(e -> {
                        stopAllPlayers();
                        UserProfileView upv = new UserProfileView(stage, utenteLoggato, u);
                        stage.getScene().setRoot(upv.getView());
                    });

                    Circle avatar = new Circle(24);
                    // Use u.getFotoUri() if we had logic to load it, fallback to initial
                    String initial = u.getUsername().substring(0, 1).toUpperCase();
                    Label initLbl = new Label(initial);
                    initLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
                    StackPane avStack = new StackPane(avatar, initLbl);
                    avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));

                    VBox info = new VBox(4);
                    Label usernameLbl = new Label("@" + u.getUsername());
                    usernameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3E2723;");
                    Label nameLbl = new Label(u.getNome() + " " + u.getCognome());
                    nameLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #795548;");
                    info.getChildren().addAll(usernameLbl, nameLbl);

                    Region sp = new Region();
                    HBox.setHgrow(sp, Priority.ALWAYS);

                    // Optional arrow or clean look
                    Label arrow = new Label("\u276F"); // Chevron right
                    arrow.setStyle("-fx-text-fill: #bbb; -fx-font-size: 18px;");

                    row.getChildren().addAll(avStack, info, sp, arrow);
                    usersContainer.getChildren().add(row);
                }
                feedLayout.getChildren().add(usersContainer);
            }

            // 2. Cerca Post
            List<Post> postResults = postDAO.search(query);
            if (!postResults.isEmpty()) {
                foundSomething = true;
                // Separatore
                if (!userResults.isEmpty()) {
                    Region sep = new Region();
                    sep.setMinHeight(30);
                    feedLayout.getChildren().add(sep);
                }

                Label lblPosts = new Label("POST CORRELATI");
                lblPosts.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: #6d4c41; -fx-padding: 10 0 5 0; -fx-font-size: 14px;");
                feedLayout.getChildren().add(lblPosts);

                for (Post p : postResults) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeed, this::updateSavedPostsCounter);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }

            if (!foundSomething) {
                VBox emptyBox = new VBox(20);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(50));

                Label noRes = new Label("\uD83D\uDD0D Nessun risultato trovato");
                noRes.setStyle("-fx-font-size: 20px; -fx-text-fill: #aaa; -fx-font-weight: bold;");

                Label sugg = new Label("Prova con parole chiave diverse o cerca un altro utente.");
                sugg.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");

                emptyBox.getChildren().addAll(noRes, sugg);
                feedLayout.getChildren().add(emptyBox);
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Errore Ricerca", e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Stoppa e rilascia le risorse di tutti i player video attivi.
     * Da chiamare prima di cambiare view (logout, navigazione) per evitare
     * MediaException.
     */
    private void performUserManagementSearch() {
        if (feedLayout == null)
            return;
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label title = createHeaderLabel("\uD83D\uDC64 Gestione Utenti");
        feedLayout.getChildren().add(title);

        try {
            // Empty string searches for ALL users
            List<Utente> allUsers = utenteDAO.searchByUsername("");
            if (allUsers.isEmpty()) {
                feedLayout.getChildren().add(new Label("Nessun utente trovato."));
                return;
            }

            VBox usersContainer = new VBox(10);
            for (Utente u : allUsers) {
                if (u.getUsername().startsWith("deleted_"))
                    continue;

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                // consistent styling
                row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4,0,0,2);");
                row.setMaxWidth(Double.MAX_VALUE);

                Circle avatar = new Circle(24);
                String initial = u.getUsername().isEmpty() ? "?" : u.getUsername().substring(0, 1).toUpperCase();
                Label initLbl = new Label(initial);
                initLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
                StackPane avStack = new StackPane(avatar, initLbl);
                avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));

                VBox info = new VBox(4);
                Label usernameLbl = new Label("@" + u.getUsername() + " [" + u.getTipo() + "]");
                usernameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3E2723;");
                Label nameLbl = new Label(u.getNome() + " " + u.getCognome());
                nameLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #795548;");
                info.getChildren().addAll(usernameLbl, nameLbl);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                HBox actions = new HBox(10);

                Button btnProfile = new Button("Visita");
                btnProfile.getStyleClass().add("button-secondary");
                btnProfile.setOnAction(e -> {
                    UserProfileView upv = new UserProfileView(stage, utenteLoggato, u);
                    stage.getScene().setRoot(upv.getView());
                });

                actions.getChildren().add(btnProfile);

                // Add Delete Button if not self
                if (!u.getUsername().equals(utenteLoggato.getUsername())
                        && utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN
                        && u.getTipo() != Utente.TipoUtente.ADMIN) {
                    Button btnDelete = new Button("Elimina");
                    btnDelete.getStyleClass().add("button-danger");
                    btnDelete.setStyle("-fx-background-color: #e57373; -fx-text-fill: white;");

                    btnDelete.setOnAction(e -> {
                        Alert alert = new Alert(AlertType.CONFIRMATION, "Eliminare utente " + u.getUsername() + "?",
                                ButtonType.YES, ButtonType.NO);
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                try {
                                    utenteDAO.delete(u.getUsername());
                                    performUserManagementSearch(); // Refresh list
                                } catch (SQLException ex) {
                                    showAlert(AlertType.ERROR, "Errore Eliminazione", ex.getMessage());
                                }
                            }
                        });
                    });
                    actions.getChildren().add(btnDelete);
                }

                row.getChildren().addAll(avStack, info, sp, actions);
                usersContainer.getChildren().add(row);
            }
            feedLayout.getChildren().add(usersContainer);

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Errore", e.getMessage());
        }
    }

    private void stopAllPlayers() {
        if (activeCards == null)
            return;
        for (PostCard card : activeCards) {
            card.dispose();
        }
        activeCards.clear();
    }

    // --- CATEGORY MANAGEMENT ---
    private void openCategoryManagement() {
        if (feedLayout == null)
            return;
        feedLayout.getChildren().clear();
        stopAllPlayers();

        Label lblTitle = createHeaderLabel("\uD83D\uDCC2 Gestione Categorie");
        feedLayout.getChildren().add(lblTitle);

        VBox content = new VBox(20);
        content.setMaxWidth(800);

        // List area styled
        ListView<Categoria> listView = new ListView<>();
        listView.setPrefHeight(350);
        listView.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");

        // Custom Styled Cell Factory
        listView.setCellFactory(param -> new ListCell<Categoria>() {
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                updateStyle(selected);
            }

            private void updateStyle(boolean selected) {
                if (getGraphic() == null)
                    return;
                // Keep radius and shadow, toggle background
                getGraphic().setStyle(
                        "-fx-background-color: " + (selected ? "#ffe082" : "white") + ";" +
                                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2,0,0,1); -fx-padding: 10;");
            }

            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox cell = new HBox(15);
                    cell.setAlignment(Pos.CENTER_LEFT);

                    Label icon = new Label(item.getIcona() != null ? item.getIcona() : "\uD83D\uDCC2");
                    icon.setStyle("-fx-font-size: 24px;");

                    Label name = new Label(item.getNome());
                    name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");

                    cell.getChildren().addAll(icon, name);
                    setGraphic(cell);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");

                    // Apply initial state
                    updateStyle(isSelected());
                }
            }
        });

        Runnable refreshList = () -> {
            try {
                List<Categoria> cats = categoriaDAO.findAll();
                listView.getItems().setAll(cats);
                refreshSidebar();
            } catch (SQLException ex) {
                Log.error("Refresh cat error", ex);
            }
        };
        refreshList.run();

        // Editor Form
        VBox editorCard = new VBox(15);
        editorCard.setPadding(new Insets(20));
        editorCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4,0,0,2);");

        Label editorTitle = new Label("Aggiungi / Modifica Categoria");
        editorTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5D4037;");

        TextField tfName = new TextField();
        tfName.setPromptText("Nome Categoria");
        tfName.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 8;");

        TextField tfIcon = new TextField();
        tfIcon.setPromptText("Emoji");
        tfIcon.setPrefWidth(100);
        tfIcon.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 8;");

        Button btnPickEmoji = new Button("\uD83D\uDE00");
        btnPickEmoji.getStyleClass().add("button-secondary");
        btnPickEmoji.setOnAction(e -> showEmojiPicker(btnPickEmoji, selectedEmoji -> tfIcon.setText(selectedEmoji)));

        HBox inputs = new HBox(10, tfName, tfIcon, btnPickEmoji);
        inputs.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tfName, Priority.ALWAYS);

        Button btnSaveRef = new Button("Aggiungi");
        btnSaveRef.getStyleClass().add("button-success");
        btnSaveRef.setStyle("-fx-padding: 10 20; -fx-font-size: 14px;");

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setStyle("-fx-padding: 10 20; -fx-font-size: 14px;");
        btnCancel.setVisible(false);

        HBox actions = new HBox(10, btnSaveRef, btnCancel);
        actions.setAlignment(Pos.CENTER_RIGHT);

        editorCard.getChildren().addAll(editorTitle, inputs, actions);

        // Edit/Delete list actions
        HBox listActions = new HBox(10);
        listActions.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("\u270F Modifica");
        btnEdit.getStyleClass().add("button-primary");

        Button btnDelete = new Button("\uD83D\uDDD1 Elimina");
        btnDelete.getStyleClass().add("button-danger");

        listActions.getChildren().addAll(btnEdit, btnDelete);

        // Logic (State)
        final Categoria[] editingCat = { null };

        btnEdit.setOnAction(e -> {
            Categoria selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editingCat[0] = selected;
                tfName.setText(selected.getNome());
                tfIcon.setText(selected.getIcona() != null ? selected.getIcona() : "");
                btnSaveRef.setText("Salva Modifiche");
                btnCancel.setVisible(true);
                editorTitle.setText("Modifica Categoria: " + selected.getNome());
            }
        });

        btnCancel.setOnAction(e -> {
            editingCat[0] = null;
            tfName.clear();
            tfIcon.clear();
            btnSaveRef.setText("Aggiungi");
            btnCancel.setVisible(false);
            editorTitle.setText("Aggiungi / Modifica Categoria");
            listView.getSelectionModel().clearSelection();
        });

        btnSaveRef.setOnAction(e -> {
            String name = tfName.getText().trim();
            String icon = tfIcon.getText().trim();

            if (name.isBlank()) {
                showAlert(AlertType.WARNING, "Attenzione", "Il nome è obbligatorio.");
                return;
            }

            try {
                if (editingCat[0] == null) {
                    categoriaDAO.create(new Categoria(name, icon));
                } else {
                    editingCat[0].setNome(name);
                    editingCat[0].setIcona(icon);
                    categoriaDAO.update(editingCat[0]);
                    btnCancel.fire(); // Reset UI
                }
                tfName.clear();
                tfIcon.clear();
                refreshList.run();
            } catch (SQLException ex) {
                showAlert(AlertType.ERROR, "Errore", ex.getMessage());
            }
        });

        btnDelete.setOnAction(e -> {
            Categoria selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(AlertType.CONFIRMATION, "Eliminare " + selected.getNome() + "?",
                        ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        try {
                            categoriaDAO.delete(selected.getId());
                            if (editingCat[0] != null && editingCat[0].getId() == selected.getId()) {
                                btnCancel.fire();
                            }
                            refreshList.run();
                        } catch (SQLException ex) {
                            showAlert(AlertType.ERROR, "Errore", "Impossibile eliminare: " + ex.getMessage());
                        }
                    }
                });
            }
        });

        content.getChildren().addAll(listView, listActions, editorCard);
        feedLayout.getChildren().add(content);
    }

    private void refreshSidebar() {
        if (sidebarContent == null)
            return;
        sidebarContent.getChildren().clear();

        Label lblFeeds = new Label("FEEDS");
        lblFeeds.getStyleClass().add("sidebar-section-label");

        Button btnHome = creaNavButton("\uD83C\uDFE0  Home", "Home".equals(currentSection));
        btnHome.setOnAction(e -> loadFeed());

        Button btnPopular = creaNavButton("\uD83D\uDD25  Popolari", "Popolari".equals(currentSection));
        btnPopular.setOnAction(e -> loadFeedPopular());

        sidebarContent.getChildren().addAll(lblFeeds, btnHome, btnPopular);

        // Hide Followed feed for guests
        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            Button btnFollowed = creaNavButton("\uD83D\uDC65  Seguiti", "Followed".equals(currentSection));
            btnFollowed.setOnAction(e -> loadFeedFollowed());
            sidebarContent.getChildren().add(btnFollowed);
        }

        addSeparator(sidebarContent);

        try {
            java.util.List<Categoria> cats = categoriaDAO.findAll();

            Label lblComm = new Label("COMMUNITY");
            lblComm.getStyleClass().add("sidebar-section-label");
            sidebarContent.getChildren().add(lblComm);

            // Special Button for Torrefattori
            Button btnTorr = creaNavButton("\u2615  Torrefattori", "Torrefattori".equals(currentSection));
            btnTorr.setOnAction(e -> loadFeedByTorrefattori());
            sidebarContent.getChildren().add(btnTorr);

            for (Categoria c : cats) {
                if (c.getNome().equalsIgnoreCase("Torrefattori"))
                    continue;

                String icon = c.getIcona();
                if (icon == null || icon.isBlank()) {
                    icon = "\uD83D\uDCC2";
                    if (c.getNome().equalsIgnoreCase("Miscele"))
                        icon = "\uD83E\uDED8";
                    else if (c.getNome().equalsIgnoreCase("Eventi"))
                        icon = "\uD83C\uDF89";
                }

                Button btnCat = creaNavButton(icon + "  " + c.getNome(), c.getNome().equals(currentSection));
                btnCat.setOnAction(e -> loadFeedByCategory(c));
                sidebarContent.getChildren().add(btnCat);
            }
        } catch (SQLException ex) {
            sidebarContent.getChildren().add(new Label("Err caricamento cat."));
        }

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            addSeparator(sidebarContent);
            Label lblUser = new Label("IL TUO PROFILO");
            lblUser.getStyleClass().add("sidebar-section-label");

            Button btnProfile = creaNavButton("\uD83D\uDC64  Profilo", false);
            btnProfile.setOnAction(e -> {
                stopAllPlayers();
                UserProfileView profileView = new UserProfileView(stage, utenteLoggato, utenteLoggato);
                stage.getScene().setRoot(profileView.getView());
            });

            Button btnLikes = creaNavButton("\u2764  Mi piace", "MiPiace".equals(currentSection));
            btnLikes.setOnAction(e -> loadFeedLiked());

            sidebarContent.getChildren().addAll(lblUser, btnProfile, btnLikes);

            try {
                btnSavedPosts = creaNavButton(
                        "\u2B50  Salvati (" + utenteDAO.getNumSavedPosts(utenteLoggato.getUsername()) + ")",
                        "Salvati".equals(currentSection));
            } catch (SQLException e) {
                btnSavedPosts = creaNavButton("\u2B50  Salvati (Err)", false);
            }
            btnSavedPosts.setOnAction(e -> loadFeedBySaved());
            sidebarContent.getChildren().add(btnSavedPosts);

            if (utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
                addSeparator(sidebarContent);
                Label lblAdmin = new Label("AMMINISTRAZIONE");
                lblAdmin.getStyleClass().add("sidebar-section-label");

                Button btnManageUsers = creaNavButton("\uD83D\uDC65  Gestione Utenti",
                        "GestioneUtenti".equals(currentSection));
                btnManageUsers.setOnAction(e -> performUserManagementSearch());

                Button btnManageCats = creaNavButton("\uD83D\uDCC2  Gestione Categorie",
                        "GestioneCategorie".equals(currentSection));
                btnManageCats.setOnAction(e -> openCategoryManagement());

                sidebarContent.getChildren().addAll(lblAdmin, btnManageUsers, btnManageCats);
            }
        }
    }

    private void setActiveSection(String sectionName) {
        this.currentSection = sectionName;
        refreshSidebar();
    }

    private Label createHeaderLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-title");
        // Extra styling for "wow" factor
        lbl.setStyle(
                "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-padding: 0 0 10 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2,0,0,1);");
        return lbl;
    }

    private void showEmojiPicker(javafx.scene.Node owner, java.util.function.Consumer<String> onSelect) {
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        VBox box = new VBox(5);
        box.setStyle(
                "-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ccc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5,0,0,2);");

        Label lblT = new Label("Scegli Emoji");
        lblT.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        ScrollPane sp = new ScrollPane();
        sp.setPrefSize(250, 200);
        sp.setFitToWidth(true);

        FlowPane flow = new FlowPane();
        flow.setHgap(5);
        flow.setVgap(5);

        String[] emojis = {
                // Coffee & Drinks
                "\u2615", "\uD83E\uDEC8", "\uD83C\uDF75", "\uD83E\uDD64", "\uD83C\uDF7A", "\uD83C\uDF77",
                // Food
                "\uD83E\uDD50", "\uD83C\uDF70", "\uD83C\uDF6A", "\uD83C\uDF55",
                // Objects
                "\uD83D\uDCC2", "\uD83D\uDCC1", "\uD83D\uDCD6", "\uD83D\uDCDD", "\uD83D\uDCE6",
                "\uD83D\uDD25", "\uD83C\uDF89", "\uD83C\uDF81", "\u2B50", "\uD83D\uDCA1",
                // Nature
                "\uD83C\uDFE0", "\uD83C\uDFE2", "\uD83C\uDF31", "\uD83C\uDF33", "\uD83C\uDF0D",
                // Faces
                "\uD83D\uDE00", "\uD83D\uDE03", "\uD83D\uDE0E", "\uD83E\uDD13", "\uD83D\uDC4D"
        };

        for (String e : emojis) {
            Button b = new Button(e);
            b.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand;");
            b.setOnAction(ev -> {
                onSelect.accept(e);
                popup.hide();
            });
            flow.getChildren().add(b);
        }

        sp.setContent(flow);
        box.getChildren().addAll(lblT, sp);

        popup.getContent().add(box);

        javafx.geometry.Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
        popup.show(owner, bounds.getMinX(), bounds.getMaxY() + 5);
    }
}