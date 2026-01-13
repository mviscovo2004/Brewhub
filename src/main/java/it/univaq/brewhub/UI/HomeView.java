package it.univaq.brewhub.UI;

import it.univaq.brewhub.UI.components.PostCard;

import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.dao.impl.PostDAOImpl;

import java.util.List;
import java.sql.SQLException;

import it.univaq.brewhub.Post;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.MediaManager;
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

    /** Contenitore VBox per il layout del feed dei post. */
    private VBox feedLayout;

    /** Lista di PostCard attivi per gestire il ciclo di vita (es. stop video). */
    private final java.util.List<PostCard> activeCards = new java.util.ArrayList<>();

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

        Label logo = new Label("☕ BrewHub");
        logo.getStyleClass().add("header-logo");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca utenti...");
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

        Button btnNewPost = new Button("➕ Nuovo Post");
        btnNewPost.getStyleClass().addAll("button-primary");
        btnNewPost.setOnAction(e -> openCreatePostWindow());

        // --- Notifiche ---
        Button btnNotifiche = new Button("🔔");
        btnNotifiche.getStyleClass().addAll("button", "notification-btn");

        ContextMenu notifDropdown = new ContextMenu();
        notifDropdown.getStyleClass().add("notification-context-menu");

        // Aggiorna badge notifiche
        Runnable refreshBadge = () -> {
            try {
                int count = notificaDAO.getUnreadCount(utenteLoggato.getUsername());
                if (count > 0) {
                    btnNotifiche.setText("🔔 " + count);
                    if (!btnNotifiche.getStyleClass().contains("has-notifications")) {
                        btnNotifiche.getStyleClass().add("has-notifications");
                    }
                } else {
                    btnNotifiche.setText("🔔");
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

        Button profileBtn = new Button("👤 " + utenteLoggato.getUsername());
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

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().add("button-danger");
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

        VBox sidebarContent = new VBox(0);
        sidebarContent.setPrefWidth(260);
        sidebarContent.getStyleClass().add("sidebar");

        Label lblFeeds = new Label("FEEDS");
        lblFeeds.getStyleClass().add("sidebar-section-label");

        Button btnHome = creaNavButton("🏠  Home", true);
        btnHome.setOnAction(e -> loadFeed());

        Button btnPopular = creaNavButton("🔥  Popolari", false);
        Button btnAll = creaNavButton("📈  Tutti", false);
        sidebarContent.getChildren().addAll(lblFeeds, btnHome, btnPopular, btnAll);

        addSeparator(sidebarContent);

        Label lblComm = new Label("COMMUNITY");
        lblComm.getStyleClass().add("sidebar-section-label");
        sidebarContent.getChildren().addAll(lblComm, creaNavButton("☕  Torrefattori", false),
                creaNavButton("🫘  Miscele", false), creaNavButton("🎉  Eventi", false));

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            addSeparator(sidebarContent);
            Label lblUser = new Label("IL TUO PROFILO");
            lblUser.getStyleClass().add("sidebar-section-label");

            Button btnProfile = creaNavButton("👤  Profilo", false);
            btnProfile.setOnAction(e -> {
                stopAllPlayers(); // Clean up before navigation
                ProfileView profileView = new ProfileView(stage, utenteLoggato);
                stage.getScene().setRoot(profileView.getView());
            });

            sidebarContent.getChildren().addAll(lblUser, btnProfile, creaNavButton("✍️  I miei post", false),
                    creaNavButton("⭐  Salvati (" + utenteLoggato.getArchivio().size() + ")", false));
        }

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

        HBox mediaInfoBox = new HBox(10);
        mediaInfoBox.setAlignment(Pos.CENTER_LEFT);

        cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            mediaInfoBox.getChildren().clear();
            mediaInfoBox.setUserData(null);
            if (newVal == TipoPost.FOTO || newVal == TipoPost.VIDEO) {
                Button btnUpload = new Button(newVal == TipoPost.FOTO ? "📷 Carica Foto" : "🎥 Carica Video");
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

        layout.getChildren().addAll(titleLbl, fldTitolo, postArea, new Label("Tipo Post:"), cbxTipo, mediaInfoBox,
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

        feedLayout.getChildren().clear(); // Rimuove tutto, non c'è più dashboard fissa

        // Pulisce le card precedenti
        stopAllPlayers();

        try {
            List<Post> posts = postDAO.findAll();
            for (Post p : posts) {
                PostCard card = new PostCard(p, utenteLoggato, this::loadFeed);
                activeCards.add(card);
                feedLayout.getChildren().add(card);
            }
        } catch (SQLException e) {
            Log.error("Errore caricamento feed", e);
            showAlert(AlertType.ERROR, "Errore Feed", "Impossibile caricare i post.");
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

    private void performSearch(String query) {
        if (query == null || query.isBlank()) {
            loadFeed(); // Restore feed if empty search
            return;
        }

        // Clear feed layout
        feedLayout.getChildren().clear();

        Label title = new Label("Risultati ricerca per: \"" + query + "\"");
        title.getStyleClass().add("section-title");
        title.setStyle("-fx-font-size: 18px; -fx-padding: 10;");
        feedLayout.getChildren().add(title);

        boolean foundSomething = false;

        try {
            // 1. Cerca Utenti
            List<Utente> userResults = utenteDAO.searchByUsername(query);
            if (!userResults.isEmpty()) {
                foundSomething = true;
                Label lblUsers = new Label("Utenti trovati");
                lblUsers.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
                feedLayout.getChildren().add(lblUsers);

                VBox usersContainer = new VBox(10);
                for (Utente u : userResults) {
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle(
                            "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3,0,0,1);");
                    row.setMaxWidth(600);

                    Circle avatar = new Circle(20);
                    String initial = u.getUsername().substring(0, 1).toUpperCase();
                    Label initLbl = new Label(initial);
                    initLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    StackPane avStack = new StackPane(avatar, initLbl);
                    avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));

                    VBox info = new VBox(2);
                    Label usernameLbl = new Label("@" + u.getUsername());
                    usernameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    Label nameLbl = new Label(u.getNome() + " " + u.getCognome());
                    nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                    info.getChildren().addAll(usernameLbl, nameLbl);

                    Region sp = new Region();
                    HBox.setHgrow(sp, Priority.ALWAYS);

                    Button btnProfile = new Button("Visita");
                    btnProfile.getStyleClass().add("button-secondary");
                    btnProfile.setOnAction(e -> {
                        UserProfileView upv = new UserProfileView(stage, utenteLoggato, u);
                        stage.getScene().setRoot(upv.getView());
                    });

                    row.getChildren().addAll(avStack, info, sp, btnProfile);
                    usersContainer.getChildren().add(row);
                }
                feedLayout.getChildren().add(usersContainer);
            }

            // 2. Cerca Post
            List<Post> postResults = postDAO.search(query);
            if (!postResults.isEmpty()) {
                foundSomething = true;
                // Separatore se c'è già la sezione utenti
                if (!userResults.isEmpty()) {
                    Region sep = new Region();
                    sep.setMinHeight(20);
                    feedLayout.getChildren().add(sep);
                }

                Label lblPosts = new Label("Post trovati");
                lblPosts.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
                feedLayout.getChildren().add(lblPosts);

                for (Post p : postResults) {
                    PostCard card = new PostCard(p, utenteLoggato, this::loadFeed);
                    activeCards.add(card);
                    feedLayout.getChildren().add(card);
                }
            }

            if (!foundSomething) {
                feedLayout.getChildren().add(new Label("Nessun risultato trovato."));
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
    private void stopAllPlayers() {
        if (activeCards == null)
            return;
        for (PostCard card : activeCards) {
            card.dispose();
        }
        activeCards.clear();
    }
}