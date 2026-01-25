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
    private final it.univaq.brewhub.dao.TorrefattoreDAO torrefattoreDAO = new it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl();
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

        HBox usernameBox = new HBox(8, lblUsername);
        usernameBox.setAlignment(Pos.CENTER);
        if (profileUser.getTipo() == Utente.TipoUtente.TORREFATTORE) {
            it.univaq.brewhub.UI.components.VerificationBadge badge = new it.univaq.brewhub.UI.components.VerificationBadge(
                    20);
            usernameBox.getChildren().add(badge);
        }

        Label lblName = new Label();
        lblName.getStyleClass().add("profile-bio");
        Label lblDesc = null;

        if (profileUser.getTipo() == Utente.TipoUtente.TORREFATTORE) {
            try {
                it.univaq.brewhub.Torrefattore tDetails = torrefattoreDAO.findByUsername(profileUser.getUsername());
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
        // Assemblaggio info
        infoContainer.getChildren().addAll(usernameBox, lblName);
        if (lblDesc != null) {
            infoContainer.getChildren().add(lblDesc);
        }
        infoContainer.getChildren().add(statsBox);
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

            // ADMIN logic: Delete user button
            if (currentUser.getTipo() == Utente.TipoUtente.ADMIN
                    && !currentUser.getUsername().equals(profileUser.getUsername())) {
                Button btnDeleteUser = new Button("\u26A0 Elimina Utente");
                btnDeleteUser.getStyleClass().add("button-danger");
                btnDeleteUser.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; -fx-margin-top: 10px;");

                btnDeleteUser.setOnAction(e -> {
                    boolean confirmed = DialogUtils.showConfirmation("SEI SICURO?",
                            "Eliminare @" + profileUser.getUsername() + "?", stage);
                    if (confirmed) {
                        try {
                            utenteDAO.delete(profileUser.getUsername());
                            // Return to home
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
        Button btnHome = new Button("\u2190 Torna alla Home");
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
        // --- Click Handlers for Count ---
        VBox boxFoll = (VBox) lblFoll.getParent();
        boxFoll.setCursor(javafx.scene.Cursor.HAND);
        boxFoll.setOnMouseClicked(e -> {
            try {
                java.util.List<Utente> list = utenteDAO.getFollowers(profileUser.getUsername());
                showUserListDialog("Followers", list);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox boxFollng = (VBox) lblFollng.getParent();
        boxFollng.setCursor(javafx.scene.Cursor.HAND);
        boxFollng.setOnMouseClicked(e -> {
            try {
                java.util.List<Utente> list = utenteDAO.getFollowing(profileUser.getUsername());
                showUserListDialog("Following", list);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

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

    private void showUserListDialog(String title, java.util.List<Utente> users) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(350);
        root.setPrefHeight(400);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        if (users.isEmpty()) {
            Label empty = new Label("Nessun utente.");
            empty.setStyle("-fx-text-fill: #8D6E63; -fx-padding: 20; -fx-alignment: center;");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            root.setCenter(empty);
        } else {
            ListView<Utente> listView = new ListView<>();
            listView.getItems().setAll(users);
            listView.getStyleClass().add("list-view");
            listView.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

            listView.setCellFactory(param -> new ListCell<>() {
                private final HBox box = new HBox(10);
                private final Circle avatar = new Circle(16);
                private final Label name = new Label();

                {
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setPadding(new Insets(5));
                    name.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-font-size: 13px;");
                    avatar.setFill(javafx.scene.paint.Color.web("#8D6E63"));
                }

                @Override
                protected void updateItem(Utente item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        name.setText("@" + item.getUsername());
                        // Try loading avatar logic if needed or just color
                        // Simple placeholder for efficiency in dialog
                        setGraphic(box);
                        box.getChildren().setAll(avatar, name);

                        // Torrefattore badge check?
                        if (item.getTipo() == Utente.TipoUtente.TORREFATTORE) {
                            // Simple text marker for now as we don't have easy access to the Badge class
                            // instance without duplication
                            Label badge = new Label("\u2713");
                            badge.setStyle("-fx-text-fill: #D4A574; -fx-font-weight: bold;");
                            box.getChildren().add(badge);
                        }

                        setOnMouseEntered(e -> setStyle("-fx-background-color: #FFF8E1; -fx-cursor: hand;"));
                        setOnMouseExited(e -> setStyle("-fx-background-color: transparent;"));
                    }
                }
            });

            listView.setOnMouseClicked(e -> {
                Utente selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    dialogStage.close();
                    stopAllPlayers(); // Stop current view resources
                    // Navigate to clicked profile
                    UserProfileView upv = new UserProfileView(stage, currentUser, selected);
                    stage.getScene().setRoot(upv.getView());
                }
            });

            root.setCenter(listView);
        }

        // Close Button
        Button btnClose = new Button("Chiudi");
        btnClose.getStyleClass().add("button-secondary");
        btnClose.setOnAction(e -> dialogStage.close());
        HBox bottom = new HBox(btnClose);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        root.setBottom(bottom);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
        }

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
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
