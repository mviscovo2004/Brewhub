package it.univaq.brewhub.UI.components;
import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.UI.DialogUtils;
import it.univaq.brewhub.dao.impl.SfidaDAOImpl;
import it.univaq.brewhub.utility.Log;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class SfidaCard extends VBox {
    private final Sfida sfida;
    private final Utente utenteLoggato;
    private final SfidaDAOImpl sfidaDAO = new SfidaDAOImpl();
    public SfidaCard(Sfida sfida, Utente utenteLoggato) {
        this.sfida = sfida;
        this.utenteLoggato = utenteLoggato;
        initUI();
    }
    private void initUI() {
        this.setSpacing(10);
        this.setMaxWidth(700);
        this.getStyleClass().add("post-card"); 
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label dateLbl = new Label("\u23F3 Scadenza: " + sfida.getScadenza());
        dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #D32F2F;");
        Label prizeLbl = new Label("\uD83C\uDFC6 Premio: " + sfida.getPremio());
        prizeLbl.setStyle("-fx-text-fill: #FBC02D; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // Badge Creatore (Torrefattore)
        Label organizerLbl = new Label("Sfida di: " + sfida.getCreatore());
        organizerLbl.setStyle("-fx-font-size: 10px; -fx-opacity: 0.7;");
        it.univaq.brewhub.UI.components.VerificationBadge verifiedBadge = new it.univaq.brewhub.UI.components.VerificationBadge(
                14);
        header.getChildren().addAll(dateLbl, prizeLbl, spacer, organizerLbl, verifiedBadge);
        // Titolo
        Label titleLbl = new Label(sfida.getTitolo());
        titleLbl.getStyleClass().add("post-title");
        // Descrizione
        Label descLbl = new Label(sfida.getDescrizione());
        descLbl.setWrapText(true);
        descLbl.getStyleClass().add("post-content");
        // Footer: Partecipanti e Bottone
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label participantsLbl = new Label("\uD83D\uDC65 Partecipanti: " + sfida.getPartecipantiCount());
        participantsLbl.setStyle("-fx-text-fill: #5D4037;");
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
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
            Button btnParticipate = new Button();
            if (utenteLoggato.getTipo() == Utente.TipoUtente.OSPITE) {
                btnParticipate.setText("Partecipa");
                btnParticipate.setDisable(true);
                btnParticipate.setTooltip(new Tooltip("Accedi per partecipare"));
            } else {
                updateParticipateButton(btnParticipate);
                btnParticipate.setOnAction(e -> {
                    try {
                        if (sfidaDAO.isPartecipante(sfida.getId(), utenteLoggato.getUsername())) {
                            sfidaDAO.removePartecipante(sfida.getId(), utenteLoggato.getUsername());
                            sfida.setPartecipantiCount(sfida.getPartecipantiCount() - 1);
                        } else {
                            sfidaDAO.addPartecipante(sfida.getId(), utenteLoggato.getUsername());
                            sfida.setPartecipantiCount(sfida.getPartecipantiCount() + 1);
                        }
                        updateParticipateButton(btnParticipate);
                        participantsLbl.setText("\uD83D\uDC65 Partecipanti: " + sfida.getPartecipantiCount());
                    } catch (SQLException ex) {
                        Log.error("Errore gestione partecipazione sfida", ex);
                    }
                });
            }
            footer.getChildren().add(btnParticipate);
        }
        // Admin functionality: Delete button
        if (utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN ||
                (utenteLoggato.getTipo() == Utente.TipoUtente.TORREFATTORE
                        && utenteLoggato.getUsername().equals(sfida.getCreatore()))) {
            Button btnDelete = new Button("\uD83D\uDDD1 Elimina");
            btnDelete.getStyleClass().add("button-danger");
            btnDelete.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
            btnDelete.setOnAction(e -> {
                try {
                    sfidaDAO.delete(sfida.getId());
                    if (this.getParent() instanceof VBox) {
                        ((VBox) this.getParent()).getChildren().remove(this);
                    }
                } catch (SQLException ex) {
                    Log.error("Errore eliminazione sfida", ex);
                    DialogUtils.showError("Errore", "Impossibile eliminare la sfida.", this.getScene().getWindow());
                }
            });
            footer.getChildren().add(btnDelete);
        }
        this.getChildren().addAll(header, titleLbl, descLbl, footer);
    }
    private void updateParticipateButton(Button btn) {
        try {
            boolean isParticipating = sfidaDAO.isPartecipante(sfida.getId(), utenteLoggato.getUsername());
            if (isParticipating) {
                btn.setText("\u2714 Partecipi");
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
