package it.univaq.brewhub.view;

import it.univaq.brewhub.business.CategoriaService;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.AsyncTaskHelper;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.view.admin.AdminDashboardView;
import it.univaq.brewhub.view.admin.CategoryManagementView;
import it.univaq.brewhub.view.admin.DatabaseToolsView;
import it.univaq.brewhub.view.admin.UserManagementView;
import it.univaq.brewhub.view.components.SidebarComponent;
import it.univaq.brewhub.view.components.VerificationBadge;
import it.univaq.brewhub.view.utils.FeedManager;
import it.univaq.brewhub.view.utils.PostCreationDialog;
import it.univaq.brewhub.view.utils.SearchManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vista principale (Home) dell'applicazione.
 * Funge da contenitore principale per il layout e gestisce la navigazione tra
 * le varie sezioni.
 *
 * <p>
 * Componenti principali:
 * <ul>
 * <li><b>Header:</b> Barra superiore con logo, ricerca, azioni rapide e menu
 * utente.</li>
 * <li><b>Sidebar:</b> Menu di navigazione laterale.</li>
 * <li><b>Content Area:</b> Area centrale scrollabile dove vengono mostrati i
 * feed o le viste specifiche.</li>
 * </ul>
 *
 * <p>
 * Delega la gestione dei feed a {@link FeedManager} e la ricerca a
 * {@link SearchManager}.
 */
public class HomeView {

    private final Stage stage;
    private final Utente utenteLoggato;

    private final UserService userService = UserService.getInstance();
    private final CategoriaService categoriaService = CategoriaService.getInstance();

    private SidebarComponent sidebar;
    private VBox feedLayout;
    private ScrollPane feedScroll;

    private FeedManager feedManager;
    private SearchManager searchManager;
    private it.univaq.brewhub.view.utils.NotificationManager notificationManager;

    /**
     * Costruisce la HomeView.
     *
     * @param stage         Lo stage principale.
     * @param utenteLoggato L'utente attualmente loggato.
     */
    public HomeView(Stage stage, Utente utenteLoggato) {
        this.stage = stage;
        this.utenteLoggato = utenteLoggato;
    }

    /**
     * Costruisce e restituisce l'interfaccia grafica della Home.
     * Configura layout, stili e inizializza i manager.
     *
     * @return Il nodo root (BorderPane) della vista.
     */
    public Parent getView() {
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setTitle("BrewHub - Home");
        stage.centerOnScreen();
        stage.requestFocus();

        BorderPane root = new BorderPane();
        try {
            root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            // Ignora se CSS non trovato
        }

        // Init Content Area
        feedLayout = new VBox(20);
        feedLayout.setPadding(new Insets(20));
        feedLayout.setAlignment(Pos.TOP_CENTER);

        feedScroll = new ScrollPane(feedLayout);
        feedScroll.setFitToWidth(true);
        feedScroll.getStyleClass().add("scroll-pane");

        // Init Managers
        // Callback refresh sidebar (es. contatori)
        feedManager = new FeedManager(utenteLoggato, feedLayout, feedScroll);
        feedManager.setOnRefreshCallback(this::updateSavedPostsCounter);

        searchManager = new SearchManager(stage, utenteLoggato, feedLayout, feedScroll, this::updateSavedPostsCounter);

        // --- HEADER ---
        HBox header = createHeader();

        // --- SIDEBAR ---
        sidebar = new SidebarComponent(utenteLoggato, this::handleNavigation);
        sidebar.setPrefWidth(260);

        // Caricamento Iniziale
        feedManager.loadFeed();

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(feedScroll);

        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("☕ BrewHub");
        logo.getStyleClass().add("header-logo");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca post o utenti...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("text-field");

        // Logica Ricerca Rapida
        setupSearchLogic(searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bottoni Header
        Button btnNewPost = new Button("➕ Nuovo Post");
        btnNewPost.getStyleClass().addAll("button", "header-action-btn");
        btnNewPost.setId("btnNewPost");
        btnNewPost.setOnAction(
                e -> PostCreationDialog.show(stage, utenteLoggato, CategoriaService.getInstance().getAllCategories(),
                        () -> feedManager.loadFeed()));

        Button btnNotifiche = new Button("🔔");
        btnNotifiche.getStyleClass().addAll("button", "notification-btn");

        notificationManager = new it.univaq.brewhub.view.utils.NotificationManager(stage, utenteLoggato, btnNotifiche);
        notificationManager.initialize();

        Button profileBtn = new Button("👤 " + utenteLoggato.getUsername());
        profileBtn.getStyleClass().addAll("button", "header-profile-btn");
        profileBtn.setOnAction(e -> {
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                DialogUtils.showWarning("Accesso Limitato",
                        "Devi registrarti per personalizzare il tuo profilo.", stage);
                return;
            }
            stopManagers();
            UserProfileView profileView = new UserProfileView(stage, utenteLoggato, utenteLoggato);
            stage.getScene().setRoot(profileView.getView());
        });

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().addAll("button", "header-action-btn", "logout-btn");
        logoutBtn.setOnAction(e -> {
            stopManagers();
            LoginView login = new LoginView(stage);
            stage.getScene().setRoot(login.getView());
        });

        if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
            header.getChildren().addAll(logo, searchField, spacer, profileBtn, logoutBtn);
        } else {
            header.getChildren().addAll(logo, searchField, spacer, btnNewPost, btnNotifiche, profileBtn, logoutBtn);
        }

