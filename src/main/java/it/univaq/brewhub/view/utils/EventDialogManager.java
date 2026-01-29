package it.univaq.brewhub.view.utils;

import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.business.EventoService;
import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestisce il dialogo per la creazione di nuovi eventi.
 */
public class EventDialogManager {

    private static final EventoService eventoService = EventoService.getInstance();

    /**
     * Mostra il dialogo di creazione evento.
     *
     * @param owner          La finestra proprietaria.
     * @param utenteLoggato  L'utente che sta creando l'evento.
     * @param onEventCreated Callback eseguita dopo la creazione con successo.
     */
    public static void showCreateEventDialog(Window owner, Utente utenteLoggato, Runnable onEventCreated) {
        Stage dialog = new Stage();
        dialog.setTitle("Nuovo Evento");
        dialog.initOwner(owner);
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #FFF8E1;");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome Evento");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descrizione");
        txtDesc.setPrefRowCount(3);
        TextField txtData = new TextField(); // Idealmente DatePicker, ma stringa per semplicità
        txtData.setPromptText("Data (es. 25/12/2026 18:00)");
        TextField txtLuogo = new TextField();
        txtLuogo.setPromptText("Luogo");

        Button btnConfirm = new Button("Crea");
        btnConfirm.getStyleClass().add("button-primary");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);

        btnConfirm.setOnAction(e -> {
            if (txtNome.getText().isBlank() || txtData.getText().isBlank() || txtLuogo.getText().isBlank()) {
                DialogUtils.showError("Attenzione", "Compila tutti i campi obbligatori.", dialog);
                return;
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                LocalDateTime eventDate = LocalDateTime.parse(txtData.getText(), formatter);
                if (eventDate.isBefore(LocalDateTime.now())) {
                    DialogUtils.showError("Errore Data", "Non puoi creare eventi nel passato!", dialog);
                    return;
                }
            } catch (Exception ex) {
                DialogUtils.showError("Formato Data Errato", "Usa il formato: dd/MM/yyyy HH:mm", dialog);
                return;
            }
            try {
                Evento nuovoEvento = new Evento(
                        txtNome.getText(),
                        txtDesc.getText(),
                        txtData.getText(),
                        txtLuogo.getText(),
                        utenteLoggato.getUsername());
                eventoService.createEvent(nuovoEvento);
                if (onEventCreated != null) {
                    onEventCreated.run();
                }
                dialog.close();
            } catch (BusinessException ex) {
                DialogUtils.showError("Errore", ex.getMessage(), dialog);
            }
        });

        root.getChildren().addAll(
                new Label("Nome *"), txtNome,
                new Label("Data *"), txtData,
                new Label("Luogo *"), txtLuogo,
                new Label("Descrizione"), txtDesc,
                btnConfirm);

        Scene scene = new Scene(root, 400, 450);
        try {
            scene.getStylesheets().add(EventDialogManager.class.getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
        }
        dialog.setScene(scene);
        dialog.show();
    }
}
