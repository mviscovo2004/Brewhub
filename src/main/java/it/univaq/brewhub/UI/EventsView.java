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

public class EventsView extends BorderPane {

    private final Utente utenteLoggato;
    private final EventoDAOImpl eventoDAO = new EventoDAOImpl();
    private VBox eventsContainer;

    public EventsView(Utente utenteLoggato) {
        this.utenteLoggato = utenteLoggato;
        initUI();
        loadEvents();
    }

    private void initUI() {
        this.setPadding(new Insets(20));

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📅 Eventi");
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
                    // Fallback if parsing fails: consider it upcoming or handle gracefully
                    // For now, let's treat it as upcoming so it's visible
                    upcomingEvents.add(e);
                }
            }

            // Section: Upcoming Events
            Label upcomingLabel = new Label("📅 Eventi in programma");
            upcomingLabel.getStyleClass().add("section-header"); // You might want to define this in CSS or reuse
                                                                 // section-title with smaller font
            upcomingLabel.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-padding: 10 0 5 0;");
            eventsContainer.getChildren().add(upcomingLabel);

            if (upcomingEvents.isEmpty()) {
                eventsContainer.getChildren().add(createEmptyState("📅", "Nessun evento in programma."));
            } else {
                for (Evento e : upcomingEvents) {
                    eventsContainer.getChildren().add(new EventCard(e, utenteLoggato));
                }
            }

            // Separator
            Region separator = new Region();
            separator.setMinHeight(20);
            eventsContainer.getChildren().add(separator);

            // Section: Past Events
            Label pastLabel = new Label("🕰️ Eventi Passati");
            // pastLabel.getStyleClass().add("section-header");
            pastLabel.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5D4037; -fx-padding: 10 0 5 0; -fx-opacity: 0.8;");
            eventsContainer.getChildren().add(pastLabel);

            if (pastEvents.isEmpty()) {
                eventsContainer.getChildren().add(createEmptyState("🕰️", "Nessun evento passato recente."));
            } else {
                for (Evento e : pastEvents) {
                    EventCard card = new EventCard(e, utenteLoggato);
                    card.setOpacity(0.7); // Dim past events content
                    eventsContainer.getChildren().add(card);
                }
            }

        } catch (SQLException e) {
            Log.error("Errore caricamento eventi", e);
            DialogUtils.showError("Errore", "Impossibile caricare gli eventi.", this.getScene().getWindow());
        }
    }

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

        TextField txtData = new TextField(); // Idealmente DatePicker, ma stringa per semplicità come da modello
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
