package it.univaq.brewhub.UI.components;

import it.univaq.brewhub.Commento;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.UI.DialogUtils;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.CommentoDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class PostCard extends VBox {

    private final Post post;
    private final Utente utenteLoggato;
    private final Runnable onRefreshNeeded;
    private final Runnable onSaveAction;

    // DAOs
    private final PostDAOImpl postDAO = new PostDAOImpl();
    private final UtenteDAOImpl utenteDAO = new UtenteDAOImpl();
    private final CommentoDAOImpl commentoDAO = new CommentoDAOImpl();

    private MediaPlayer mediaPlayer;

    public PostCard(Post post, Utente utenteLoggato, Runnable onRefreshNeeded) {
        this(post, utenteLoggato, onRefreshNeeded, null);
    }

    public PostCard(Post post, Utente utenteLoggato, Runnable onRefreshNeeded, Runnable onSaveAction) {
        this.post = post;
        this.utenteLoggato = utenteLoggato;
        this.onRefreshNeeded = onRefreshNeeded;
        this.onSaveAction = onSaveAction;

        initUI();
    }

    private void initUI() {
        this.setSpacing(10);
        this.setMaxWidth(700);
        this.getStyleClass().add("post-card");

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
                    Image img = new Image(f.toURI().toString(), true); // Async loading
                    avatar.setFill(new javafx.scene.paint.ImagePattern(img));
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

        String displayAuthor = post.getAutore().getUsername();
        if (displayAuthor.startsWith("deleted_")) {
            displayAuthor = "Utente eliminato";
        }
        Label authorLbl = new Label(displayAuthor);
        authorLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723;");

        // Verified Badge Logic
        HBox authorBox = new HBox(5, authorLbl);
        authorBox.setAlignment(Pos.CENTER_LEFT);

        if (post.getAutore().getTipo() == Utente.TipoUtente.TORREFATTORE) {
            VerificationBadge badge = new VerificationBadge(16);
            authorBox.getChildren().add(badge);
        }

        // BADGE TIPO UTENTE
        Label userTypeBadge = null;
        if (post.getAutore() != null && post.getAutore().getTipo() != null) {
            userTypeBadge = new Label(post.getAutore().getTipo().toString());
            userTypeBadge.getStyleClass().addAll("badge", "badge-user-type");
        }

        // BADGE CATEGORIA (Opzionale)
        Label categoryBadge = null;
        if (post.getCategoria() != null) {
            categoryBadge = new Label(post.getCategoria().getNome());
            categoryBadge.getStyleClass().addAll("badge", "badge-category");
        }

        Label dateLbl = new Label(post.getDataCreazione().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLbl.setStyle("-fx-font-size: 10px; -fx-opacity: 0.6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = null;
        if (post.getAutore().getUsername().equals(utenteLoggato.getUsername())
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            btnDelete = new Button("\uD83D\uDDD1");
            btnDelete.getStyleClass().addAll("button", "post-delete-btn");
            btnDelete.setOnAction(e -> {
                boolean confirmed = DialogUtils.showConfirmation("Elimina Post", "Eliminare questo post?",
                        this.getScene().getWindow());
                if (confirmed) {
                    try {
                        postDAO.delete(post.getId());
                        if (onRefreshNeeded != null)
                            onRefreshNeeded.run();
                    } catch (SQLException ex) {
                        DialogUtils.showError("Errore", ex.getMessage(), this.getScene().getWindow());
                    }
                }
            });
        }

        header.getChildren().addAll(avatarContainer, authorBox);

        if (userTypeBadge != null) {
            header.getChildren().add(userTypeBadge);
        }

        if (categoryBadge != null) {
            header.getChildren().add(categoryBadge);
        }

        header.getChildren().add(dateLbl);
        header.getChildren().add(spacer);

        if (btnDelete != null) {
            header.getChildren().add(btnDelete);
        }

        Label titleLbl = new Label(post.getTitolo());
        titleLbl.getStyleClass().add("post-title");

        this.getChildren().addAll(header, titleLbl);

        if (post.getTipo() == TipoPost.FOTO && post.getMedia() != null) {
            ImageView iv = new ImageView();
            caricaFoto(iv, post.getMedia());
            iv.setFitWidth(600);
            iv.setPreserveRatio(true);
            this.getChildren().add(new VBox(iv));
        } else if (post.getTipo() == TipoPost.VIDEO && post.getMedia() != null) {
            MediaView mv = new MediaView();
            mv.setFitWidth(600);
            mv.setPreserveRatio(true);
            caricaVideo(mv, post.getMedia());

            MediaPlayer mp = mv.getMediaPlayer();
            // Store locally for dispose
            this.mediaPlayer = mp;

            if (mp != null) {
                Button btnPlay = new Button("\u25B6\uFE0F");
                btnPlay.getStyleClass().add("video-button");
                btnPlay.setStyle("-fx-font-size: 16px;");

                btnPlay.setOnAction(e -> {
                    if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                        mp.pause();
                        btnPlay.setText("\u25B6\uFE0F");
                    } else {
                        mp.play();
                        btnPlay.setText("\u23F8\uFE0F");
                    }
                });

                mp.setOnEndOfMedia(() -> {
                    mp.stop();
                    btnPlay.setText("\u25B6\uFE0F");
                });

                Slider timeSlider = new Slider();
                timeSlider.getStyleClass().add("video-slider");
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

                Label lblVol = new Label("\uD83D\uDD0A");
                lblVol.setStyle("-fx-text-fill: white;");
                Slider volSlider = new Slider(0, 1, 0.5);
                volSlider.getStyleClass().add("video-slider");
                volSlider.setPrefWidth(80);
                mp.volumeProperty().bind(volSlider.valueProperty());

                // Controls (Overlay style matching UserProfileView)
                HBox controls = new HBox(10, btnPlay, timeSlider, lblVol, volSlider);
                controls.getStyleClass().add("video-controls-overlay");
                controls.setAlignment(Pos.CENTER);
                controls.setPadding(new Insets(10));
                controls.setMaxHeight(Region.USE_PREF_SIZE);
                StackPane.setAlignment(controls, Pos.BOTTOM_CENTER);

                // Container video
                StackPane mediaContainer = new StackPane();
                mediaContainer.getStyleClass().add("video-player-container");
                mediaContainer.getChildren().addAll(mv, controls);

                this.getChildren().add(mediaContainer);
            } else {
                StackPane mediaContainer = new StackPane(mv);
                mediaContainer.setStyle("-fx-background-color: black; -fx-min-height: 200px;");
                this.getChildren().add(mediaContainer);
            }
        }

        if (post.getContenuto() != null) {
            Label content = new Label(post.getContenuto());
            content.setWrapText(true);
            content.getStyleClass().add("post-content");
            this.getChildren().add(content);
        }

        createActionsBar();
        createCommentsSection();
    }

    private void createActionsBar() {
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

        Button btnLike = new Button((isLiked ? "\u2764 " : "\u2661 ") + likes);
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
                btnLike.setText((!liked ? "\u2764 " : "\u2661 ") + newCount);
            } catch (SQLException ex) {
                Log.error("Errore gestione like", ex);
            }
        });

        actions.getChildren().add(btnLike);

        // --- SAVE BUTTON ---
        boolean isSaved = false;
        try {
            isSaved = utenteDAO.isArchived(utenteLoggato.getUsername(), post.getId());
        } catch (SQLException e) {
        }

        Button btnSave = new Button(isSaved ? "\u2B50 Salvato" : "\u2606 Salva");
        btnSave.getStyleClass().add("save-button");
        if (isSaved) {
            btnSave.getStyleClass().add("save-button-saved");
        }

        btnSave.setOnAction(e -> {
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE)
                return;
            try {
                boolean saved = utenteDAO.isArchived(utenteLoggato.getUsername(), post.getId());
                if (saved) {
                    utenteDAO.removeFromArchive(utenteLoggato.getUsername(), post.getId());
                    btnSave.setText("\u2606 Salva");
                    btnSave.getStyleClass().remove("save-button-saved");
                } else {
                    utenteDAO.addToArchive(utenteLoggato.getUsername(), post.getId());
                    btnSave.setText("\u2B50 Salvato");
                    if (!btnSave.getStyleClass().contains("save-button-saved")) {
                        btnSave.getStyleClass().add("save-button-saved");
                    }
                }
            } catch (SQLException ex) {
                Log.error("Errore gestione archivio", ex);
            }
            if (onSaveAction != null) {
                onSaveAction.run();
            }
        });

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            actions.getChildren().add(btnSave);
        }
        this.getChildren().add(actions);
    }

    private void createCommentsSection() {
        VBox commentsBox = new VBox(10);
        commentsBox.getStyleClass().add("comments-box");

        // Header Commenti
        Label lblComm = new Label("Commenti");
        lblComm.getStyleClass().add("comments-header");

        Separator sep = new Separator();
        sep.getStyleClass().add("comments-separator");

        commentsBox.getChildren().addAll(lblComm, sep);

        VBox list = new VBox(10);

        if (post.getCommenti().isEmpty()) {
            Label noComm = new Label("Nessun commento.");
            noComm.getStyleClass().add("no-comments-label");
            list.getChildren().add(noComm);
        } else {
            for (Commento c : post.getCommenti()) {
                list.getChildren().add(createCommentRow(c, list));
            }
        }

        HBox inputComm = new HBox(8);
        inputComm.setAlignment(Pos.CENTER_LEFT);

        TextArea tf = new TextArea();
        tf.setPromptText("Scrivi un commento...");
        tf.setWrapText(true);
        tf.setPrefRowCount(1);
        tf.setPrefHeight(36);
        tf.getStyleClass().add("comment-field");
        HBox.setHgrow(tf, Priority.ALWAYS);

        Button btnSend = new Button("Pubblica");
        btnSend.getStyleClass().addAll("button", "comment-send-btn");
        // Remove text if icon is enough, or keep "Invia". Let's use icon for
        // compactness as planned.

        Runnable sendAction = () -> {
            if (!tf.getText().isBlank()) {
                try {
                    Commento c = new Commento(utenteLoggato, post, tf.getText(), LocalDateTime.now());
                    commentoDAO.create(c);

                    if (!list.getChildren().isEmpty() &&
                            list.getChildren().get(0) instanceof Label &&
                            list.getChildren().get(0).getStyleClass().contains("no-comments-label")) {
                        list.getChildren().clear();
                    }

                    list.getChildren().add(createCommentRow(c, list));
                    tf.clear();
                } catch (SQLException ex) {
                    Log.error("Errore inserimento commento", ex);
                }
            }
        };

        btnSend.setOnAction(e -> sendAction.run());

        tf.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    tf.insertText(tf.getCaretPosition(), "\n");
                    event.consume();
                } else {
                    event.consume();
                    sendAction.run();
                }
            }
        });

        commentsBox.getChildren().add(list);
        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            inputComm.getChildren().addAll(tf, btnSend);
            commentsBox.getChildren().add(inputComm);
        }
        this.getChildren().add(commentsBox);
    }

    private HBox createCommentRow(Commento c, VBox parentList) {
        HBox commentRow = new HBox(10);
        commentRow.setAlignment(Pos.CENTER_LEFT);
        commentRow.getStyleClass().add("comment-row");

        String commentAuthor = c.getUtente().getUsername();
        if (commentAuthor.startsWith("deleted_")) {
            commentAuthor = "Utente eliminato";
        }

        // Use TextFlow for rich text (Bold Author + Normal Content)
        javafx.scene.text.Text authorText = new javafx.scene.text.Text(commentAuthor + ": ");
        authorText.getStyleClass().add("comment-author");

        javafx.scene.text.Text contentText = new javafx.scene.text.Text(c.getContenuto());
        contentText.getStyleClass().add("comment-text");

        javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow(authorText, contentText);
        // HBox.setHgrow(flow, Priority.ALWAYS); // TextFlow doesn't grow same as Label?
        // Wrap content

        Region spacerCommenti = new Region();
        HBox.setHgrow(spacerCommenti, Priority.ALWAYS);

        commentRow.getChildren().addAll(flow, spacerCommenti);

        HBox actionButtons = new HBox(5);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setOpacity(0); // Hidden by default

        // EDIT (Solo autori)
        if (c.getUtente().getUsername().equals(utenteLoggato.getUsername())) {
            Button btnEdit = new Button("\u270E");
            btnEdit.getStyleClass().addAll("comment-action-btn", "comment-edit-btn");
            btnEdit.setTooltip(new Tooltip("Modifica"));
            btnEdit.setOnAction(ev -> {
                showEditCommentDialog(c, contentText);
            });
            actionButtons.getChildren().add(btnEdit);
        }

        // DELETE (Autori o Admin)
        if (c.getUtente().getUsername().equals(utenteLoggato.getUsername())
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnDel = new Button("\u2716");
            btnDel.getStyleClass().addAll("comment-action-btn", "comment-delete-btn");
            btnDel.setTooltip(new Tooltip("Elimina"));
            btnDel.setOnAction(ev -> {
                boolean confirmed = DialogUtils.showConfirmation("Elimina Commento", "Eliminare commento?",
                        this.getScene().getWindow());
                if (confirmed) {
                    try {
                        commentoDAO.delete(c.getId());
                        parentList.getChildren().remove(commentRow);
                    } catch (SQLException ex) {
                        DialogUtils.showError("Errore", "Impossibile eliminare: " + ex.getMessage(),
                                this.getScene().getWindow());
                    }
                }
            });
            actionButtons.getChildren().add(btnDel);
        }

        if (!actionButtons.getChildren().isEmpty()) {
            commentRow.getChildren().add(actionButtons);

            // Hover effects
            commentRow.setOnMouseEntered(e -> {
                actionButtons.setOpacity(1);
                commentRow.setStyle("-fx-background-color: rgba(0,0,0,0.02); -fx-background-radius: 5;");
            });
            commentRow.setOnMouseExited(e -> {
                actionButtons.setOpacity(0);
                commentRow.setStyle("-fx-background-color: transparent;");
            });
        }

        return commentRow;
    }

    private void caricaFoto(ImageView view, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                File file = MediaManager.getMediaFile(path);
                if (file != null && file.exists()) {
                    Image img = new Image(file.toURI().toString(), true);
                    img.exceptionProperty().addListener((obs, old, ex) -> {
                        Log.error("Errore caricamento immagine asincrono: " + path, ex);
                        view.setStyle("-fx-opacity: 0.5; -fx-background-color: #eee;");
                    });
                    view.setImage(img);
                } else {
                    Log.warning("File immagine non trovato: " + path);
                }
            }
        } catch (Exception e) {
            Log.error("Errore in caricaFoto: " + path, e);
        }
    }

    private void caricaVideo(MediaView view, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                File file = MediaManager.getMediaFile(path);
                if (file != null && file.exists()) {
                    Media media = new Media(file.toURI().toString());
                    media.setOnError(() -> Log.error("Errore media", media.getError()));
                    MediaPlayer mp = new MediaPlayer(media);
                    mp.setOnError(() -> Log.error("Errore player video", mp.getError()));
                    view.setMediaPlayer(mp);
                } else {
                    Log.warning("File video non trovato: " + path);
                }
            }
        } catch (Exception e) {
            Log.error("Errore in caricaVideo: " + path, e);
        }
    }

    // showAlert method removed

    private void showEditCommentDialog(Commento c, javafx.scene.text.Text contentText) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Modifica Commento");
        dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        // Ensure we have a window owner
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            dialogStage.initOwner(this.getScene().getWindow());
        }

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(400);

        // Header
        Label titleLbl = new Label("Modifica Commento");
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        // Content
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20, 0, 20, 0));

        TextArea area = new TextArea(c.getContenuto());
        area.setWrapText(true);
        area.setPrefRowCount(4);
        area.getStyleClass().add("text-area");

        contentBox.getChildren().add(area);
        root.setCenter(contentBox);

        // Actions
        HBox actions = new HBox(15);
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");

        Button btnSave = new Button("Salva");
        btnSave.getStyleClass().add("button-primary");

        btnCancel.setOnAction(e -> dialogStage.close());

        btnSave.setOnAction(e -> {
            String newText = area.getText().trim();
            if (!newText.isBlank() && !newText.equals(c.getContenuto())) {
                try {
                    c.setContenuto(newText);
                    commentoDAO.update(c);
                    contentText.setText(newText);
                    dialogStage.close();
                } catch (SQLException ex) {
                    DialogUtils.showError("Errore", "Impossibile modificare: " + ex.getMessage(),
                            this.getScene().getWindow());
                }
            } else {
                dialogStage.close();
            }
        });

        actions.getChildren().addAll(btnCancel, btnSave);
        root.setBottom(actions);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
            Log.error("Errore caricamento CSS dialog", ex);
        }

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public void dispose() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
