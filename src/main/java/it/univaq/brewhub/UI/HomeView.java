package it.univaq.brewhub.UI;

// Importazioni JavaFX e classi del progetto
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

// Vista principale Home
public class HomeView {
    // Riferimento allo stage principale
    private final Stage stage;

    // Utente loggato
    private final Utente utenteLoggato;

    // Costruttore
    public HomeView(Stage stage, Utente utenteLoggato) {
        this.stage = stage;
        this.utenteLoggato = utenteLoggato;
    }

    // Metodo per ottenere la vista principale
    public Parent getView() {

        // Configurazione finestra
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setTitle("BrewHub - Home");
        stage.centerOnScreen();

        // --- LAYOUT PRINCIPALE ---
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // --- HEADER ---
        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo
        Label logo = new Label("☕ BrewHub");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Barra di ricerca
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca post o comunità...");
        searchField.setPrefWidth(350);
        searchField.getStyleClass().add("text-field");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bottone Profilo
        Button profileBtn = new Button("👤 " + utenteLoggato.getUsername());
        profileBtn.getStyleClass().add("button");
        profileBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;");

        // Azione bottone profilo
        profileBtn.setOnAction(e -> {

            // Controllo tipo utente
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {

                // Mostra avviso
                showAlert(AlertType.WARNING, "Accesso Limitato", "Devi registrarti per personalizzare il tuo profilo.");
                return;
            }

            // Vai alla vista profilo
            ProfileView profileView = new ProfileView(stage, utenteLoggato);
            stage.getScene().setRoot(profileView.getView());
        });

        // Bottone Logout
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().add("button-danger");

        // Azione bottone logout
        logoutBtn.setOnAction(e -> {

            // Torna alla vista di login
            LoginView login = new LoginView(stage);
            stage.getScene().setRoot(login.getView());
        });

        // Aggiunta elementi all'header
        header.getChildren().addAll(logo, searchField, spacer, profileBtn, logoutBtn);

        // --- SIDEBAR ---
        VBox sidebarContent = new VBox(0);
        sidebarContent.setPrefWidth(260);
        sidebarContent.getStyleClass().add("sidebar");

        // Sezione feed
        Label lblFeeds = new Label("FEEDS");
        lblFeeds.getStyleClass().add("sidebar-section-label");

        // Bottoni feed (home, popolari, tutti)
        Button btnHome = creaNavButton("🏠  Home", true);
        Button btnPopular = creaNavButton("🔥  Popolari", false);
        Button btnAll = creaNavButton("📈  Tutti", false);

        // Azioni simulate
        btnHome.setOnAction(e -> System.out.println("Vai a Home"));

        // Aggiunta elementi alla sidebar
        sidebarContent.getChildren().addAll(lblFeeds, btnHome, btnPopular, btnAll);

        // Separatore
        addSeparator(sidebarContent);

        // Sezione community
        Label lblComm = new Label("COMMUNITY");
        lblComm.getStyleClass().add("sidebar-section-label");

        // Bottoni community (torrefattori, miscele, eventi)
        Button btnTorrefattori = creaNavButton("☕  Torrefattori", false);
        Button btnMiscele = creaNavButton("🫘  Miscele", false);
        Button btnEventi = creaNavButton("🎉  Eventi", false);

        // Aggiunta elementi community alla sidebar
        sidebarContent.getChildren().addAll(lblComm, btnTorrefattori, btnMiscele, btnEventi);

        
       

        // Sezione profilo utente (se non ospite)
        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {

            // Separatore
             addSeparator(sidebarContent);

            // Scheda utente
            Label lblUser = new Label("IL TUO PROFILO");
            lblUser.getStyleClass().add("sidebar-section-label");

            // Bottoni profilo
            Button btnProfile = creaNavButton("👤  Profilo", false);

            // Azione bottone profilo
            btnProfile.setOnAction(e -> {

                // Vai alla vista profilo
                ProfileView profileView = new ProfileView(stage, utenteLoggato);
                stage.getScene().setRoot(profileView.getView());
            });

            // Bottoni i miei post e salvati
            Button btnMyPosts = creaNavButton("✍️  I miei post", false);
            Button btnSaved = creaNavButton("⭐  Salvati (" + utenteLoggato.getArchivio().size() + ")", false);

            // Aggiunta elementi profilo alla sidebar
            sidebarContent.getChildren().addAll(lblUser, btnProfile, btnMyPosts, btnSaved);
        }

        // Contenitore scrollabile per la sidebar
        ScrollPane sidebarScroll = new ScrollPane(sidebarContent);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.getStyleClass().add("sidebar-scroll");
        sidebarScroll.setStyle("-fx-background-color: transparent;");

        // --- FEED PRINCIPALE ---
        VBox feedLayout = new VBox(20);
        feedLayout.setPadding(new Insets(20));
        feedLayout.setAlignment(Pos.TOP_CENTER);

        // Dashboard creazione nuovo post
        VBox dashboard = new VBox(12);
        dashboard.getStyleClass().add("dashboard");
        dashboard.setMaxWidth(700);

        // Titolo dashboard
        Label dashTitle = new Label("✍️ Crea un nuovo post");
        dashTitle.getStyleClass().add("label");
        dashTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        // Campo titolo per il post
        TextField fldTitolo = new TextField();
        fldTitolo.setPromptText("Titolo del post...");
        fldTitolo.getStyleClass().add("text-field");

        // Area testo per il contenuto del post
        TextArea postArea = new TextArea();
        postArea.setPromptText("Scrivi qui il tuo post...");
        postArea.setPrefRowCount(3);
        postArea.getStyleClass().add("text-area");

        // Controlli post (tipo, media, pubblica)
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        // ChoiceBox tipo post
        ChoiceBox<Post.TipoPost> cbxTipo = new ChoiceBox<>();
        cbxTipo.getItems().setAll(TipoPost.values());
        cbxTipo.setValue(TipoPost.TESTO);
        cbxTipo.getStyleClass().add("choice-box");

        // Box per info media (file caricato)
        HBox mediaInfoBox = new HBox(10);
        mediaInfoBox.setAlignment(Pos.CENTER_LEFT);

        // Listener cambio tipo post
        cbxTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            // Reset info media
            mediaInfoBox.getChildren().clear();
            mediaInfoBox.setUserData(null);

            // Se tipo è foto o video, mostra opzione caricamento
            if (newVal == TipoPost.FOTO || newVal == TipoPost.VIDEO) {

                // Bottone caricamento file
                Button btnUpload = new Button(newVal == TipoPost.FOTO ? "Carica Foto" : "Carica Video");
                btnUpload.getStyleClass().add("button-secondary");

                // Label per mostrare nome file selezionato
                Label lblFile = new Label("Nessun file");

                // Azione bottone caricamento
                btnUpload.setOnAction(e -> {

                    // Apri file chooser
                    FileChooser fc = new FileChooser();

                    // Configura filtro in base al tipo
                    fc.setTitle(newVal == TipoPost.FOTO ? "Seleziona una foto" : "Seleziona un video");
                    if (newVal == TipoPost.FOTO)
                        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png"));
                    else
                        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));

                    // Mostra dialogo e ottieni file selezionato
                    File f = fc.showOpenDialog(stage);

                    // Se file selezionato, copialo nella cartella media e aggiorna UI
                    if (f != null) {

                        // Inizializza cartella media se necessario
                        MediaManager.initMediaFolder();
                        String path = MediaManager.copyMediaToFolder(f);

                        // Aggiorna info media
                        if (path != null) {
                            mediaInfoBox.setUserData(path);
                            lblFile.setText(f.getName());
                        }
                    }
                });

