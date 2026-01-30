package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.SfidaService;
import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestisce il dialogo per la creazione di nuove sfide.
 */
public class SfidaDialogManager {

    private static final SfidaService sfidaService = SfidaService.getInstance();

    /**
     * Mostra il dialogo di creazione sfida.
     *
     * @param owner          La finestra proprietaria.
     * @param utenteLoggato  L'utente (Torrefattore) che crea la sfida.
     * @param onSfidaCreated Callback eseguita dopo la creazione con successo.
     */
    public static void showCreateSfidaDialog(Window owner, Utente utenteLoggato, Runnable onSfidaCreated) {
        Stage dialog = new Stage();
        dialog.setTitle("Nuova Sfida");
        dialog.initOwner(owner);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #FFF8E1;");

        TextField txtTitolo = new TextField();
        txtTitolo.setPromptText("Titolo Sfida");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descrizione e regole...");
        txtDesc.setPrefRowCount(3);
        TextField txtScadenza = new TextField();
        txtScadenza.setPromptText("Scadenza (dd/MM/yyyy HH:mm)");
        TextField txtPremio = new TextField();
        txtPremio.setPromptText("Premio (opzionale)");

        Button btnConfirm = new Button("Lancia Sfida");
        btnConfirm.getStyleClass().add("button-primary");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);

        btnConfirm.setOnAction(e -> {
            if (txtTitolo.getText().isBlank() || txtScadenza.getText().isBlank()) {
                DialogUtils.showError("Attenzione", "Compila Titolo e Scadenza.", dialog);
                return;
            }
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                // just parse to validate
                LocalDateTime.parse(txtScadenza.getText(), fmt);
            } catch (Exception ex) {
                DialogUtils.showError("Formato Data", "Usa format dd/MM/yyyy HH:mm", dialog);
                return;
            }

            try {
                Sfida nuova = new Sfida(
                        txtTitolo.getText(),
                        txtDesc.getText(),
                        txtScadenza.getText(),
                        txtPremio.getText(),
                        utenteLoggato.getUsername());
                sfidaService.createChallenge(nuova);
                if (onSfidaCreated != null)
                    onSfidaCreated.run();
                dialog.close();
            } catch (BusinessException ex) {
                DialogUtils.showError("Errore Creazione", ex.getMessage(), dialog);
            }
        });

        root.getChildren().addAll(
                new Label("Titolo *"), txtTitolo,
                new Label("Scadenza *"), txtScadenza,
                new Label("Premio"), txtPremio,
                new Label("Descrizione"), txtDesc,
                btnConfirm);

        Scene scene = new Scene(root, 400, 500);
        try {
            scene.getStylesheets().add(SfidaDialogManager.class.getResource("/style.css").toExternalForm());
        } catch (Exception e) {
        }
        dialog.setScene(scene);
        dialog.show();
    }
}
