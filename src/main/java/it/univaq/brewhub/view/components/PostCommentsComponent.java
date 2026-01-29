package it.univaq.brewhub.view.components;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.PostService;
import it.univaq.brewhub.model.Commento;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import it.univaq.brewhub.view.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.time.LocalDateTime;

/**
 * Componente UI che gestisce la visualizzazione e l'interazione con i commenti
 * di un post.
 * Permette di leggere, aggiungere, modificare ed eliminare i commenti.
 */
public class PostCommentsComponent extends VBox {

    private final Post post;
    private final Utente utenteLoggato;
    private final PostService postService = PostService.getInstance();
    private VBox list;

    /**
     * Costruisce il componente dei commenti.
     *
     * @param post          Il post a cui sono associati i commenti.
     * @param utenteLoggato L'utente corrente.
     */
    public PostCommentsComponent(Post post, Utente utenteLoggato) {
        this.post = post;
        this.utenteLoggato = utenteLoggato;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente.
     */
    private void initUI() {
        this.setSpacing(10);
        this.getStyleClass().add("comments-box");

        // Header Commenti
        Label lblComm = new Label("Commenti");
        lblComm.getStyleClass().add("comments-header");
        Separator sep = new Separator();
        sep.getStyleClass().add("comments-separator");
        this.getChildren().addAll(lblComm, sep);

        list = new VBox(10);
        loadComments();
        this.getChildren().add(list);

        if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
            createInputArea();
        }
    }

    /**
     * Carica e visualizza la lista dei commenti.
     */
    private void loadComments() {
        list.getChildren().clear();
        if (post.getCommenti().isEmpty()) {
            Label noComm = new Label("Nessun commento.");
            noComm.getStyleClass().add("no-comments-label");
            list.getChildren().add(noComm);
        } else {
            for (Commento c : post.getCommenti()) {
                list.getChildren().add(createCommentRow(c));
            }
        }
    }

    /**
     * Crea l'area di input per aggiungere nuovi commenti.
     */
    private void createInputArea() {
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

        Runnable sendAction = () -> {
            if (!tf.getText().isBlank()) {
                try {
                    Commento c = new Commento(utenteLoggato, post, tf.getText(), LocalDateTime.now());
                    postService.addComment(c);
                    // Aggiornamento ottimistico della UI
                    if (!list.getChildren().isEmpty() &&
                            list.getChildren().get(0) instanceof Label &&
                            list.getChildren().get(0).getStyleClass().contains("no-comments-label")) {
                        list.getChildren().clear();
                    }
                    list.getChildren().add(createCommentRow(c));
                    tf.clear();
                } catch (BusinessException ex) {
                    Log.error("Errore inserimento commento", ex);
                    DialogUtils.showError("Errore", "Impossibile pubblicare il commento: " + ex.getMessage(),
                            this.getScene().getWindow());
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

        inputComm.getChildren().addAll(tf, btnSend);
        this.getChildren().add(inputComm);
    }

    /**
     * Crea una riga visuale per un singolo commento.
     * Include testo, autore e pulsanti di azione (modifica/elimina) se applicabili.
     *
     * @param c Il commento da visualizzare.
     * @return HBox contenente la riga del commento.
     */
    private HBox createCommentRow(Commento c) {
        HBox commentRow = new HBox(10);
        commentRow.setAlignment(Pos.CENTER_LEFT);
        commentRow.getStyleClass().add("comment-row");

        String commentAuthor = c.getUtente().getUsername();
        if (commentAuthor.startsWith("deleted_")) {
            commentAuthor = "Utente eliminato";
        }

        Text authorText = new Text(commentAuthor + ": ");
        authorText.getStyleClass().add("comment-author");
        Text contentText = new Text(c.getContenuto());
        contentText.getStyleClass().add("comment-text");

        TextFlow flow = new TextFlow(authorText, contentText);

        Region spacerCommenti = new Region();
        HBox.setHgrow(spacerCommenti, Priority.ALWAYS);

        commentRow.getChildren().addAll(flow, spacerCommenti);

        HBox actionButtons = new HBox(5);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setOpacity(0);

        // EDIT (Solo autori)
        if (c.getUtente().getUsername().equals(utenteLoggato.getUsername())) {
            Button btnEdit = new Button("✎");
            btnEdit.getStyleClass().addAll("comment-action-btn", "comment-edit-btn");
            btnEdit.setTooltip(new Tooltip("Modifica"));
            btnEdit.setOnAction(ev -> showEditCommentDialog(c, contentText));
            actionButtons.getChildren().add(btnEdit);
        }

        // DELETE (Autori o Admin)
        if (c.getUtente().getUsername().equals(utenteLoggato.getUsername())
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnDel = new Button("✖");
            btnDel.getStyleClass().addAll("comment-action-btn", "comment-delete-btn");
            btnDel.setTooltip(new Tooltip("Elimina"));
            btnDel.setOnAction(ev -> {
                boolean confirmed = DialogUtils.showConfirmation("Elimina Commento", "Eliminare commento?",
                        this.getScene().getWindow());
                if (confirmed) {
                    try {
                        postService.deleteComment(c.getId());
                        list.getChildren().remove(commentRow);
                    } catch (BusinessException ex) {
                        DialogUtils.showError("Errore", "Impossibile eliminare: " + ex.getMessage(),
                                this.getScene().getWindow());
                    }
                }
            });
            actionButtons.getChildren().add(btnDel);
        }

        if (!actionButtons.getChildren().isEmpty()) {
            commentRow.getChildren().add(actionButtons);
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

    /**
     * Mostra un dialogo modale per modificare il testo di un commento.
     *
     * @param c           Il commento da modificare.
     * @param contentText Il nodo Text nella UI da aggiornare in caso di successo.
     */
    private void showEditCommentDialog(Commento c, Text contentText) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Modifica Commento");
        dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            dialogStage.initOwner(this.getScene().getWindow());
        }

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(400);

        Label titleLbl = new Label("Modifica Commento");
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20, 0, 20, 0));
        TextArea area = new TextArea(c.getContenuto());
        area.setWrapText(true);
        area.setPrefRowCount(4);
        area.getStyleClass().add("text-area");
        contentBox.getChildren().add(area);
        root.setCenter(contentBox);

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
                    postService.updateComment(c);
                    contentText.setText(newText);
                    dialogStage.close();
                } catch (BusinessException ex) {
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
        }
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}
