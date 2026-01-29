package it.univaq.brewhub.view.admin;

import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.view.UserProfileView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vista della Dashboard per l'amministratore.
 * Mostra statistiche globali (utenti, post) e la lista degli utenti più attivi.
 */
public class AdminDashboardView extends VBox {

    private final Stage stage;
    private final Utente utenteLoggato;
    private final UserService userService = UserService.getInstance();
    private final PostService postService = PostService.getInstance();

    /**
     * Costruisce la vista della dashboard.
     *
     * @param stage         Lo stage dell'applicazione.
     * @param utenteLoggato L'utente amministratore attualmente loggato.
     */
    public AdminDashboardView(Stage stage, Utente utenteLoggato) {
        this.stage = stage;
        this.utenteLoggato = utenteLoggato;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente, caricando le statistiche e la lista dei top
     * contributor.
     */
    private void initUI() {
        this.setSpacing(20);
        this.setPadding(new Insets(20));

        Label title = new Label("📊 Dashboard Amministratore");
        title.getStyleClass().add("section-title");
        this.getChildren().add(title);

        try {
            int totalUsers = userService.getTotalUsersCount();
            int totalPosts = postService.getTotalPostsCount();
            int postsToday = postService.getPostsLast24hCount();

            HBox statsContainer = new HBox(20);
            statsContainer.setAlignment(Pos.CENTER);
            statsContainer.setPadding(new Insets(10, 0, 20, 0));
            statsContainer.getChildren().addAll(
                    createStatCard("Utenti Totali", String.valueOf(totalUsers), "👥", "#E3F2FD", "#1565C0"),
                    createStatCard("Post Totali", String.valueOf(totalPosts), "📝", "#E8F5E9", "#2E7D32"),
                    createStatCard("Post Oggi", String.valueOf(postsToday), "🔥", "#FFF3E0", "#EF6C00"));
            this.getChildren().add(statsContainer);

            Label lblTopUsers = new Label("Top Contributor");
            lblTopUsers.getStyleClass().addAll("text-bold-coffee");
            lblTopUsers.setStyle("-fx-padding: 10 0 5 0; -fx-font-size: 18px;");
            this.getChildren().add(lblTopUsers);

            List<Utente> topUsers = userService.getTopActiveUsers(5);
            if (topUsers.isEmpty()) {
                this.getChildren().add(new Label("Nessun dato disponibile."));
            } else {
                VBox usersContainer = new VBox(10);
                for (Utente u : topUsers) {
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("dashboard-user-row");
                    row.setMaxWidth(Double.MAX_VALUE);

                    Circle avatar = new Circle(24);
                    String initial = u.getUsername().isEmpty() ? "?" : u.getUsername().substring(0, 1).toUpperCase();
                    Label initLbl = new Label(initial);
                    initLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
                    StackPane avStack = new StackPane(avatar, initLbl);
                    avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));

                    VBox info = new VBox(4);
                    Label usernameLbl = new Label("@" + u.getUsername());
                    usernameLbl.getStyleClass().add("text-bold-coffee");
                    usernameLbl.setStyle("-fx-font-size: 16px;");
                    Label roleLbl = new Label(u.getTipo().toString());
                    roleLbl.getStyleClass().add("text-subtle-coffee");
                    roleLbl.setStyle("-fx-font-size: 14px;");
                    info.getChildren().addAll(usernameLbl, roleLbl);

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
                this.getChildren().add(usersContainer);
            }
        } catch (Exception e) {
            Log.error("Errore Dashboard", e);
            this.getChildren().add(new Label("Errore caricamento dashboard."));
        }
    }

    /**
     * Crea una card per visualizzare statistiche (es. Utenti totali).
     *
     * @param title     Titolo della statistica.
     * @param value     Valore della statistica.
     * @param icon      Icona da visualizzare.
     * @param bgColor   Colore di sfondo della card.
     * @param textColor Colore del testo.
     * @return VBox configurato come card statistica.
     */
    private VBox createStatCard(String title, String value, String icon, String bgColor, String textColor) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);");
        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 32px;");
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        card.getChildren().addAll(lblIcon, lblValue, lblTitle);
        return card;
    }
}
