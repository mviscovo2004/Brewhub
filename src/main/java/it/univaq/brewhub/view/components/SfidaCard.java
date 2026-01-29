package it.univaq.brewhub.view.components;

import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.DialogUtils;
import it.univaq.brewhub.business.SfidaService;
import it.univaq.brewhub.business.BusinessException;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.HBox;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Componente UI per visualizzare una card relativa a una Sfida (contest).
 * Mostra dettagli come scadenza, premio, creatore e permette la partecipazione.
 */
public class SfidaCard extends BaseCard {
    private final Sfida sfida;
    private final Utente utenteLoggato;
    private final SfidaService sfidaService = SfidaService.getInstance();

    /**
     * Costruisce una nuova card per una sfida.
     *
     * @param sfida         La sfida da visualizzare.
     * @param utenteLoggato L'utente corrente.
     */
    public SfidaCard(Sfida sfida, Utente utenteLoggato) {
        super();
        this.sfida = sfida;
        this.utenteLoggato = utenteLoggato;
        initUI();
    }

    /**
     * Inizializza l'interfaccia utente.
     */
    private void initUI() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label dateLbl = createPrimaryInfoLabel("⏳ Scadenza: " + sfida.getScadenza(), "#D32F2F");
        Label prizeLbl = createSecondaryInfoLabel("🏆 Premio: " + sfida.getPremio(), "#FBC02D");
        Region spacer = createSpacer();

        // Badge Creatore
        Label organizerLbl = createSmallInfoLabel("Sfida di: " + sfida.getCreatore());
        VerificationBadge verifiedBadge = new VerificationBadge(14);

        header.getChildren().addAll(dateLbl, prizeLbl, spacer, organizerLbl, verifiedBadge);

        // Titolo e Descrizione
        Label titleLbl = createTitleLabel(sfida.getTitolo());
        Label descLbl = createContentLabel(sfida.getDescrizione());

        // Footer
        HBox footer = createFooterBox();
        Label participantsLbl = createSecondaryInfoLabel("👥 Partecipanti: " + sfida.getPartecipantiCount(), "#5D4037");
        Region footerSpacer = createSpacer();
        footer.getChildren().addAll(participantsLbl, footerSpacer);

        // Check expired
        boolean isExpired = false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate deadline = LocalDate.parse(sfida.getScadenza(), formatter);
            if (deadline.isBefore(LocalDate.now())) {
                isExpired = true;
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }

        if (isExpired) {
            Button btnEnded = new Button("Terminata");
            btnEnded.setDisable(true);
            btnEnded.setStyle("-fx-opacity: 0.6; -fx-background-color: #9E9E9E; -fx-text-fill: white;");
            footer.getChildren().add(btnEnded);
        } else {
            if (utenteLoggato.getTipo() != Utente.TipoUtente.OSPITE) {
                Button btnParticipate = new Button();
                updateParticipateButton(btnParticipate);
                btnParticipate.setOnAction(e -> {
                    try {
                        if (sfidaService.isParticipating(sfida.getId(), utenteLoggato.getUsername())) {
                            sfidaService.removeParticipant(sfida.getId(), utenteLoggato.getUsername());
                            sfida.setPartecipantiCount(sfida.getPartecipantiCount() - 1);
                        } else {
                            sfidaService.addParticipant(sfida.getId(), utenteLoggato.getUsername());
                            sfida.setPartecipantiCount(sfida.getPartecipantiCount() + 1);
                        }
                        updateParticipateButton(btnParticipate);
                        participantsLbl.setText("👥 Partecipanti: " + sfida.getPartecipantiCount());
                    } catch (BusinessException ex) {
                        Log.error("Errore gestione partecipazione sfida", ex);
                        DialogUtils.showError("Errore", ex.getMessage(), this.getScene().getWindow());
                    }
                });
                footer.getChildren().add(btnParticipate);
            }
        }

        // Admin functionality: Delete button
        if (utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN ||
                (utenteLoggato.getTipo() == Utente.TipoUtente.TORREFATTORE
                        && utenteLoggato.getUsername().equals(sfida.getCreatore()))) {
            Button btnDelete = new Button("🗑 Elimina");
            btnDelete.getStyleClass().add("button-danger");
            btnDelete.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
            btnDelete.setOnAction(e -> {
                try {
                    sfidaService.deleteChallenge(sfida.getId());
                    if (this.getParent() instanceof VBox) {
                        ((VBox) this.getParent()).getChildren().remove(this);
                    }
                } catch (BusinessException ex) {
                    Log.error("Errore eliminazione sfida", ex);
                    DialogUtils.showError("Errore", "Impossibile eliminare la sfida.", this.getScene().getWindow());
                }
            });
            footer.getChildren().add(btnDelete);
        }

        this.getChildren().addAll(header, titleLbl, descLbl, footer);
    }

    /**
     * Aggiorna lo stato del pulsante partecipazione (Iscriviti/Disiscriviti).
     */
    private void updateParticipateButton(Button btn) {
        boolean isParticipating = sfidaService.isParticipating(sfida.getId(), utenteLoggato.getUsername());
        if (isParticipating) {
            btn.setText("✔ Partecipi");
            btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            btn.setText("Partecipa");
            btn.setStyle("-fx-background-color: #5D4037; -fx-text-fill: white;");
        }
    }
}
