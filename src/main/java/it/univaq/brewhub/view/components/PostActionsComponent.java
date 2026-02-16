package it.univaq.brewhub.view.components;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.ChatService;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.business.UserService;
import it.univaq.brewhub.model.Gruppo;
import it.univaq.brewhub.model.Messaggio;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.view.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Componente UI che raggruppa le azioni disponibili su un post.
 * Include i pulsanti per mettere "Mi piace", salvare nei preferiti e
 * condividere.
 */
public class PostActionsComponent extends HBox {

    private final Post post;
    private final Utente utenteLoggato;
    private final Runnable onSaveAction;

    private final PostService postService = PostService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final ChatService chatService = ChatService.getInstance();

    /**
     * Crea un nuovo componente per le azioni del post.
     *
     * @param post          Il post a cui si riferiscono le azioni.
     * @param utenteLoggato L'utente corrente.
     * @param onSaveAction  Callback opzionale da eseguire dopo il salvataggio del
     *                      post.
     */
    public PostActionsComponent(Post post, Utente utenteLoggato, Runnable onSaveAction) {
        this.post = post;
        this.utenteLoggato = utenteLoggato;
        this.onSaveAction = onSaveAction;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente con i pulsanti appropriati.
     */
    private void initUI() {
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER_LEFT);

        createLikeButton();

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            createSaveButton();
            createShareButton();
        }
    }

    /**
     * Crea il pulsante "Mi piace" e ne gestisce la logica.
     */
    private void createLikeButton() {
        boolean isLiked = false;
        int likes = 0;
        try {
            isLiked = postService.isLiked(post.getId(), utenteLoggato.getUsername());
            likes = postService.getLikesCount(post.getId());
        } catch (Exception e) {
            Log.error("Errore refresh like", e);
        }

        Button btnLike = new Button((isLiked ? "❤ " : "♡ ") + likes);
        btnLike.getStyleClass().add("like-button");
        if (isLiked)
            btnLike.getStyleClass().add("like-button-active");

        btnLike.setOnAction(e -> {
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE)
                return;
            try {
                boolean liked = postService.isLiked(post.getId(), utenteLoggato.getUsername());
                if (liked) {
                    postService.removeLike(post.getId(), utenteLoggato.getUsername());
                    btnLike.getStyleClass().remove("like-button-active");
                } else {
                    postService.addLike(post.getId(), utenteLoggato.getUsername());
                    if (!btnLike.getStyleClass().contains("like-button-active"))
                        btnLike.getStyleClass().add("like-button-active");
                }
                int newCount = postService.getLikesCount(post.getId());
                btnLike.setText((!liked ? "❤ " : "♡ ") + newCount);
            } catch (BusinessException ex) {
                Log.error("Errore gestione like", ex);
            }
        });

        this.getChildren().add(btnLike);
    }

    /**
     * Crea il pulsante "Salva" e ne gestisce la logica.
     */
    private void createSaveButton() {
        boolean isSaved = false;
        try {
            isSaved = userService.isArchived(utenteLoggato.getUsername(), post.getId());
        } catch (Exception e) {
        }

        Button btnSave = new Button(isSaved ? "⭐ Salvato" : "☆ Salva");
        btnSave.getStyleClass().add("save-button");
        if (isSaved) {
            btnSave.getStyleClass().add("save-button-saved");
        }

        btnSave.setOnAction(e -> {
            try {
                boolean saved = userService.isArchived(utenteLoggato.getUsername(), post.getId());
                if (saved) {
                    userService.removeFromArchive(utenteLoggato.getUsername(), post.getId());
                    btnSave.setText("☆ Salva");
                    btnSave.getStyleClass().remove("save-button-saved");
                } else {
                    userService.addToArchive(utenteLoggato.getUsername(), post.getId());
                    btnSave.setText("⭐ Salvato");
                    if (!btnSave.getStyleClass().contains("save-button-saved")) {
                        btnSave.getStyleClass().add("save-button-saved");
                    }
                }
            } catch (BusinessException ex) {
                Log.error("Errore gestione archivio", ex);
            }
            if (onSaveAction != null) {
                onSaveAction.run();
            }
        });

        this.getChildren().add(btnSave);
    }

    /**
     * Crea il pulsante "Condividi" che apre il dialogo di condivisione.
     */
    private void createShareButton() {
        Button btnShare = new Button("📤 Condividi");
        btnShare.getStyleClass().add("share-button");
        btnShare.setOnAction(e -> showShareDialog());
        this.getChildren().add(btnShare);
    }

    /**
     * Mostra una finestra di dialogo modale per condividere il post con un altro
     * utente o un gruppo tramite chat.
     */
    private void showShareDialog() {
        Stage stage = new Stage();
        stage.setTitle("Condividi Post");
        stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            stage.initOwner(this.getScene().getWindow());
        }

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(400);

        // Header
        Label titleLbl = new Label("Condividi Post");
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 0, 20, 0));
        content.setAlignment(Pos.CENTER_LEFT);

        Label lblDest = new Label("Invia questo post a:");
        lblDest.setStyle("-fx-font-weight: bold; -fx-text-fill: #5D4037;");

        ComboBox<ShareTarget> targetCombo = new ComboBox<>();
        targetCombo.setEditable(false);
        targetCombo.setPromptText("Seleziona utente o gruppo...");
        targetCombo.setMaxWidth(Double.MAX_VALUE);
        targetCombo.getStyleClass().add("choice-box");

        try {
            // Add active private conversations
            List<String> recentUsers = chatService.getActiveConversations(utenteLoggato.getUsername());
            for (String user : recentUsers) {
                targetCombo.getItems().add(new ShareTarget(user));
            }

            // Add user groups
            List<Gruppo> groups = chatService.getUserGroups(utenteLoggato.getUsername());
            for (Gruppo g : groups) {
                targetCombo.getItems().add(new ShareTarget(g));
            }
        } catch (Exception e) {
            Log.error("Errore caricamento destinatari condivisione", e);
        }

        content.getChildren().addAll(lblDest, targetCombo);
        root.setCenter(content);

        // Actions
        HBox actions = new HBox(15);
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSend = new Button("Invia");
        btnSend.getStyleClass().add("button-primary");
        btnSend.setOnAction(e -> {
            ShareTarget selected = targetCombo.getValue();
            if (selected != null) {
                try {
                    Messaggio m = new Messaggio();
                    m.setSender(utenteLoggato.getUsername());
                    m.setContenuto("[POST:::" + post.getId() + "]");
                    m.setTimestamp(LocalDateTime.now().toString());
                    m.setLetto(false);

                    if (selected.isGroup) {
                        m.setIdGruppo(selected.groupId);
                    } else {
                        m.setReceiver(selected.username);
                    }

                    chatService.sendMessage(m);
                    stage.close();
                    DialogUtils.showInfo("Condiviso", "Post inviato a " + selected.label, this.getScene().getWindow());
                } catch (Exception ex) {
                    DialogUtils.showError("Errore", "Impossibile inviare: " + ex.getMessage(),
                            this.getScene().getWindow());
                }
            } else {
                DialogUtils.showWarning("Attenzione", "Seleziona un destinatario.", stage);
            }
        });

        actions.getChildren().addAll(btnCancel, btnSend);
        root.setBottom(actions);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
        }
        stage.setScene(scene);
        stage.showAndWait();

    }

    /**
     * Classe helper per gestire la selezione mista (Utente/Gruppo) nel ComboBox.
     */
    private static class ShareTarget {
        final String label;
        final String username;
        final Integer groupId;
        final boolean isGroup;

        // Costruttore per utente (chat privata)
        public ShareTarget(String username) {
            this.label = "👤 " + username;
            this.username = username;
            this.groupId = null;
            this.isGroup = false;
        }

        // Costruttore per gruppo
        public ShareTarget(Gruppo gruppo) {
            this.label = "👥 " + gruppo.getNome();
            this.username = null;
            this.groupId = gruppo.getId();
            this.isGroup = true;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
