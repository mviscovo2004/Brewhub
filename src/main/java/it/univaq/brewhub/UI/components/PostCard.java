package it.univaq.brewhub.UI.components;

import it.univaq.brewhub.Commento;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.MediaManager;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.CommentoDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
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

public class PostCard extends VBox {

    private final Post post;
    private final Utente utenteLoggato;
    private final Runnable onRefreshNeeded;

    // DAOs
    private final PostDAOImpl postDAO = new PostDAOImpl();
    private final UtenteDAOImpl utenteDAO = new UtenteDAOImpl();
    private final CommentoDAOImpl commentoDAO = new CommentoDAOImpl();

    private MediaPlayer mediaPlayer;

    public PostCard(Post post, Utente utenteLoggato, Runnable onRefreshNeeded) {
        this.post = post;
        this.utenteLoggato = utenteLoggato;
        this.onRefreshNeeded = onRefreshNeeded;

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

        Label authorLbl = new Label(post.getAutore().getUsername());
        authorLbl.setStyle("-fx-font-weight: bold;");

        Label dateLbl = new Label(post.getDataCreazione().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLbl.setStyle("-fx-font-size: 10px; -fx-opacity: 0.6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = null;
        if (post.getAutore().getUsername().equals(utenteLoggato.getUsername())) {
            btnDelete = new Button("🗑");
            btnDelete.getStyleClass().addAll("button", "post-delete-btn");
            btnDelete.setOnAction(e -> {
                Alert alert = new Alert(AlertType.CONFIRMATION, "Eliminare questo post?", ButtonType.YES,
                        ButtonType.NO);
                alert.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        try {
                            postDAO.delete(post.getId());
                            if (onRefreshNeeded != null)
                                onRefreshNeeded.run();
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
                Button btnPlay = new Button("▶");
                btnPlay.getStyleClass().add("video-button");
                btnPlay.setStyle("-fx-font-size: 16px;");

                btnPlay.setOnAction(e -> {
                    if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                        mp.pause();
                        btnPlay.setText("▶");
                    } else {
                        mp.play();
                        btnPlay.setText("⏸");
                    }
                });

                mp.setOnEndOfMedia(() -> {
                    mp.stop();
                    btnPlay.setText("▶");
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

                Label lblVol = new Label("🔊");
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

        // --- SAVE BUTTON ---
        boolean isSaved = false;
        try {
            isSaved = utenteDAO.isArchived(utenteLoggato.getUsername(), post.getId());
        } catch (SQLException e) {
        }

        Button btnSave = new Button(isSaved ? "⭐ Salvato" : "☆ Salva");
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
                    btnSave.setText("☆ Salva");
                    btnSave.getStyleClass().remove("save-button-saved");
                } else {
                    utenteDAO.addToArchive(utenteLoggato.getUsername(), post.getId());
                    btnSave.setText("⭐ Salvato");
                    if (!btnSave.getStyleClass().contains("save-button-saved")) {
                        btnSave.getStyleClass().add("save-button-saved");
                    }
                }
            } catch (SQLException ex) {
                Log.error("Errore gestione archivio", ex);
            }
        });

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            actions.getChildren().add(btnSave);
            this.getChildren().add(actions);
        } else {
            // Se ospite non mettiamo save, e actions non viene aggiunto se non ha figli
            // ecc?
            // HomeView logic was: add btnLike always. add btnSave if not guest.
            // add actions if not guest.
            // Wait, btnLike IS adding to actions.
            // HomeView:
            // actions.getChildren().add(btnLike);
            // if (not guest) actions.add(btnSave)
            // if (not guest) card.add(actions).
            // So GUEST sees NO likes?
            // Checking HomeView lines 739-741:
            // if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE)
            // card.getChildren().add(actions);
            // This means guests see NO like button. Correct.
            // But they were created.
            // I'll replicate:
            // If not guest, add actions.
            // Guest sees nothing.
            // Actually, guests might want to see like COUNT.
            // But original code hid the whole actions bar. ok.
        }
    }

    private void createCommentsSection() {
        VBox commentsBox = new VBox(5);
        commentsBox.getStyleClass().add("comments-box");
        VBox list = new VBox(5);

        for (Commento c : post.getCommenti()) {
            list.getChildren().add(createCommentRow(c, list));
        }

        HBox inputComm = new HBox(5);
        TextArea tf = new TextArea();
        tf.setPromptText("Commenta...");
        tf.setWrapText(true);
        tf.setPrefRowCount(1);
        tf.setPrefHeight(30);
        HBox.setHgrow(tf, Priority.ALWAYS);

        Button btnSend = new Button("Invia");
        btnSend.setMaxHeight(Double.MAX_VALUE);
        btnSend.getStyleClass().addAll("button", "comment-send-btn");

        Runnable sendAction = () -> {
            if (!tf.getText().isBlank()) {
                try {
                    Commento c = new Commento(utenteLoggato, post, tf.getText(), LocalDateTime.now());
                    commentoDAO.create(c);

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

        Label l = new Label(c.getUtente().getUsername() + ": " + c.getContenuto());
        l.setWrapText(true);

        Region spacerCommenti = new Region();
        HBox.setHgrow(spacerCommenti, Priority.ALWAYS);

        commentRow.getChildren().addAll(l, spacerCommenti);

        if (c.getUtente().getUsername().equals(utenteLoggato.getUsername())) {
            Button btnEdit = new Button("✎");
            btnEdit.getStyleClass().addAll("comment-action-btn", "comment-edit-btn");
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
            btnDel.getStyleClass().addAll("comment-action-btn", "comment-delete-btn");
            btnDel.setOnAction(ev -> {
                Alert alert = new Alert(AlertType.CONFIRMATION, "Eliminare commento?", ButtonType.YES,
                        ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            commentoDAO.delete(c.getId());
                            parentList.getChildren().remove(commentRow);
                        } catch (SQLException ex) {
                            showAlert(AlertType.ERROR, "Errore", "Impossibile eliminare: " + ex.getMessage());
                        }
                    }
                });
            });

            commentRow.getChildren().addAll(btnEdit, btnDel);
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

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
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
