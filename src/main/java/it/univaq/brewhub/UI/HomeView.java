package it.univaq.brewhub.UI;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import it.univaq.brewhub.Commento;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.MediaManager;

import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.CommentoDAOImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
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
    /** DAO per gestione Commenti. */
    private final CommentoDAOImpl commentoDAO = new CommentoDAOImpl();

    /** Contenitore VBox per il layout del feed dei post. */
    private VBox feedLayout;

    /**
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
        searchField.setPromptText("🔍 Cerca su BrewHub...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("text-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button profileBtn = new Button("👤 " + utenteLoggato.getUsername());
        profileBtn.getStyleClass().addAll("button", "header-profile-btn");
        profileBtn.setOnAction(e -> {
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                showAlert(AlertType.WARNING, "Accesso Limitato", "Devi registrarti per personalizzare il tuo profilo.");
                return;
            }
            ProfileView profileView = new ProfileView(stage, utenteLoggato);
            stage.getScene().setRoot(profileView.getView());
        });

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().add("button-danger");
        logoutBtn.setOnAction(e -> {
            LoginView login = new LoginView(stage);
            stage.getScene().setRoot(login.getView());
        });

        header.getChildren().addAll(logo, searchField, spacer, profileBtn, logoutBtn);

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

        VBox dashboard = createDashboard(feedLayout);

        feedLayout.getChildren().add(dashboard);
        loadFeed();

        ScrollPane feedScroll = new ScrollPane(feedLayout);
        feedScroll.setFitToWidth(true);
        feedScroll.getStyleClass().add("scroll-pane");

        root.setTop(header);
        root.setLeft(sidebarScroll);
        root.setCenter(feedScroll);

        return root;
    }

    private VBox createDashboard(VBox feedLayout) {
        VBox dashboard = new VBox(12);
        dashboard.getStyleClass().add("dashboard");
        dashboard.setMaxWidth(700);

        Label dashTitle = new Label("✍️ Crea un nuovo post");
        dashTitle.getStyleClass().addAll("label", "dashboard-title");

        TextField fldTitolo = new TextField();
        fldTitolo.setPromptText("Titolo del post...");
        fldTitolo.getStyleClass().add("text-field");
        fldTitolo.setId("fldTitolo");

        TextArea postArea = new TextArea();
        postArea.setPromptText("Scrivi qui il tuo post...");
        postArea.setPrefRowCount(3);
        postArea.getStyleClass().add("text-area");
        postArea.setId("postArea");

        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<Post.TipoPost> cbxTipo = new ChoiceBox<>();
        cbxTipo.getItems().setAll(TipoPost.values());
        cbxTipo.setValue(TipoPost.TESTO);
        cbxTipo.getStyleClass().add("choice-box");
        cbxTipo.setId("cbxTipo");

        HBox mediaInfoBox = new HBox(10);
        mediaInfoBox.setAlignment(Pos.CENTER_LEFT);

        cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            mediaInfoBox.getChildren().clear();
            mediaInfoBox.setUserData(null);
            if (newVal == TipoPost.FOTO || newVal == TipoPost.VIDEO) {
                Button btnUpload = new Button(newVal == TipoPost.FOTO ? "Carica Foto" : "Carica Video");
                btnUpload.getStyleClass().add("button-secondary");
                Label lblFile = new Label("Nessun file");
                btnUpload.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    fc.getExtensionFilters()
                            .add(newVal == TipoPost.FOTO
                                    ? new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg")
                                    : new FileChooser.ExtensionFilter("Video", "*.mp4"));
                    File f = fc.showOpenDialog(stage);
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

        Region dashSpacer = new Region();
        HBox.setHgrow(dashSpacer, Priority.ALWAYS);

        Button publishBtn = new Button("Pubblica");
        publishBtn.getStyleClass().add("button-success");
        publishBtn.setId("publishBtn");
        publishBtn.setOnAction(e -> {
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                showAlert(AlertType.WARNING, "Stop", "Gli ospiti non possono pubblicare.");
                return;
            }
            String titolo = fldTitolo.getText();
            String content = postArea.getText();
            TipoPost tipo = cbxTipo.getValue();
            String mediaPath = (String) mediaInfoBox.getUserData();

            if (titolo.isBlank()) {
                showAlert(AlertType.ERROR, "Errore", "Titolo mancante");
                return;
            }
            if (tipo != TipoPost.TESTO && mediaPath == null) {
                showAlert(AlertType.ERROR, "Errore", "Media mancante");
                return;
            }

            try {
                Post p = new Post(titolo, content, utenteLoggato, tipo, mediaPath);
                postDAO.create(p);

                loadFeed();

                fldTitolo.clear();
                postArea.clear();
                mediaInfoBox.getChildren().clear();
                cbxTipo.setValue(TipoPost.TESTO);

                showAlert(AlertType.INFORMATION, "Fatto", "Post pubblicato!");
            } catch (SQLException ex) {
                showAlert(AlertType.ERROR, "Errore DB", ex.getMessage());
            }
        });

        controlsBox.getChildren().addAll(cbxTipo, mediaInfoBox, dashSpacer, publishBtn);
        dashboard.getChildren().addAll(dashTitle, fldTitolo, postArea, controlsBox);
        return dashboard;
    }

    private void loadFeed() {
        if (feedLayout == null)
            return;
        // Keep header/dashboard (index 0)
        if (feedLayout.getChildren().size() > 1) {
            feedLayout.getChildren().remove(1, feedLayout.getChildren().size());
        }

        try {
            List<Post> posts = postDAO.findAll();
            for (Post p : posts) {
                feedLayout.getChildren().add(creaCardPost(p));
            }
        } catch (SQLException e) {
            Log.error("Errore caricamento feed", e);
            showAlert(AlertType.ERROR, "Errore Feed", "Impossibile caricare i post.");
        }
    }

    private VBox creaCardPost(Post post) {
        VBox card = new VBox(10);
        card.setMaxWidth(700);
        card.getStyleClass().add("post-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarContainer = new StackPane();
        Circle avatar = new Circle(20);
        boolean imageLoaded = false;

        try {
            String foto = post.getAutore().getFotoProfilo();
            if (foto != null && !foto.isBlank()) {
                File f = MediaManager.getMediaFile(foto);
                if (f != null && f.exists()) {
                    avatar.setFill(new javafx.scene.paint.ImagePattern(new Image(f.toURI().toString())));
                    imageLoaded = true;
                }
            }
        } catch (Exception e) {

        }

        if (!imageLoaded) {

            String username = post.getAutore().getUsername();

            int hash = username.hashCode();
            int r = (hash & 0xFF0000) >> 16;
            int g = (hash & 0x00FF00) >> 8;
            int b = hash & 0x0000FF;
            javafx.scene.paint.Color color = javafx.scene.paint.Color.rgb(Math.abs(r) % 255, Math.abs(g) % 255,
                    Math.abs(b) % 255);
            avatar.setFill(color);
            avatar.setOpacity(0.7);

            String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
            Label initialLbl = new Label(initial);
            initialLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
            avatarContainer.getChildren().addAll(avatar, initialLbl);
        } else {
            avatarContainer.getChildren().add(avatar);
        }

        Label authorLbl = new Label(post.getAutore().getUsername());
        authorLbl.setStyle("-fx-font-weight: bold;");

        Label dateLbl = new Label(post.getDataCreazione().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLbl.setStyle("-fx-font-size: 10px; -fx-opacity: 0.6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = null;
        if (post.getAutore().getUsername().equals(utenteLoggato.getUsername())) {
            btnDelete = new Button("🗑");
            btnDelete.getStyleClass().addAll("button", "button-danger", "button-small");
            btnDelete.setOnAction(e -> {
                Alert alert = new Alert(AlertType.CONFIRMATION, "Eliminare questo post?", ButtonType.YES,
                        ButtonType.NO);
                alert.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        try {
                            postDAO.delete(post.getId());
                            loadFeed();
                        } catch (SQLException ex) {
                            showAlert(AlertType.ERROR, "Errore", ex.getMessage());
                        }
                    }
                });
            });
        }

        if (btnDelete != null)
            header.getChildren().addAll(avatarContainer, authorLbl, dateLbl, spacer, btnDelete);
        else
            header.getChildren().addAll(avatarContainer, authorLbl, dateLbl, spacer);

        Label titleLbl = new Label(post.getTitolo());
        titleLbl.getStyleClass().add("post-title");

        card.getChildren().addAll(header, titleLbl);

        if (post.getTipo() == TipoPost.FOTO && post.getMedia() != null) {
            ImageView iv = new ImageView();
            caricaFoto(iv, post.getMedia());
            iv.setFitWidth(600);
            iv.setPreserveRatio(true);
            card.getChildren().add(new VBox(iv));
        } else if (post.getTipo() == TipoPost.VIDEO && post.getMedia() != null) {
            MediaView mv = new MediaView();
            mv.setFitWidth(600);
            mv.setPreserveRatio(true);
            caricaVideo(mv, post.getMedia());

            MediaPlayer mp = mv.getMediaPlayer();
            if (mp != null) {
                Button btnPlay = new Button("▶ Play");
                btnPlay.getStyleClass().add("button-secondary");
                btnPlay.setStyle("-fx-min-width: 80px;");

                btnPlay.setOnAction(e -> {
                    if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                        mp.pause();
                        btnPlay.setText("▶ Play");
                    } else {
                        mp.play();
                        btnPlay.setText("⏸ Pause");
                    }
                });

                mp.setOnEndOfMedia(() -> {
                    mp.stop();
                    btnPlay.setText("▶ Play");
                });

                Slider timeSlider = new Slider();
                HBox.setHgrow(timeSlider, Priority.ALWAYS);

                mp.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!timeSlider.isValueChanging()) {
                        timeSlider.setValue(newTime.toSeconds());
                    }
                });

                mp.setOnReady(() -> {
                    timeSlider.setMax(mp.getTotalDuration().toSeconds());
                });

                timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (timeSlider.isValueChanging()) {
                        mp.seek(Duration.seconds(newVal.doubleValue()));
                    }
                });

                timeSlider.setOnMouseClicked(e -> {
                    mp.seek(Duration.seconds(timeSlider.getValue()));
                });

                Label lblVol = new Label("🔊");
                Slider volSlider = new Slider(0, 1, 0.5);
                volSlider.setPrefWidth(80);
                mp.volumeProperty().bind(volSlider.valueProperty());

                HBox controls = new HBox(10, btnPlay, timeSlider, lblVol, volSlider);
                controls.setAlignment(Pos.CENTER);
                controls.setPadding(new Insets(5));

                VBox videoContainer = new VBox(10, mv, controls);
                videoContainer.setAlignment(Pos.TOP_CENTER);
                card.getChildren().add(videoContainer);
            } else {
                card.getChildren().add(new VBox(mv));
            }
        }

        if (post.getContenuto() != null) {
            Label content = new Label(post.getContenuto());
            content.setWrapText(true);
            content.getStyleClass().add("post-content");
            card.getChildren().add(content);
        }

        card.getChildren().add(new Separator());

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        boolean isLiked = false;
        int likes = 0;
        try {
            isLiked = postDAO.isLiked(post.getId(), utenteLoggato.getUsername());
            likes = postDAO.getLikesCount(post.getId());
        } catch (SQLException e) {
            Log.error("Errore refresh feed", e);
        }

        Button btnLike = new Button((isLiked ? "❤️ " : "🤍 ") + likes);
        btnLike.getStyleClass().add("like-button");
        if (isLiked)
            btnLike.getStyleClass().add("like-button-active");

        btnLike.setOnAction(e -> {
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE)
                return;
            try {
                boolean liked = postDAO.isLiked(post.getId(), utenteLoggato.getUsername());
                if (liked) {
                    postDAO.removeLike(post.getId(), utenteLoggato.getUsername());
                    btnLike.getStyleClass().remove("like-button-active");
                } else {
                    postDAO.addLike(post.getId(), utenteLoggato.getUsername());
                    if (!btnLike.getStyleClass().contains("like-button-active"))
                        btnLike.getStyleClass().add("like-button-active");
                }

                int newCount = postDAO.getLikesCount(post.getId());
                btnLike.setText((!liked ? "❤️ " : "🤍 ") + newCount);
            } catch (SQLException ex) {
                Log.error("Errore gestione like", ex);
            }
        });

        actions.getChildren().add(btnLike);
        card.getChildren().add(actions);

        VBox commentsBox = new VBox(5);
        commentsBox.getStyleClass().add("comments-box");
        VBox list = new VBox(5);

        for (Commento c : post.getCommenti()) {
            HBox commentRow = new HBox(10);
            commentRow.setAlignment(Pos.CENTER_LEFT);

            Label l = new Label(c.getUtente().getUsername() + ": " + c.getContenuto());
            l.setWrapText(true);
            HBox.setHgrow(l, Priority.ALWAYS);

            commentRow.getChildren().add(l);

            if (c.getUtente().getUsername().equals(utenteLoggato.getUsername())) {
                Button btnEdit = new Button("✎");
                btnEdit.setStyle("-fx-font-size: 10px; -fx-padding: 2 5;");
                btnEdit.getStyleClass().add("button-secondary");
                btnEdit.setOnAction(ev -> {
                    TextInputDialog dialog = new TextInputDialog(c.getContenuto());
                    dialog.setTitle("Modifica Commento");
                    dialog.setHeaderText(null);
                    dialog.setContentText("Modifica il commento:");

                    dialog.showAndWait().ifPresent(newText -> {
                        if (!newText.isBlank() && !newText.equals(c.getContenuto())) {
                            try {
                                c.setContenuto(newText);
                                commentoDAO.update(c);
                                l.setText(c.getUtente().getUsername() + ": " + newText);
                            } catch (SQLException ex) {
                                showAlert(AlertType.ERROR, "Errore", "Impossibile modificare: " + ex.getMessage());
                            }
                        }
                    });
                });

                Button btnDel = new Button("X");
                btnDel.setStyle(
                        "-fx-font-size: 10px; -fx-padding: 2 5; -fx-text-fill: white; -fx-background-color: #ff4444;");
                btnDel.setOnAction(ev -> {
                    Alert alert = new Alert(AlertType.CONFIRMATION, "Eliminare commento?", ButtonType.YES,
                            ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                commentoDAO.delete(c.getId());
                                list.getChildren().remove(commentRow);
                            } catch (SQLException ex) {
                                showAlert(AlertType.ERROR, "Errore", "Impossibile eliminare: " + ex.getMessage());
                            }
                        }
                    });
                });

                commentRow.getChildren().addAll(btnEdit, btnDel);
            }
            list.getChildren().add(commentRow);
        }

        HBox inputComm = new HBox(5);
        TextField tf = new TextField();
        tf.setPromptText("Commenta...");
        HBox.setHgrow(tf, Priority.ALWAYS);
        Button btnSend = new Button("Invia");
        btnSend.setOnAction(e -> {
            if (!tf.getText().isBlank()) {
                try {
                    Commento c = new Commento(utenteLoggato, post, tf.getText(), LocalDateTime.now());
                    commentoDAO.create(c);

                    Label l = new Label(utenteLoggato.getUsername() + ": " + c.getContenuto());
                    list.getChildren().add(l);
                    tf.clear();
                } catch (SQLException ex) {
                    Log.error("Errore inserimento commento", ex);
                }
            }
        });

        inputComm.getChildren().addAll(tf, btnSend);
        commentsBox.getChildren().addAll(list, inputComm);
        card.getChildren().add(commentsBox);

        return card;
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

    private void caricaFoto(ImageView view, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                File file = MediaManager.getMediaFile(path);
                if (file != null && file.exists()) {
                    view.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
        }
    }

    private void caricaVideo(MediaView view, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                File file = MediaManager.getMediaFile(path);
                if (file != null && file.exists()) {
                    MediaPlayer mp = new MediaPlayer(new Media(file.toURI().toString()));
                    view.setMediaPlayer(mp);

                }
            }
        } catch (Exception e) {
        }
    }

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}