        return header;
    }

    /**
     * Ferma i task in background dei manager (video, notifiche, ecc.).
     */
    private void stopManagers() {
        if (feedManager != null)
            feedManager.stopAllPlayers();
        if (notificationManager != null)
            notificationManager.shutdown();
    }

    private void setupSearchLogic(TextField searchField) {
        ContextMenu searchDropdown = new ContextMenu();
        searchDropdown.setStyle(
                "-fx-background-color: #FFFBF5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        javafx.animation.PauseTransition debounce = new javafx.animation.PauseTransition(
                javafx.util.Duration.millis(300));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            debounce.stop();
            if (newVal == null || newVal.isBlank()) {
                searchDropdown.hide();
                return;
            }

            debounce.setOnFinished(event -> {
                AsyncTaskHelper.<List<Utente>>runAsync(
                        () -> userService.searchUsers(newVal),
                        results -> {
                            searchDropdown.getItems().clear();
                            if (results.isEmpty()) {
                                MenuItem noResult = new MenuItem("Nessun utente trovato");
                                noResult.setDisable(true);
                                searchDropdown.getItems().add(noResult);
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

                                    if (u.getTipo() == Utente.TipoUtente.TORREFATTORE) {
                                        VerificationBadge badge = new VerificationBadge(12);
                                        itemBox.getChildren().add(badge);
                                    }

                                    CustomMenuItem item = new CustomMenuItem(itemBox);
                                    item.setHideOnClick(true);
                                    item.setOnAction(ev -> {
                                        stopManagers();
                                        UserProfileView upv = new UserProfileView(stage, utenteLoggato, u);
                                        stage.getScene().setRoot(upv.getView());
                                    });
                                    searchDropdown.getItems().add(item);
                                }
                            }
                            if (!searchDropdown.isShowing() && searchField.isFocused()) {
                                searchDropdown.show(searchField, Side.BOTTOM, 0, 0);
                            }
                        },
                        error -> Log.error("Errore ricerca utenti", error));
            });
            debounce.play();
        });

        searchField.setOnAction(e -> {
            searchDropdown.hide();
            restoreFeedView();
            searchManager.performSearch(searchField.getText());
        });
    }

    private void handleNavigation(String section) {
        if (section == null)
            return;

        restoreFeedView();

        switch (section) {
            case "Home":
                feedManager.loadFeed();
                return;
            case "Popolari":
                feedManager.loadFeedPopular();
                return;
            case "Followed":
                feedManager.loadFeedFollowed();
                return;
            case "Torrefattori":
                feedManager.loadFeedByTorrefattori();
                return;
            case "Guide": {
                // Logica specifica per categoria Guide
                try {
                    List<Categoria> cats = categoriaService.getAllCategories();
                    for (Categoria c : cats) {
                        if (c.getNome().equalsIgnoreCase("Guide")) {
                            feedManager.loadFeedByCategory(c);
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.error("Errore nel caricamento feed Guide", e);
                }
                feedLayout.getChildren().clear();
                feedLayout.getChildren().add(new Label("Categoria Guide non trovata."));
                return;
            }
            case "Eventi":
                loadEventsView();
                return;
            case "Sfide":
                loadSfideView();
                return;
            case "MiPiace":
                feedManager.loadFeedLiked();
                return;
            case "Salvati":
                feedManager.loadFeedBySaved();
                return;
            case "Messaggi":
                stopManagers(); // Cambio scena, quindi stop completo
                ChatView cv = new ChatView(stage, utenteLoggato, null);
                stage.getScene().setRoot(cv.getView());
                return;
            case "Profilo":
                stopManagers();
                UserProfileView profileView = new UserProfileView(stage, utenteLoggato, utenteLoggato);
                stage.getScene().setRoot(profileView.getView());
                return;
            case "GestioneUtenti":
                setActiveSection("GestioneUtenti");
                feedLayout.getChildren().clear();
                stopManagers(); // Anche se non cambio scena, fermo feed video
                feedLayout.getChildren().add(new UserManagementView(stage, utenteLoggato));
                return;
            case "GestioneCategorie":
                setActiveSection("GestioneCategorie");
                feedLayout.getChildren().clear();
                stopManagers();
                feedLayout.getChildren().add(new CategoryManagementView(stage, sidebar));
                return;
            case "Dashboard":
                setActiveSection("Dashboard");
                feedLayout.getChildren().clear();
                stopManagers();
                feedLayout.getChildren().add(new AdminDashboardView(stage, utenteLoggato));
                return;
            case "GestioneDB":
                setActiveSection("GestioneDB");
                feedLayout.getChildren().clear();
                stopManagers();
                feedLayout.getChildren().add(new DatabaseToolsView(stage));
                return;
            default:
                break;
        }

        // Controllo generico per categorie dinamiche
        try {
            List<Categoria> cats = categoriaService.getAllCategories();
            for (Categoria c : cats) {
                if (c.getNome().equals(section)) {
                    feedManager.loadFeedByCategory(c);
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    private void loadEventsView() {
        if (feedLayout == null)
            return;
        setActiveSection("Eventi");
        feedLayout.getChildren().clear();
        feedManager.stopAllPlayers();

        EventsView eventsView = new EventsView(utenteLoggato);
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        root.setCenter(eventsView);
    }

    private void loadSfideView() {
        if (feedLayout == null)
            return;
        setActiveSection("Sfide");
        feedLayout.getChildren().clear();
        feedManager.stopAllPlayers();

        SfideView sfideView = new SfideView(utenteLoggato);
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        root.setCenter(sfideView);
    }

    private void restoreFeedView() {
        if (stage.getScene() == null || stage.getScene().getRoot() == null)
            return;
        if (stage.getScene().getRoot() instanceof BorderPane) {
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            if (root.getCenter() != feedScroll) {
                root.setCenter(feedScroll);
            }
        }
    }

    private void setActiveSection(String sectionName) {
        if (sidebar != null)
            sidebar.setActiveSection(sectionName);
    }

    private void updateSavedPostsCounter() {
        if (sidebar != null)
            sidebar.refresh();
    }
}
