package it.univaq.brewhub.view.components;

import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Post.TipoPost;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.utility.MediaManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

/**
 * Componente UI per la visualizzazione dei media (Foto o Video) allegati ad un
 * post.
 * Gestisce il caricamento asincrono delle immagini e un player video con
 * controlli custom.
 */
public class PostMediaComponent extends VBox {

    private final Post post;
    private MediaPlayer mediaPlayer;
    private boolean isDisposed = false;

    /**
     * Costruisce il componente media.
     *
     * @param post Il post contenente i dati del media.
     */
    public PostMediaComponent(Post post) {
        this.post = post;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente in base al tipo di post (FOTO o VIDEO).
     */
    private void initUI() {
        if (post.getTipo() == TipoPost.FOTO && post.getMedia() != null) {
            ImageView iv = new ImageView();
            caricaFoto(iv, post.getMedia());
            iv.setFitWidth(600);
            iv.setPreserveRatio(true);
            this.getChildren().add(iv);
        } else if (post.getTipo() == TipoPost.VIDEO && post.getMedia() != null) {
            createVideoPlayer();
        }
    }

    /**
     * Carica un'immagine in modo sicuro, gestendo errori e file mancanti.
     *
     * @param view L'ImageView target.
     * @param path Il percorso relativo del file.
     */
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

    /**
     * Crea un player video con controlli personalizzati (play/pausa, progress bar,
     * volume).
     */
    private void createVideoPlayer() {
        MediaView mv = new MediaView();
        mv.setFitWidth(600);
        mv.setPreserveRatio(true);
        caricaVideo(mv, post.getMedia());
        MediaPlayer mp = mv.getMediaPlayer();
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

            HBox controls = new HBox(10, btnPlay, timeSlider, lblVol, volSlider);
            controls.getStyleClass().add("video-controls-overlay");
            controls.setAlignment(Pos.CENTER);
            controls.setPadding(new Insets(10));
            controls.setMaxHeight(Region.USE_PREF_SIZE);
            StackPane.setAlignment(controls, Pos.BOTTOM_CENTER);

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

    /**
     * Carica e configura il video nel MediaView.
     *
     * @param view MediaView target.
     * @param path Percorso relativo del video.
     */
    private void caricaVideo(MediaView view, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                File file = MediaManager.getMediaFile(path);
                if (file != null && file.exists()) {
                    try {
                        Media media = new Media(file.toURI().toString());
                        media.setOnError(() -> handleMediaError(view, media.getError()));
                        MediaPlayer mp = new MediaPlayer(media);
                        mp.setOnError(() -> handleMediaError(view, mp.getError()));
                        view.setMediaPlayer(mp);
                    } catch (Exception e) {
                        handleMediaError(view, e);
                    }
                } else {
                    Log.warning("File video non trovato: " + path);
                    showErrorPlaceholder(view, "File non trovato");
                }
            }
        } catch (Exception e) {
            Log.error("Errore in caricaVideo: " + path, e);
            handleMediaError(view, e);
        }
    }

    /**
     * Gestisce gli errori di riproduzione media, mostrando un placeholder di
     * errore.
     */
    private void handleMediaError(MediaView view, Throwable t) {
        if (isDisposed)
            return;
        String msg = "Impossibile riprodurre video";
        if (t != null) {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                return;
            }
            Log.error("Errore Media Player", t);
            if (t.getMessage() != null && t.getMessage().contains("ERROR_MEDIA_INVALID")) {
                msg = "Formato video non supportato";
            }
        }
        showErrorPlaceholder(view, msg);
    }

    /**
     * Mostra un messaggio di errore visuale al posto del video.
     */
    private void showErrorPlaceholder(MediaView view, String msg) {
        javafx.application.Platform.runLater(() -> {
            if (view.getParent() instanceof Pane) {
                Pane parent = (Pane) view.getParent();
                parent.getChildren().clear();
                VBox errorBox = new VBox(5);
                errorBox.setAlignment(Pos.CENTER);
                errorBox.setPadding(new Insets(20));
                errorBox.setStyle("-fx-background-color: #FFEBEE; -fx-border-color: #FFCDD2; -fx-border-radius: 5;");
                Label icon = new Label("⚠");
                icon.setStyle("-fx-font-size: 24px;");
                Label lbl = new Label(msg);
                lbl.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                errorBox.getChildren().addAll(icon, lbl);
                parent.getChildren().add(errorBox);
            }
        });
    }

    /**
     * Libera le risorse, fermando e distruggendo il media player.
     * Da chiamare quando il componente non è più necessario.
     */
    public void dispose() {
        this.isDisposed = true;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}
