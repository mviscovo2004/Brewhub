package it.univaq.brewhub.UI;

import it.univaq.brewhub.Post;
import it.univaq.brewhub.Utente;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.util.List;

public class UserProfileView {
    private final Stage stage;
    private final Utente currentUser;
    private final Utente profileUser;

    // DAO
    private final it.univaq.brewhub.dao.UtenteDAO utenteDAO = new it.univaq.brewhub.dao.impl.UtenteDAOImpl();
    private final it.univaq.brewhub.dao.PostDAO postDAO = new it.univaq.brewhub.dao.impl.PostDAOImpl();

    // List of active PostCards for resource management
    private final java.util.List<it.univaq.brewhub.UI.components.PostCard> activeCards = new java.util.ArrayList<>();

    public UserProfileView(Stage stage, Utente currentUser, Utente profileUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.profileUser = profileUser;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Main Container con scroll
        VBox mainContent = new VBox(0);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: #FFFBF5;"); // Sfondo pagina

        // --- HEADER SECTION PROFILO ---
        StackPane profileHeader = new StackPane();
        profileHeader.setAlignment(Pos.BOTTOM_CENTER);

        // 1. Cover Image
        Region cover = new Region();
        cover.setPrefHeight(200);
        cover.setMaxWidth(800);
        cover.getStyleClass().add("profile-cover");

        // 2. Info Container
        VBox infoContainer = new VBox(10);
        infoContainer.getStyleClass().add("profile-info-container");
        infoContainer.setMaxWidth(800);
        infoContainer.setAlignment(Pos.TOP_CENTER);

        Label lblUsername = new Label(profileUser.getUsername());
        lblUsername.getStyleClass().add("profile-username-large");

        Label lblName = new Label(profileUser.getNome() + " " + profileUser.getCognome());
        lblName.getStyleClass().add("profile-bio");

        // Action Button
        Button followBtn = new Button();
        followBtn.getStyleClass().addAll("button", "button-primary");
        followBtn.setPrefWidth(120);

        // Stats Box
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10, 0, 10, 0));

        Label lblFollowersNum = new Label("?");
        lblFollowersNum.getStyleClass().add("profile-stat-number");
        Label lblFollowersTxt = new Label("Followers");
        lblFollowersTxt.getStyleClass().add("profile-stat-label");
        VBox boxFollowers = new VBox(2, lblFollowersNum, lblFollowersTxt);
        boxFollowers.getStyleClass().add("profile-stat-box");

        Label lblFollowingNum = new Label("?");
        lblFollowingNum.getStyleClass().add("profile-stat-number");
        Label lblFollowingTxt = new Label("Following");
        lblFollowingTxt.getStyleClass().add("profile-stat-label");
        VBox boxFollowing = new VBox(2, lblFollowingNum, lblFollowingTxt);
        boxFollowing.getStyleClass().add("profile-stat-box");

        statsBox.getChildren().addAll(boxFollowers, boxFollowing);

        // Assemblaggio info
        infoContainer.getChildren().addAll(lblUsername, lblName, statsBox);
        if (currentUser.getTipo() != Utente.TipoUtente.OSPITE) {
            infoContainer.getChildren().add(followBtn);
        }

        // Logica pulsante e contatori
        updateProfileLogic(followBtn, lblFollowersNum, lblFollowingNum);

        // 3. Foto Profilo
        Circle avatarClip = new Circle(60);
        ImageView avatarIv = new ImageView();
        avatarIv.setFitWidth(120);
        avatarIv.setFitHeight(120);
        avatarIv.setPreserveRatio(true);
        avatarIv.setClip(avatarClip);

        Image img = loadProfileImage(profileUser.getFotoProfilo());
        avatarIv.setImage(img);

        StackPane avatarContainer = new StackPane(avatarIv);
        avatarContainer.setMaxSize(120, 120);
        avatarContainer.setTranslateY(60);

        VBox layersUnder = new VBox(0, cover, infoContainer);
        layersUnder.setAlignment(Pos.TOP_CENTER);

        StackPane headerStack = new StackPane();
        headerStack.setAlignment(Pos.TOP_CENTER);
        headerStack.getChildren().addAll(layersUnder, avatarContainer);
        StackPane.setMargin(avatarContainer, new Insets(140, 0, 0, 0));

        profileHeader.getChildren().add(headerStack);

        // --- CONTENT SECTION (POSTS) ---
        VBox postsBox = new VBox(15);
        postsBox.setMaxWidth(800);
        postsBox.setPadding(new Insets(30, 20, 20, 20));

        Label postsTitle = new Label("Post Recenti");
        postsTitle.getStyleClass().add("section-title");
        postsBox.getChildren().add(postsTitle);

        loadPosts(postsBox);

        mainContent.getChildren().addAll(profileHeader, postsBox);

        ScrollPane sp = new ScrollPane(mainContent);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(sp);

        // Top Navigation
        Button btnHome = new Button("← Torna alla Home");
        btnHome.getStyleClass().add("button-secondary");
        btnHome.setOnAction(e -> {
            stopAllPlayers();
            HomeView hv = new HomeView(stage, currentUser);
            stage.getScene().setRoot(hv.getView());
        });
        HBox navBar = new HBox(btnHome);
        navBar.setPadding(new Insets(10));
        navBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(navBar);

        return root;
    }

    private Image loadProfileImage(String foto) {
        if (foto != null && !foto.isBlank()) {
            try {
                if (foto.startsWith("http") || foto.startsWith("file:"))
                    return new Image(foto, true);
                File f = new File(foto);
                if (f.exists())
                    return new Image(f.toURI().toString(), true);
                java.net.URL res = getClass().getResource(foto);
                if (res != null)
                    return new Image(res.toString(), true);
            } catch (Exception ignored) {
            }
        }
        try {
            return new Image(getClass().getResource("/media/default-profile.png").toString(), true);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateProfileLogic(Button followBtn, Label lblFoll, Label lblFollng) {
        Runnable refresh = () -> {
            try {
                lblFoll.setText(String.valueOf(utenteDAO.getFollowersCount(profileUser.getUsername())));
                lblFollng.setText(String.valueOf(utenteDAO.getFollowingCount(profileUser.getUsername())));

                if (currentUser.getUsername().equals(profileUser.getUsername())) {
                    followBtn.setText("Modifica");
                    followBtn.setOnAction(e -> {
                        stopAllPlayers();
                        ProfileView pv = new ProfileView(stage, currentUser);
                        stage.getScene().setRoot(pv.getView());
                    });
                } else {
                    boolean isFollowing = utenteDAO.isFollowing(currentUser.getUsername(), profileUser.getUsername());
                    followBtn.setText(isFollowing ? "Non seguire più" : "Segui");
                    followBtn.getStyleClass().removeAll("button-primary", "button-secondary");
                    followBtn.getStyleClass().add(isFollowing ? "button-secondary" : "button-primary");

                    followBtn.setOnAction(e -> {
                        try {
                            if (isFollowing)
                                utenteDAO.unfollow(currentUser.getUsername(), profileUser.getUsername());
                            else
                                utenteDAO.follow(currentUser.getUsername(), profileUser.getUsername());

                            // Refresh logic inline or reload view
                            // Here we just reload the view to be safe and simple
                            stopAllPlayers();
                            stage.getScene().setRoot(new UserProfileView(stage, currentUser, profileUser).getView());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
    }

    private void loadPosts(VBox container) {
        stopAllPlayers();

        try {
            List<Post> posts = postDAO.findByAuthor(profileUser.getUsername());
            if (posts.isEmpty()) {
                Label empty = new Label("Nessun post da mostrare.");
                empty.setStyle("-fx-text-fill: #A1887F; -fx-padding: 20;");
                container.getChildren().add(empty);
                return;
            }
            for (Post p : posts) {
                it.univaq.brewhub.UI.components.PostCard card = new it.univaq.brewhub.UI.components.PostCard(p,
                        currentUser, () -> loadPosts(container));
                activeCards.add(card);
                container.getChildren().add(card);
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("Errore caricamento post." + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void stopAllPlayers() {
        if (activeCards == null)
            return;
        for (it.univaq.brewhub.UI.components.PostCard card : activeCards) {
            card.dispose();
        }
        activeCards.clear();
    }
}
