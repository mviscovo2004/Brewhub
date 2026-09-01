package it.univaq.brewhub.view;

import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Torrefattore;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.utility.MediaManager;
import it.univaq.brewhub.view.components.PostCard;
import it.univaq.brewhub.view.components.VerificationBadge;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Vista per visualizzare il profilo pubblico di un utente (diverso da quello
 * loggato).
 *
 * <p>
 * Componenti:
 * <ul>
 * <li>Header con avatar, username, badge e biografia/dati azienda.</li>
 * <li>Statistiche follower/following.</li>
 * <li>Pulsanti azione (Segui/Smetti di seguire, Messaggio, Elimina per
 * Admin).</li>
 * <li>Lista dei post pubblicati dall'utente.</li>
 * </ul>
 */
public class UserProfileView {

    private final Stage stage;
    private final Utente currentUser;
    private final Utente profileUser;

    private final UserService userService = UserService.getInstance();
    private final PostService postService = PostService.getInstance();

    private final List<PostCard> activeCards = new ArrayList<>();

    /**
     * Costruisce la vista profilo utente.
     *
     * @param stage       Lo stage principale.
     * @param currentUser L'utente che sta guardando il profilo.
     * @param profileUser L'utente di cui si sta guardando il profilo.
     */
    public UserProfileView(Stage stage, Utente currentUser, Utente profileUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.profileUser = profileUser;
    }

    /**
     * Costruisce e restituisce l'interfaccia.
     *
     * @return Il nodo root {@link Parent}.
     */
    public Parent getView() {
        BorderPane root = new BorderPane();
        try {
            root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            // Ignora
        }

        VBox mainContent = new VBox(0);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: #FFFBF5;");

        // Header Profilo
        StackPane profileHeader = createProfileHeader();

        // Sezione Post
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
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setCenter(sp);

        // Navbar Superiore
        Button btnHome = new Button("← Torna alla Home");
        btnHome.getStyleClass().add("button-secondary");
        btnHome.setOnAction(e -> {
            stopAllPlayers();
            // Torna alla home con l'utente corrente
            HomeView hv = new HomeView(stage, currentUser);
            stage.getScene().setRoot(hv.getView());
        });

        HBox navBar = new HBox(btnHome);
        navBar.setPadding(new Insets(10));
        navBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(navBar);

        return root;
    }

