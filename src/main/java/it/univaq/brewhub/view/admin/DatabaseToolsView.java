package it.univaq.brewhub.view.admin;

import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.view.LoginView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Vista con strumenti di manutenzione del database (Backup e Ripristino).
 * Accessibile solo agli amministratori.
 */
public class DatabaseToolsView extends VBox {

    private final Stage stage;

    /**
     * Costruisce la vista degli strumenti database.
     *
     * @param stage Lo stage dell'applicazione.
     */
    public DatabaseToolsView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente mostrando le opzioni per il backup e il
     * ripristino.
     */
    private void initUI() {
        this.setSpacing(20);
        this.setPadding(new Insets(20));

        Label title = new Label("🗂 Strumenti Database");
        title.getStyleClass().add("section-title");
        this.getChildren().add(title);

        VBox toolsContainer = new VBox(20);
        toolsContainer.setAlignment(Pos.TOP_CENTER);
        toolsContainer.setPadding(new Insets(20));

        // Backup Card
        VBox backupCard = new VBox(15);
        backupCard.setStyle(
                "-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);");
        backupCard.setMaxWidth(600);
        backupCard.setAlignment(Pos.CENTER_LEFT);

        Label lblBackupTitle = new Label("Backup Database");
        lblBackupTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        Label lblBackupDesc = new Label(
                "Esegui una copia completa del database locale (.db) per sicurezza. Il file salvato può essere usato per ripristinare i dati in caso di problemi.");
        lblBackupDesc.setWrapText(true);
        lblBackupDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button btnExecuteBackup = new Button("⬇️ Esegui Backup");
        btnExecuteBackup.getStyleClass().add("button-primary");
        btnExecuteBackup.setStyle("-fx-font-size: 16px; -fx-padding: 10 25;");

        btnExecuteBackup.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Salva Backup Database");
            fc.setInitialFileName("brewhub_backup_" + System.currentTimeMillis() + ".db");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
            File dest = fc.showSaveDialog(stage);
            if (dest != null) {
                try {
                    DatabaseManager.backup(dest);
                    DialogUtils.showInfo("Backup Completato", "Backup salvato in:\n" + dest.getAbsolutePath(), stage);
                } catch (Exception ex) {
                    DialogUtils.showError("Errore Backup", "Impossibile eseguire il backup: " + ex.getMessage(), stage);
                    ex.printStackTrace();
                }
            }
        });
        backupCard.getChildren().addAll(lblBackupTitle, lblBackupDesc, btnExecuteBackup);

        // Restore Card
        VBox restoreCard = new VBox(15);
        restoreCard.setStyle(
                "-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);");
        restoreCard.setMaxWidth(600);
        restoreCard.setAlignment(Pos.CENTER_LEFT);

        Label lblRestoreTitle = new Label("Ripristina Database");
        lblRestoreTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #b71c1c;");
        Label lblRestoreDesc = new Label(
                "Ripristina i dati da un file di backup precedente. \nATTENZIONE: Questa operazione cancellerà tutti i dati attuali e li sostituirà con quelli del backup. L'applicazione verrà riavviata.");
        lblRestoreDesc.setWrapText(true);
        lblRestoreDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button btnExecuteRestore = new Button("⚠ Ripristina da Backup");
        btnExecuteRestore.getStyleClass().add("button-danger");
        btnExecuteRestore.setStyle(
                "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-border-color: #c62828; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 16px; -fx-padding: 10 25;");

        btnExecuteRestore.setOnAction(ev -> {
            boolean confirm = DialogUtils.showConfirmation("Conferma Ripristino",
                    "Sei sicuro di voler ripristinare il database?\nTutti i dati attuali andranno PERSI per sempre.\nL'operazione è irreversibile.",
                    stage);
            if (confirm) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Seleziona File di Backup");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
                File source = fc.showOpenDialog(stage);
                if (source != null) {
                    try {
                        DatabaseManager.restore(source);
                        DialogUtils.showInfo("Ripristino Completato",
                                "Il database è stato ripristinato con successo.\nVerrai reindirizzato alla pagina di login.",
                                stage);
                        LoginView login = new LoginView(stage);
                        stage.getScene().setRoot(login.getView());
                    } catch (Exception ex) {
                        DialogUtils.showError("Errore Ripristino",
                                "Impossibile ripristinare il database (File in uso?):\n" + ex.getMessage(), stage);
                        ex.printStackTrace();
                    }
                }
            }
        });
        restoreCard.getChildren().addAll(lblRestoreTitle, lblRestoreDesc, btnExecuteRestore);

        toolsContainer.getChildren().addAll(backupCard, restoreCard);
        this.getChildren().add(toolsContainer);
    }
}
