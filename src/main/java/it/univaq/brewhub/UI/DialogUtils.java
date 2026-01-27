package it.univaq.brewhub.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.concurrent.atomic.AtomicBoolean;
import it.univaq.brewhub.utility.Log;

/**
 * Classe di utilità per la visualizzazione di finestre di dialogo (Alert,
 * Conferma, Input).
 *
 * Fornisce metodi statici per mostrare messaggi all'utente mantenendo lo stile
 * grafico dell'applicazione.
 *
 */
public class DialogUtils {

    /**
     * Mostra una finestra di dialogo informativa.
     * 
     * @param title   Titolo della finestra.
     * @param message Messaggio da mostrare.
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
     * @return true se l'utente ha cliccato "Sì", false altrimenti.
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
                icon = "\u2139"; // Information source
                break;
            case "WARNING":
                icon = "\u26A0"; // Warning sign
                break;
            case "ERROR":
                icon = "\u274C"; // Cross mark
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
        Label msgLbl = new Label("\u2753  " + message); // Question mark
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
     * Mostra una finestra di dialogo per richiedere un input testuale all'utente.
     * 
     * @param title        Titolo della finestra.
     * @param message      Messaggio di richiesta.
     * @param defaultValue Valore di default nel campo di testo.
     * @param owner        Finestra proprietaria.
     * @return Il testo inserito dall'utente, o null se annullato.
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
        // Riferimento atomico per il risultato
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
}
