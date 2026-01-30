package it.univaq.brewhub.view;

import it.univaq.brewhub.business.SfidaService;
import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.components.SfidaCard;
import it.univaq.brewhub.view.utils.SfidaDialogManager;
import javafx.scene.control.Button;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista per visualizzare e gestire le Sfide (Contest).
 * Estende {@link BaseSectionView} per il layout comune.
 *
 * <p>
 * Funzionalità:
 * <ul>
 * <li>Visualizzazione sfide attive e concluse separatamente.</li>
 * <li>Creazione di nuove sfide (solo Torrefattori/Admin).</li>
 * </ul>
 */
public class SfideView extends BaseSectionView {

    private final SfidaService sfidaService = SfidaService.getInstance();

    /**
     * Costruisce la vista Sfide.
     *
     * @param utenteLoggato L'utente corrente.
     */
    public SfideView(Utente utenteLoggato) {
        super(utenteLoggato);
        refreshData();
    }

    @Override
    protected String getSectionTitle() {
        return "🏆 Sfide";
    }

    @Override
    protected Button createActionButton() {
        if (utenteLoggato.getTipo() == Utente.TipoUtente.TORREFATTORE
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnCreate = new Button("+ Crea Sfida");
            btnCreate.getStyleClass().add("button-primary");
            btnCreate.setOnAction(e -> SfidaDialogManager.showCreateSfidaDialog(
                    this.getScene().getWindow(),
                    utenteLoggato,
                    this::refreshData));
            return btnCreate;
        }
        return null;
    }

    @Override
    protected void refreshData() {
        contentContainer.getChildren().clear();
        List<Sfida> allSfide = sfidaService.getAllChallenges();
        List<Sfida> activeChallenges = new ArrayList<>();
        List<Sfida> pastChallenges = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.time.LocalDate now = java.time.LocalDate.now();

        for (Sfida s : allSfide) {
            try {
                // Parsing data scadenza (formato dd/MM/yyyy)
                java.time.LocalDate deadline = java.time.LocalDate.parse(s.getScadenza(), formatter);
                if (deadline.isBefore(now)) {
                    pastChallenges.add(s);
                } else {
                    activeChallenges.add(s);
                }
            } catch (Exception ex) {
                // Se parsing fallisce assumiamo attiva per sicurezza (o log errore)
                activeChallenges.add(s);
            }
        }

        // Sezione Sfide Attive
        contentContainer.getChildren().add(createSectionHeader("🔥 Sfide Attive"));
        if (activeChallenges.isEmpty()) {
            contentContainer.getChildren().add(createEmptyState("💤", "Nessuna sfida attiva al momento."));
        } else {
            for (Sfida s : activeChallenges) {
                contentContainer.getChildren().add(new SfidaCard(s, utenteLoggato));
            }
        }

        // Separatore
        contentContainer.getChildren().add(createSeparator());

        // Sezione Sfide Concluse
        contentContainer.getChildren().add(createSecondaryHeader("🏆 Sfide Concluse"));
        if (pastChallenges.isEmpty()) {
            contentContainer.getChildren().add(createEmptyState("📂", "Nessuna sfida conclusa recente."));
        } else {
            for (Sfida s : pastChallenges) {
                SfidaCard card = new SfidaCard(s, utenteLoggato);
                card.setOpacity(0.7); // Visualizzazione "disattivata"
                contentContainer.getChildren().add(card);
            }
        }
    }
}
