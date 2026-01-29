package it.univaq.brewhub.view.components;

import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.business.EventoService;
import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.HBox;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Componente UI che rappresenta una card per un singolo evento.
 * Mostra i dettagli dell'evento e permette agli utenti di partecipare o (se
 * admin) di eliminarlo.
 */
public class EventCard extends BaseCard {
    private final Evento evento;
    private final Utente utenteLoggato;
    private final EventoService eventoService = EventoService.getInstance();

    /**
     * Costruisce una nuova card per un evento.
     *
     * @param evento        L'evento da visualizzare.
     * @param utenteLoggato L'utente attualmente loggato.
     */
    public EventCard(Evento evento, Utente utenteLoggato) {
        super();
        this.evento = evento;
        this.utenteLoggato = utenteLoggato;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente con header, contenuto e footer.
     */
    private void initUI() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label dateLbl = createPrimaryInfoLabel("🗓 " + evento.getData(), "#5D4037");
        Label locationLbl = createSecondaryInfoLabel("📍 " + evento.getLuogo(), "#795548");
        Region spacer = createSpacer();

        // Badge Organizzatore
        Label organizerLbl = createSmallInfoLabel("Organizzato da: " + evento.getOrganizzatore());
        VerificationBadge verifiedBadge = new VerificationBadge(14);

        header.getChildren().addAll(dateLbl, locationLbl, spacer, organizerLbl, verifiedBadge);

        // Titolo e Descrizione
        Label titleLbl = createTitleLabel(evento.getNome());
        Label descLbl = createContentLabel(evento.getDescrizione());

        // Footer
        HBox footer = createFooterBox();
        Label participantsLbl = createSecondaryInfoLabel("👥 Partecipanti: " + evento.getPartecipantiCount(),
                "#5D4037");
        Region footerSpacer = createSpacer();
        footer.getChildren().addAll(participantsLbl, footerSpacer);

        // Controlla se l'evento è passato
        boolean isPast = false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime eventDate = LocalDateTime.parse(evento.getData(), formatter);
            if (eventDate.isBefore(LocalDateTime.now())) {
                isPast = true;
            }
        } catch (Exception e) {
            // Ignora errori di parsing
        }

        if (isPast) {
            Button btnEnded = new Button("Terminato");
            btnEnded.setDisable(true);
            btnEnded.setStyle("-fx-opacity: 0.6; -fx-background-color: #9E9E9E; -fx-text-fill: white;");
            footer.getChildren().add(btnEnded);
        } else {
            if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
                Button btnParticipate = new Button();
                updateParticipateButton(btnParticipate);
                btnParticipate.setOnAction(e -> {
                    try {
                        if (eventoService.isParticipating(evento.getId(), utenteLoggato.getUsername())) {
                            eventoService.removeParticipant(evento.getId(), utenteLoggato.getUsername());
                            evento.setPartecipantiCount(evento.getPartecipantiCount() - 1);
                        } else {
                            eventoService.addParticipant(evento.getId(), utenteLoggato.getUsername());
                            evento.setPartecipantiCount(evento.getPartecipantiCount() + 1);
                        }
                        updateParticipateButton(btnParticipate);
                        participantsLbl.setText("👥 Partecipanti: " + evento.getPartecipantiCount());
                    } catch (BusinessException ex) {
                        Log.error("Errore gestione partecipazione evento", ex);
                        DialogUtils.showError("Errore", ex.getMessage(), this.getScene().getWindow());
                    }
                });
                footer.getChildren().add(btnParticipate);
            }
        }

        // Funzionalità Admin: Bottone Elimina
        if (utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnDelete = new Button("🗑 Elimina");
            btnDelete.getStyleClass().add("button-danger");
            btnDelete.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
            btnDelete.setOnAction(e -> {
                try {
                    eventoService.deleteEvent(evento.getId());
                    // Rimuove questa card dalla UI
                    if (this.getParent() instanceof VBox) {
                        ((VBox) this.getParent()).getChildren().remove(this);
                    }
                } catch (BusinessException ex) {
                    Log.error("Errore eliminazione evento", ex);
                    DialogUtils.showError("Errore", "Impossibile eliminare l'evento.", this.getScene().getWindow());
                }
            });
            footer.getChildren().add(btnDelete);
        }

        this.getChildren().addAll(header, titleLbl, descLbl, footer);
    }

    /**
     * Aggiorna lo stato e il testo del pulsante di partecipazione.
     *
     * @param btn Il pulsante da aggiornare.
     */
    private void updateParticipateButton(Button btn) {
        boolean isParticipating = eventoService.isParticipating(evento.getId(), utenteLoggato.getUsername());
        if (isParticipating) {
            btn.setText("✔ Partecipi");
            btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            btn.setText("Partecipa");
            btn.setStyle("-fx-background-color: #5D4037; -fx-text-fill: white;");
        }
    }
}
