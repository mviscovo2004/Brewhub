package it.univaq.brewhub.view;

import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Classe di utilità per la visualizzazione di finestre di dialogo standard.
 * Supporta:
 * <ul>
 * <li>Messaggi informativi, warning ed errori</li>
 * <li>Richieste di conferma (Sì/No)</li>
 * <li>Richiesta di input testuale</li>
 * <li>Selezione di utenti da lista</li>
 * </ul>
 */
public class DialogUtils {

    /**
     * Mostra una finestra di dialogo informativa.
     *
     * @param title   Titolo della finestra.
     * @param message Messaggio da visualizzare.
     * @param owner   Finestra proprietaria (modale).
     */
    public static void showInfo(String title, String message, Window owner) {
        showDialog(title, message, "INFO", owner);
    }

    /**
     * Mostra una finestra di avviso (Warning).
     *
     * @param title   Titolo della finestra.
     * @param message Messaggio di avviso.
     * @param owner   Finestra proprietaria.
     */
    public static void showWarning(String title, String message, Window owner) {
        showDialog(title, message, "WARNING", owner);
    }

    /**
     * Mostra una finestra di errore.
     *
     * @param title   Titolo della finestra.
     * @param message Messaggio di errore.
     * @param owner   Finestra proprietaria.
     */
    public static void showError(String title, String message, Window owner) {
        showDialog(title, message, "ERROR", owner);
    }

    /**
     * Mostra una finestra di conferma (Sì/No).
     *
     * @param title   Titolo della finestra.
     * @param message Domanda da porre all'utente.
     * @param owner   Finestra proprietaria.
     * @return {@code true} se l'utente ha confermato, {@code false} altrimenti.
     */
    public static boolean showConfirmation(String title, String message, Window owner) {
        return showConfirmDialog(title, message, owner);
    }

    private static void showDialog(String title, String message, String type, Window owner) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null)
            stage.initOwner(owner);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(400);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 20, 30, 20));
        content.setAlignment(Pos.CENTER);

        String icon = "";
        switch (type) {
            case "INFO":
                icon = "ℹ";
                break;
            case "WARNING":
                icon = "⚠";
                break;
            case "ERROR":
                icon = "❌";
                break;
        }

        Label msgLbl = new Label(icon + "  " + message);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 14px; -fx-text-alignment: center; -fx-text-fill: #3E2723;");
        content.getChildren().add(msgLbl);
        root.setCenter(content);

        HBox actions = new HBox(10);
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER);

        Button btnOk = new Button("OK");
        btnOk.getStyleClass().add("button-primary");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(e -> stage.close());

        actions.getChildren().add(btnOk);
        root.setBottom(actions);

        Scene scene = new Scene(root);
        applyStyles(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static boolean showConfirmDialog(String title, String message, Window owner) {
        AtomicBoolean result = new AtomicBoolean(false);
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null)
            stage.initOwner(owner);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(400);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 20, 30, 20));
        content.setAlignment(Pos.CENTER);

        Label msgLbl = new Label("❓  " + message);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 14px; -fx-text-alignment: center; -fx-text-fill: #3E2723;");
        content.getChildren().add(msgLbl);
        root.setCenter(content);

        HBox actions = new HBox(20);
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER);

        Button btnNo = new Button("No");
        btnNo.getStyleClass().add("button-secondary");
        btnNo.setPrefWidth(90);
        btnNo.setOnAction(e -> {
            result.set(false);
            stage.close();
        });

        Button btnYes = new Button("Sì");
        btnYes.getStyleClass().add("button-primary");
        btnYes.setPrefWidth(90);
        btnYes.setOnAction(e -> {
            result.set(true);
            stage.close();
        });

        actions.getChildren().addAll(btnNo, btnYes);
        root.setBottom(actions);

        Scene scene = new Scene(root);
        applyStyles(scene);
        stage.setScene(scene);
        stage.showAndWait();

        return result.get();
    }

    private static void applyStyles(Scene scene) {
        try {
            scene.getStylesheets().add(DialogUtils.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            Log.error("Errore caricamento CSS in DialogUtils", e);
        }
    }

    /**
     * Mostra una finestra di dialogo per richiedere un input testuale semplice.
     *
     * @param title        Titolo della finestra.
     * @param message      Etichetta del campo di input.
     * @param defaultValue Valore iniziale del campo (opzionale).
     * @param owner        Finestra proprietaria.
     * @return La stringa inserita dall'utente o null se annullato.
     */
    public static String showInputDialog(String title, String message, String defaultValue, Window owner) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null)
            stage.initOwner(owner);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(400);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #3E2723;");

        javafx.scene.control.TextField textField = new javafx.scene.control.TextField(
                defaultValue != null ? defaultValue : "");
        textField.getStyleClass().add("text-field");
        textField.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(msgLbl, textField);
        root.setCenter(content);

        HBox actions = new HBox(15);
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER);

        java.util.concurrent.atomic.AtomicReference<String> result = new java.util.concurrent.atomic.AtomicReference<>(
                null);

        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> stage.close());

        Button btnOk = new Button("OK");
        btnOk.getStyleClass().add("button-primary");
        btnOk.setOnAction(e -> {
            result.set(textField.getText());
            stage.close();
        });

        textField.setOnAction(e -> {
            result.set(textField.getText());
            stage.close();
        });

        actions.getChildren().addAll(btnCancel, btnOk);
        root.setBottom(actions);

        Scene scene = new Scene(root);
        applyStyles(scene);
        stage.setScene(scene);
        stage.setOnShown(e -> textField.requestFocus());
        stage.showAndWait();

        return result.get();
    }

    /**
     * Mostra una finestra di selezione utente da una lista.
     *
     * @param title          Titolo della finestra.
     * @param users          Lista di utenti da visualizzare.
     * @param owner          Finestra proprietaria.
     * @param onUserSelected Callback invocata con l'utente selezionato.
     */
    public static void showUserListDialog(String title, List<Utente> users, Window owner,
            Consumer<Utente> onUserSelected) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        if (owner != null)
            dialogStage.initOwner(owner);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("modal-root");
        root.setPrefWidth(350);
        root.setPrefHeight(400);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("modal-title");
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        root.setTop(titleLbl);

        if (users == null || users.isEmpty()) {
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
                        setGraphic(box);
                        box.getChildren().setAll(avatar, name);
                        if (item.getTipo() == Utente.TipoUtente.TORREFATTORE) {
                            Label badge = new Label("✓");
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
                    if (onUserSelected != null) {
                        onUserSelected.accept(selected);
                    }
                }
            });
            root.setCenter(listView);
        }

        Button btnClose = new Button("Chiudi");
        btnClose.getStyleClass().add("button-secondary");
        btnClose.setOnAction(e -> dialogStage.close());

        HBox bottom = new HBox(btnClose);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        root.setBottom(bottom);

        Scene scene = new Scene(root);
        applyStyles(scene);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}
