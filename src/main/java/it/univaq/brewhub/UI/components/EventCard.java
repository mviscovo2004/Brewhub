package it.univaq.brewhub.UI.components;

import it.univaq.brewhub.Evento;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.UI.DialogUtils;
import it.univaq.brewhub.dao.impl.EventoDAOImpl;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventCard extends VBox {

    private final Evento evento;
    private final Utente utenteLoggato;
    private final EventoDAOImpl eventoDAO = new EventoDAOImpl();

    public EventCard(Evento evento, Utente utenteLoggato) {
        this.evento = evento;
        this.utenteLoggato = utenteLoggato;
        initUI();
    }

    private void initUI() {
        this.setSpacing(10);
        this.setMaxWidth(700);
        this.getStyleClass().add("post-card"); // Reuse post-card style for consistency

        // Header: Data e Luogo
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label dateLbl = new Label("📅 " + evento.getData());
        dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #5D4037;");

        Label locationLbl = new Label("📍 " + evento.getLuogo());
        locationLbl.setStyle("-fx-text-fill: #795548;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge Organizzatore (Torrefattore)
        Label organizerLbl = new Label("Organizzato da: " + evento.getOrganizzatore());
        organizerLbl.setStyle("-fx-font-size: 10px; -fx-opacity: 0.7;");

        Label verifiedBadge = new Label("\u2714");
        verifiedBadge.getStyleClass().add("verified-badge");
        verifiedBadge.setTooltip(new Tooltip("Torrefattore Verificato"));

        header.getChildren().addAll(dateLbl, locationLbl, spacer, organizerLbl, verifiedBadge);

        // Titolo
        Label titleLbl = new Label(evento.getNome());
        titleLbl.getStyleClass().add("post-title"); // Reuse title style

        // Descrizione
        Label descLbl = new Label(evento.getDescrizione());
        descLbl.setWrapText(true);
        descLbl.getStyleClass().add("post-content");

        // Footer: Partecipanti e Bottone
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label participantsLbl = new Label("👥 Partecipanti: " + evento.getPartecipantiCount());
        participantsLbl.setStyle("-fx-text-fill: #5D4037;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        footer.getChildren().addAll(participantsLbl, footerSpacer);

        // Check if event is past
        boolean isPast = false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime eventDate = LocalDateTime.parse(evento.getData(), formatter);
            if (eventDate.isBefore(LocalDateTime.now())) {
                isPast = true;
            }
        } catch (Exception e) {
            // Ignore parsing errors, assume not past or handle appropriately
        }

        if (isPast) {
            Button btnEnded = new Button("Terminato");
            btnEnded.setDisable(true);
            btnEnded.setStyle("-fx-opacity: 0.6; -fx-background-color: #9E9E9E; -fx-text-fill: white;");
            footer.getChildren().add(btnEnded);
        } else {
            Button btnParticipate = new Button();
            updateParticipateButton(btnParticipate);
            btnParticipate.setOnAction(e -> {
                try {
                    if (eventoDAO.isPartecipante(evento.getId(), utenteLoggato.getUsername())) {
                        eventoDAO.removePartecipante(evento.getId(), utenteLoggato.getUsername());
                        evento.setPartecipantiCount(evento.getPartecipantiCount() - 1);
                    } else {
                        eventoDAO.addPartecipante(evento.getId(), utenteLoggato.getUsername());
                        evento.setPartecipantiCount(evento.getPartecipantiCount() + 1);
                    }
                    updateParticipateButton(btnParticipate);
                    participantsLbl.setText("👥 Partecipanti: " + evento.getPartecipantiCount());
                } catch (SQLException ex) {
                    Log.error("Errore gestione partecipazione evento", ex);
                }
            });
            footer.getChildren().add(btnParticipate);
        }

        // Admin functionality: Delete button
        if (utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnDelete = new Button("🗑 Elimina");
            btnDelete.getStyleClass().add("button-danger"); // Assuming this class exists or generic style
            btnDelete.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");

            btnDelete.setOnAction(e -> {
                try {
                    eventoDAO.delete(evento.getId());
                    // Remove this card from the UI
                    if (this.getParent() instanceof VBox) {
                        ((VBox) this.getParent()).getChildren().remove(this);
                    }
                } catch (SQLException ex) {
                    Log.error("Errore eliminazione evento", ex);
                    DialogUtils.showError("Errore", "Impossibile eliminare l'evento.", this.getScene().getWindow());
                }
            });
            footer.getChildren().add(btnDelete);
        }

        this.getChildren().addAll(header, titleLbl, descLbl, footer);
    }

    private void updateParticipateButton(Button btn) {
        try {
            boolean isParticipating = eventoDAO.isPartecipante(evento.getId(), utenteLoggato.getUsername());
            if (isParticipating) {
                btn.setText("✔ Partecipi");
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            } else {
                btn.setText("Partecipa");
                btn.setStyle("-fx-background-color: #5D4037; -fx-text-fill: white;");
            }
        } catch (SQLException e) {
            Log.error("Errore check partecipazione", e);
            btn.setText("Partecipa");
        }
    }
}