    private StackPane createProfileHeader() {
        StackPane profileHeader = new StackPane();
        profileHeader.setAlignment(Pos.BOTTOM_CENTER);

        // Copertina del profilo
        StackPane cover = new StackPane();
        cover.setPrefSize(800, 200);
        cover.setMinSize(800, 200);
        cover.setMaxSize(800, 200);
        cover.getStyleClass().add("profile-cover");

        Image coverImage = loadProfileImage(profileUser.getFotoProfilo());

        if (coverImage != null) {
            ImageView coverImageView = new ImageView(coverImage);

            coverImageView.setFitWidth(800);
            coverImageView.setFitHeight(200);

            // La foto occupa completamente il rettangolo della copertina
            coverImageView.setPreserveRatio(false);

            cover.getChildren().add(coverImageView);
        }

        VBox infoContainer = new VBox(10);
        infoContainer.getStyleClass().add("profile-info-container");
        infoContainer.setMaxWidth(800);
        infoContainer.setAlignment(Pos.TOP_CENTER);

        // Username e Badge
        Label lblUsername = new Label(profileUser.getUsername());
        lblUsername.getStyleClass().add("profile-username-large");
        HBox usernameBox = new HBox(8, lblUsername);
        usernameBox.setAlignment(Pos.CENTER);
        if (profileUser.getTipo() == Utente.TipoUtente.TORREFATTORE) {
            VerificationBadge badge = new VerificationBadge(20);
            usernameBox.getChildren().add(badge);
        }

        // Nome e Descrizione
        Label lblName = new Label();
        lblName.getStyleClass().add("profile-bio");
        Label lblDesc = null;

        if (profileUser.getTipo() == Utente.TipoUtente.TORREFATTORE) {
            try {
                Torrefattore tDetails = userService.getTorrefattoreDetails(profileUser.getUsername());
                if (tDetails != null && tDetails.getNomeAzienda() != null) {
                    lblName.setText(tDetails.getNomeAzienda());
                    if (tDetails.getDescrizione() != null && !tDetails.getDescrizione().isBlank()) {
                        lblDesc = new Label(tDetails.getDescrizione());
                        lblDesc.setStyle(
                                "-fx-font-style: italic; -fx-text-fill: #6D4C41; -fx-font-size: 14px; -fx-padding: 5 0;");
                    }
                } else {
                    lblName.setText(profileUser.getNome() + " " + profileUser.getCognome());
                    }
                } catch (Exception e) {
                lblName.setText(profileUser.getNome() + " " + profileUser.getCognome());
                }
        } else {
            lblName.setText(profileUser.getNome() + " " + profileUser.getCognome());
    }

        // Statistiche
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

        // Pulsanti Azione
        Button followBtn = new Button();
        followBtn.getStyleClass().addAll("button", "button-primary");
        followBtn.setPrefWidth(120);

        if (currentUser.getTipo() != Utente.TipoUtente.OSPITE) {
            HBox actionButtons = new HBox(10);
            actionButtons.setAlignment(Pos.CENTER);
            actionButtons.getChildren().add(followBtn);

            if (!currentUser.getUsername().equals(profileUser.getUsername())) {
                Button msgBtn = new Button("Messaggio");
                msgBtn.getStyleClass().add("button-secondary");
                msgBtn.setOnAction(e -> {
                    stopAllPlayers();
                    ChatView cv = new ChatView(stage, currentUser, profileUser.getUsername());
                    stage.getScene().setRoot(cv.getView());
                });
                actionButtons.getChildren().add(msgBtn);
            }
            infoContainer.getChildren().add(actionButtons);

            // Admin Actions
            if (currentUser.getTipo() == Utente.TipoUtente.ADMIN
                    && !currentUser.getUsername().equals(profileUser.getUsername())) {
                Button btnDeleteUser = new Button("⚠ Elimina Utente");
                btnDeleteUser.getStyleClass().add("button-danger");
                btnDeleteUser.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; -fx-margin-top: 10px;");
                btnDeleteUser.setOnAction(e -> {
                    boolean confirmed = DialogUtils.showConfirmation("SEI SICURO?",
                            "Eliminare @" + profileUser.getUsername() + "?", stage);
                    if (confirmed) {
                        try {
                            userService.deleteUser(profileUser.getUsername());
                            stopAllPlayers();
                            HomeView hv = new HomeView(stage, currentUser);
                            stage.getScene().setRoot(hv.getView());
                        } catch (Exception ex) {
                            DialogUtils.showError("Errore", ex.getMessage(), stage);
                        }
                    }
                });
                infoContainer.getChildren().add(btnDeleteUser);
            }
        }

        infoContainer.getChildren().addAll(usernameBox, lblName);
        if (lblDesc != null) {
            infoContainer.getChildren().add(lblDesc);
        }
        infoContainer.getChildren().add(statsBox);

        // Update Logica Statistiche e Bottone Follow
        updateProfileLogic(followBtn, lblFollowersNum, lblFollowingNum);

        // Foto Profilo (Avatar)
        Circle avatarClip = new Circle(60);
        ImageView avatarIv = new ImageView();
        avatarIv.setFitWidth(120);
        avatarIv.setFitHeight(120);
        avatarIv.setPreserveRatio(false);
        avatarIv.setClip(avatarClip);
        Image img = loadProfileImage(profileUser.getFotoProfilo());
        avatarIv.setImage(img);

        StackPane avatarContainer = new StackPane(avatarIv);
        avatarContainer.setMaxSize(120, 120);
        // TranslateY per sovrapporsi tra cover e info
        StackPane.setMargin(avatarContainer, new Insets(140, 0, 0, 0));

        VBox layersUnder = new VBox(0, cover, infoContainer);
        layersUnder.setAlignment(Pos.TOP_CENTER);

        StackPane headerStack = new StackPane();
        headerStack.setAlignment(Pos.TOP_CENTER);
        headerStack.getChildren().addAll(layersUnder, avatarContainer);

        profileHeader.getChildren().add(headerStack);
        return profileHeader;
    }

