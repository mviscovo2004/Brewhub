package it.univaq.brewhub.UI;

import it.univaq.brewhub.Evento;
import it.univaq.brewhub.UI.components.EventCard;
import it.univaq.brewhub.dao.impl.EventoDAOImpl;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista per la gestione e visualizzazione degli Eventi.
 *
 * Mostra l'elenco degli eventi in programma e passati, e permette
 * ai Torrefattori o Admin di crearne di nuovi.
 *
 */
public class EventsView extends BorderPane {

    private final Utente utenteLoggato;
    private final EventoDAOImpl eventoDAO = new EventoDAOImpl();
    private VBox eventsContainer;

    /**
     * Costruttore.
     * 
     * @param utenteLoggato L'utente che sta visualizzando la pagina.
     */
    public EventsView(Utente utenteLoggato) {
        this.utenteLoggato = utenteLoggato;
        initUI();
        loadEvents();
    }

    /**
     * Inizializza l'interfaccia utente.
     * Configura il layout, l'intestazione e il contenitore scorrevole per gli
     * eventi.
     */
    private void initUI() {
        this.setPadding(new Insets(20));
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("\uD83D\uDCC5 Eventi");
        title.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer);
        if (utenteLoggato.getTipo() == Utente.TipoUtente.TORREFATTORE
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnCreate = new Button("+ Crea Evento");
            btnCreate.getStyleClass().add("button-primary");
            btnCreate.setOnAction(e -> showCreateEventDialog());
            header.getChildren().add(btnCreate);
        }
        this.setTop(header);
        // Container scrollabile
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        eventsContainer = new VBox(15);
        eventsContainer.setAlignment(Pos.TOP_CENTER);
        eventsContainer.setPadding(new Insets(10));
        scrollPane.setContent(eventsContainer);
        this.setCenter(scrollPane);
    }

    /**
     * Carica gli eventi dal database e li visualizza nella vista.
     * Separa gli eventi in "In programma" e "Passati".
     */
    private void loadEvents() {
        eventsContainer.getChildren().clear();
        try {
            List<Evento> allEvents = eventoDAO.findAll();
            List<Evento> upcomingEvents = new ArrayList<>();
            List<Evento> pastEvents = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime now = LocalDateTime.now();
            for (Evento e : allEvents) {
                try {
                    LocalDateTime eventDate = LocalDateTime.parse(e.getData(), formatter);
                    if (eventDate.isBefore(now)) {
                        pastEvents.add(e);
                    } else {
                        upcomingEvents.add(e);
                    }
                } catch (Exception ex) {
                    // Fallback se il parsing fallisce: considera imminente o gestisci
                    upcomingEvents.add(e);
                }
            }
            // Sezione: Eventi Imminenti
            Label upcomingLabel = new Label("\uD83D\uDCC5 Eventi in programma");
            upcomingLabel.getStyleClass().add("section-header");
            upcomingLabel.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-padding: 10 0 5 0;");
            eventsContainer.getChildren().add(upcomingLabel);
            if (upcomingEvents.isEmpty()) {
                eventsContainer.getChildren().add(createEmptyState("\uD83D\uDCC5", "Nessun evento in programma."));
            } else {
                for (Evento e : upcomingEvents) {
                    eventsContainer.getChildren().add(new EventCard(e, utenteLoggato));
                }
            }
            // Separatore
            Region separator = new Region();
            separator.setMinHeight(20);
            eventsContainer.getChildren().add(separator);
            // Sezione: Eventi Passati
            Label pastLabel = new Label("\uD83D\uDD70 Eventi Passati");
            pastLabel.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5D4037; -fx-padding: 10 0 5 0; -fx-opacity: 0.8;");
            eventsContainer.getChildren().add(pastLabel);
            if (pastEvents.isEmpty()) {
                eventsContainer.getChildren().add(createEmptyState("\uD83D\uDD70", "Nessun evento passato recente."));
            } else {
                for (Evento e : pastEvents) {
                    EventCard card = new EventCard(e, utenteLoggato);
                    card.setOpacity(0.7); // Oscura eventi passati
                    eventsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            Log.error("Errore caricamento eventi", e);
            javafx.stage.Window owner = (this.getScene() != null) ? this.getScene().getWindow() : null;
            DialogUtils.showError("Errore", "Impossibile caricare gli eventi.", owner);
        }
    }

    /**
     * Mostra una finestra di dialogo per la creazione di un nuovo evento.
     * Permette di inserire nome, descrizione, data e luogo dell'evento.
     */
    private void showCreateEventDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Nuovo Evento");
        dialog.initOwner(this.getScene().getWindow());
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
                eventoDAO.create(nuovoEvento);
                loadEvents();
                dialog.close();
            } catch (SQLException ex) {
                Log.error("Errore creazione evento", ex);
                DialogUtils.showError("Errore", "Impossibile creare l'evento.", dialog);
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
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
        }
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Crea un componente grafico per lo stato vuoto (nessun evento).
     * 
     * @param emoji   Emoji da visualizzare.
     * @param message Messaggio da visualizzare.
     * @return VBox contenente l'emoji e il messaggio.
     */
    private VBox createEmptyState(String emoji, String message) {

        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.5); -fx-background-radius: 10; -fx-border-color: #D7CCC8; -fx-border-radius: 10; -fx-border-style: dashed;");
        Label iconLbl = new Label(emoji);
        iconLbl.setStyle("-fx-font-size: 40px;");
        Label msgLbl = new Label(message);
        msgLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #5D4037; -fx-font-style: italic;");
        box.getChildren().addAll(iconLbl, msgLbl);
        return box;
    }
}