                // Aggiungi bottone e label al box info media
                mediaInfoBox.getChildren().addAll(btnUpload, lblFile);
            }
        });

        // Spacer orizzontale
        Region dashSpacer = new Region();
        HBox.setHgrow(dashSpacer, Priority.ALWAYS);

        // Bottone pubblica post
        Button publishBtn = new Button("Pubblica");
        publishBtn.getStyleClass().add("button-success");

        // Aggiunta controlli alla dashboard
        controlsBox.getChildren().addAll(cbxTipo, mediaInfoBox, dashSpacer, publishBtn);
        dashboard.getChildren().addAll(dashTitle, fldTitolo, postArea, controlsBox);

        // Azione bottone pubblica
        publishBtn.setOnAction(e -> {

            // Controllo tipo utente
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                // Ospiti non possono pubblicare
                showAlert(AlertType.WARNING, "Stop", "Gli ospiti non possono pubblicare.");
                return;
            }

            // Raccogli dati post
            String titolo = fldTitolo.getText();
            String content = postArea.getText();
            TipoPost tipo = cbxTipo.getValue();
            String mediaPath = (String) mediaInfoBox.getUserData();

            // Controllo titolo
            if (titolo.isBlank()) {
                // Titolo obbligatorio
                showAlert(AlertType.ERROR, "Errore", "Titolo mancante");
                return;
            }

            // Controllo media se necessario
            if (tipo != TipoPost.TESTO && mediaPath == null) {
                // Media obbligatorio
                showAlert(AlertType.ERROR, "Errore", "Media mancante");
                return;
            }

            // Crea e salva post
            try {
                // Crea post
                Post p = new Post(titolo, content, utenteLoggato, tipo,
                        mediaPath != null ? MediaManager.getMediaFile(mediaPath) : null);

                // Salva nel database
                p.salvaPost();

                // Aggiungi al feed in cima
                int index = feedLayout.getChildren().contains(dashboard)
                        ? feedLayout.getChildren().indexOf(dashboard) + 1
                        : 0;
                feedLayout.getChildren().add(index, creaCardPost(p));

                // Reset campi
                fldTitolo.clear();
                postArea.clear();
                mediaInfoBox.getChildren().clear();
                cbxTipo.setValue(TipoPost.TESTO);
                mediaInfoBox.setUserData(null);

                // Conferma pubblicazione
                showAlert(AlertType.INFORMATION, "Fatto", "Post pubblicato!");
            } catch (SQLException ex) {

                // Errore salvataggio
                showAlert(AlertType.ERROR, "Errore DB", ex.getMessage());
            }
        });

        // Aggiunta dashboard al feed
        feedLayout.getChildren().add(dashboard);

        // Caricamento post dal database
        caricaPostDalDatabase(feedLayout);

        // Contenitore scrollabile per il feed
        ScrollPane feedScroll = new ScrollPane(feedLayout);
        feedScroll.setFitToWidth(true);
        feedScroll.getStyleClass().add("scroll-pane");
        feedScroll.setStyle("-fx-background-color: transparent;");

        // Aggiunta sezioni al layout principale
        root.setTop(header);
        root.setLeft(sidebarScroll);
        root.setCenter(feedScroll);

        // Ritorna il layout completo
        return root;
    }

    // Metodo per creare la card di un post
    private VBox creaCardPost(Post post) {
        // Card post
        VBox card = new VBox(10);
        card.setMaxWidth(700);
        card.getStyleClass().add("post-card");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Autore e data
        Label authorLbl = new Label(post.getAutore().getUsername());
        authorLbl.getStyleClass().add("post-author");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLbl = new Label(post.getDataCreazione().format(fmt));
        dateLbl.getStyleClass().add("post-date");

        // Spacer orizzontale
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bottone salva post
        Button btnSave = new Button("Salva");
        btnSave.getStyleClass().add("save-button");

        // Azione bottone salva
        btnSave.setOnAction(e -> {
            // Controllo tipo utente
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                showAlert(AlertType.ERROR, "Stop", "Gli ospiti non possono salvare post.");
                return;
            }

            // Aggiungi post all'archivio se non già presente
            boolean exists = utenteLoggato.getArchivio().stream().anyMatch(p -> p.getTitolo().equals(post.getTitolo()));

            // Aggiorna UI se salvato
            if (!exists) {
                // Aggiungi al archivio
                utenteLoggato.getArchivio().add(post);
                btnSave.setText("Salvato");
                btnSave.getStyleClass().add("save-button-saved");
            }
        });

        // Aggiunta elementi all'header
        header.getChildren().addAll(authorLbl, dateLbl, spacer, btnSave);

        // Titolo post
        Label titleLbl = new Label(post.getTitolo());
        titleLbl.getStyleClass().add("post-title");

        // Aggiunta header e titolo alla card
        card.getChildren().addAll(header, titleLbl);

        // Media
        if (post.getTipo() == TipoPost.FOTO && post.getMedia() != null) {
            // Immagine
            try {
                // Caricamento immagine
                ImageView iv = new ImageView(new Image(post.getMedia().toURI().toString()));
                iv.setFitWidth(600);
                iv.setPreserveRatio(true);

                // Contenitore centrato
                VBox mediaBox = new VBox(iv);
                mediaBox.setAlignment(Pos.CENTER);

                // Aggiunta mediaBox alla card
                card.getChildren().add(mediaBox);
            } catch (Exception e) {
                // Errore caricamento immagine
                card.getChildren().add(new Label("Errore caricamento immagine: " + e.getMessage()));
            }
        } else if (post.getTipo() == TipoPost.VIDEO && post.getMedia() != null) {
            // Video
            try {
                // Caricamento video
                Media m = new Media(post.getMedia().toURI().toString());
                MediaPlayer mp = new MediaPlayer(m);
                MediaView mv = new MediaView(mp);
                mv.setFitWidth(600);
                mv.setPreserveRatio(true);

                // Controlli video
                HBox controls = new HBox(10);
                controls.setAlignment(Pos.CENTER);
                controls.setPadding(new Insets(5, 0, 5, 0));

                // Bottone Play
                Button btnPlay = new Button("▶ Play");
                btnPlay.getStyleClass().add("button-primary");

                // Slider Tempo
                Slider timeSlider = new Slider();
                HBox.setHgrow(timeSlider, Priority.ALWAYS);

                // Slider Volume
                Label lblVol = new Label("🔊");
                Slider volSlider = new Slider(0, 1, 0.5);
                volSlider.setPrefWidth(80);
                volSlider.getStyleClass().add("volume-slider");

                // Azione bottone Play/Pausa
                btnPlay.setOnAction(ev -> {
                    // Toggle Play/Pausa
                    if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                        // Pausa
                        mp.pause();
                        btnPlay.setText("▶ Play");
                    } else {
                        // Play
                        mp.play();
                        btnPlay.setText("⏸ Pausa");
                    }
                });

                // Aggiornamento automatico slider tempo
                mp.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    // Aggiorna slider solo se non in dragging
                    if (!timeSlider.isValueChanging()) {
                        timeSlider.setValue(newTime.toSeconds());
                    }
                });

                // Setup Max Slider quando il video è pronto
                mp.setOnReady(() -> {
                    // Imposta il massimo dello slider alla durata del video
                    timeSlider.setMax(m.getDuration().toSeconds());
                });

                // Seek manuale (trascinamento slider)
                timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    // Seek solo se l'utente sta interagendo con lo slider
                    if (timeSlider.isValueChanging()) {
                        // Esegui seek
                        mp.seek(Duration.seconds(newVal.doubleValue()));
                    }
                });

                // Seek al click
                timeSlider.setOnMouseClicked(e -> {
                    // Esegui seek
                    mp.seek(Duration.seconds(timeSlider.getValue()));
                });

                // Binding Volume
                mp.volumeProperty().bind(volSlider.valueProperty());

                // Aggiunta controlli al box
                controls.getChildren().addAll(btnPlay, timeSlider, lblVol, volSlider);

                // Aggiunta mediaBox alla card
                VBox mediaBox = new VBox(5, mv, controls);
                mediaBox.setAlignment(Pos.CENTER);

                // Aggiunta mediaBox alla card
                card.getChildren().add(mediaBox);
            } catch (Exception e) {
                // Errore caricamento video
                card.getChildren().add(new Label("Errore caricamento video: " + e.getMessage()));
            }
        }

        // Contenuto testuale
        if (post.getContenuto() != null && !post.getContenuto().isBlank()) {
            // Testo
            Label contentLbl = new Label(post.getContenuto());
            contentLbl.getStyleClass().add("post-content");
            contentLbl.setWrapText(true);

            // Aggiunta contenuto alla card
            card.getChildren().add(contentLbl);
        }

        // Separatore
        card.getChildren().add(new Separator());

        // Footer
        HBox actions = new HBox(15);

        // Mi Piace
        Button btnLike = new Button("🤍 " + post.getMiPiace().size());
        btnLike.getStyleClass().add("like-button");

        // Azione bottone Mi Piace
        btnLike.setOnAction(e -> {
            // Controllo tipo utente
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                // Ospiti non possono mettere mi piace
                showAlert(AlertType.ERROR, "Stop", "Gli ospiti non possono mettere mi piace.");
                return;
            }

            // Toggle Mi Piace
            boolean removed = post.getMiPiace().removeIf(u -> u.getUsername().equals(utenteLoggato.getUsername()));

            // Aggiungi se non rimosso
            if (!removed)
                post.getMiPiace().add(utenteLoggato);

            // Aggiorna testo bottone
            btnLike.setText((removed ? "🤍 " : "❤️ ") + post.getMiPiace().size());
        });

        // Aggiunta bottoni al footer
        actions.getChildren().add(btnLike);

        // Aggiunta footer alla card
        card.getChildren().add(actions);

        // Commenti
        VBox commentsBox = new VBox(5);
        commentsBox.getStyleClass().add("comments-box");

        // Lista commenti
        VBox commentsList = new VBox(5);

        // Funzione per aggiornare i commenti
        Runnable refreshComm = () -> {

            // Aggiorna lista commenti
            commentsList.getChildren().clear();

            // Aggiungi commenti esistenti
            for (Commento c : post.getCommenti()) {
                // Singolo commento
                String u = c.getUtente().getUsername();
                Label l = new Label(u + ": " + c.getContenuto());
                l.setWrapText(true);
                commentsList.getChildren().add(l);
            }
        };

        // Carica commenti iniziali
        refreshComm.run();

        // Input nuovo commento
        HBox commInput = new HBox(5);

        // Campo testo commento
        TextField tfComm = new TextField();
        tfComm.setPromptText("Commenta...");
        tfComm.getStyleClass().add("comment-field");
        HBox.setHgrow(tfComm, Priority.ALWAYS);

        // Bottone invia commento
        Button btnSend = new Button("Invia");
        btnSend.getStyleClass().add("button-primary");

        // Azione bottone invia commento
        btnSend.setOnAction(e -> {
            // Controllo tipo utente
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                // Ospiti non possono commentare
                showAlert(AlertType.ERROR, "Stop", "Gli ospiti non possono commentare.");
                return;
            }
            // Aggiungi commento se non vuoto
            if (!tfComm.getText().isBlank()) {
                // Aggiungi commento al post
                post.getCommenti().add(new Commento(utenteLoggato, post, tfComm.getText(), LocalDateTime.now()));
                tfComm.clear();
                refreshComm.run();
            }
        });
        // Aggiunta input commento al box
        commInput.getChildren().addAll(tfComm, btnSend);
        commentsBox.getChildren().addAll(commentsList, commInput);

        // Aggiunta box commenti alla card
        card.getChildren().add(commentsBox);

        // Ritorna la card completa
        return card;
    }

    // Metodo per caricare i post dal database
    private void caricaPostDalDatabase(VBox feed) {

        // Caricamento post
        try {

            // Ottieni tutti i post
            List<Post> posts = Post.caricaTuttiPost();

            // Aggiungi ogni post al feed
            for (Post p : posts)
                feed.getChildren().add(creaCardPost(p));
        } catch (SQLException e) {

            // Errore caricamento post
            e.printStackTrace();
        }
    }

    // Metodo helper per mostrare alert
    private void showAlert(AlertType type, String title, String msg) {

        // Mostra alert
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Metodo helper per creare i bottoni di navigazione
    private Button creaNavButton(String text, boolean isActive) {

        // Crea bottone
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-btn");

        // Stile attivo
        if (isActive) {
            btn.setStyle("-fx-background-color: #F5E6D3; -fx-border-color: transparent;");
        }

        // Ritorna il bottone
        return btn;
    }

    // Metodo helper per aggiungere separatori
    private void addSeparator(VBox container) {

        // Separatore
        Region sep = new Region();
        sep.setMinHeight(1);
        sep.setStyle("-fx-background-color: #edeff1; -fx-margin: 5 20 5 20;");

        // Contenitore per il separatore con padding
        VBox box = new VBox(sep);
        box.setPadding(new Insets(10, 20, 10, 20));

        // Aggiunta al container
        container.getChildren().add(box);
    }
}