    private Image loadProfileImage(String foto) {
        if (foto != null && !foto.isBlank()) {
            try {
                if (foto.startsWith("http") || foto.startsWith("file:")) {
                    return new Image(foto, true);
                }

                File mediaFile = MediaManager.getMediaFile(foto);

                if (mediaFile != null && mediaFile.exists()) {
                    return new Image(mediaFile.toURI().toString(), true);
                }

                java.net.URL res = getClass().getResource(foto);

                if (res != null) {
                    return new Image(res.toString(), true);
                }

            } catch (Exception e) {
                Log.error("Errore durante il caricamento della foto profilo", e);
                }
        }

        try {
            return new Image(
                    getClass().getResource("/media/default-profile.png").toString(),
                    true);
        } catch (Exception e) {
            return null;
            }
    }

    private void updateProfileLogic(Button followBtn, Label lblFoll, Label lblFollng) {
        // Listener Liste Follower/Following
        VBox boxFoll = (VBox) lblFoll.getParent();
        boxFoll.setCursor(javafx.scene.Cursor.HAND);
        boxFoll.setOnMouseClicked(e -> {
            try {
                List<Utente> list = userService.getFollowers(profileUser.getUsername());
                DialogUtils.showUserListDialog("Followers", list, stage, selected -> {
                    stopAllPlayers();
                    UserProfileView upv = new UserProfileView(stage, currentUser, selected);
                    stage.getScene().setRoot(upv.getView());
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox boxFollng = (VBox) lblFollng.getParent();
        boxFollng.setCursor(javafx.scene.Cursor.HAND);
        boxFollng.setOnMouseClicked(e -> {
            try {
                List<Utente> list = userService.getFollowing(profileUser.getUsername());
                DialogUtils.showUserListDialog("Following", list, stage, selected -> {
                    stopAllPlayers();
                    UserProfileView upv = new UserProfileView(stage, currentUser, selected);
                    stage.getScene().setRoot(upv.getView());
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Runnable refresh = () -> {
            try {
                lblFoll.setText(String.valueOf(userService.getFollowersCount(profileUser.getUsername())));
                lblFollng.setText(String.valueOf(userService.getFollowingCount(profileUser.getUsername())));

                if (currentUser.getUsername().equals(profileUser.getUsername())) {
                    followBtn.setText("Modifica");
                    followBtn.setOnAction(e -> {
                        stopAllPlayers();
                        ProfileView pv = new ProfileView(stage, currentUser);
                        stage.getScene().setRoot(pv.getView());
                    });
                } else {
                    boolean isFollowing = userService.isFollowing(currentUser.getUsername(), profileUser.getUsername());
                    followBtn.setText(isFollowing ? "Non seguire più" : "Segui");
                    followBtn.getStyleClass().removeAll("button-primary", "button-secondary");
                    followBtn.getStyleClass().add(isFollowing ? "button-secondary" : "button-primary");

                    followBtn.setOnAction(e -> {
                        try {
                            if (isFollowing)
                                userService.unfollow(currentUser.getUsername(), profileUser.getUsername());
                            else
                                userService.follow(currentUser.getUsername(), profileUser.getUsername());

                            // Refresh Vista
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
            List<Post> posts = postService.getPostsByAuthor(profileUser.getUsername());
            if (posts.isEmpty()) {
                Label empty = new Label("Nessun post da mostrare.");
                empty.setStyle("-fx-text-fill: #A1887F; -fx-padding: 20;");
                container.getChildren().add(empty);
                return;
            }
            for (Post p : posts) {
                PostCard card = new PostCard(p, currentUser, () -> loadPosts(container));
                activeCards.add(card);
                container.getChildren().add(card);
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("Errore caricamento post: " + e.getMessage()));
            e.printStackTrace();
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
}
