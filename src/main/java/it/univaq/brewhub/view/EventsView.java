package it.univaq.brewhub.view;

import it.univaq.brewhub.business.EventoService;
import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.components.EventCard;
import it.univaq.brewhub.view.utils.EventDialogManager;
import javafx.scene.control.Button;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista per la gestione e visualizzazione degli Eventi.
 * Estende {@link BaseSectionView} per ereditare la struttura comune.
 *
 * <p>
 * Offre due sezioni principali:
 * <ul>
 * <li>Eventi in programma: visualizza gli eventi futuri filtrati per data.</li>
 * <li>Eventi passati: storico degli eventi trascorsi.</li>
 * </ul>
 *
 * I Torrefattori e gli Amministratori hanno accesso al pulsante per creare
 * nuovi eventi.
 */
public class EventsView extends BaseSectionView {

    private final EventoService eventoService = EventoService.getInstance();

    /**
     * Costruisce la vista Eventi.
     *
     * @param utenteLoggato L'utente corrente.
     */
    public EventsView(Utente utenteLoggato) {
        super(utenteLoggato);
        refreshData();
    }

    @Override
    protected String getSectionTitle() {
        return "🗓 Eventi";
    }

    @Override
    protected Button createActionButton() {
        // Mostra bottone solo per Torrefattore o Admin
        if (utenteLoggato.getTipo() == Utente.TipoUtente.TORREFATTORE
                || utenteLoggato.getTipo() == Utente.TipoUtente.ADMIN) {
            Button btnCreate = new Button("+ Crea Evento");
            btnCreate.getStyleClass().add("button-primary");
            btnCreate.setOnAction(e -> EventDialogManager.showCreateEventDialog(
                    this.getScene().getWindow(),
                    utenteLoggato,
                    this::refreshData // Callback per ricaricare la lista dopo creazione
            ));
            return btnCreate;
        }
        return null;
    }

    @Override
    protected void refreshData() {
        contentContainer.getChildren().clear();
        List<Evento> allEvents = eventoService.getAllEvents();
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
                // Fallback: se la data non è parsabile, lo consideriamo futuro per non perderlo
                upcomingEvents.add(e);
            }
        }

        // Sezione Imminenti
        contentContainer.getChildren().add(createSectionHeader("🗓 Eventi in programma"));
        if (upcomingEvents.isEmpty()) {
            contentContainer.getChildren().add(createEmptyState("🗓", "Nessun evento in programma."));
        } else {
            for (Evento e : upcomingEvents) {
                contentContainer.getChildren().add(new EventCard(e, utenteLoggato));
            }
        }

        // Separatore
        contentContainer.getChildren().add(createSeparator());

        // Sezione Passati
        contentContainer.getChildren().add(createSecondaryHeader("🕰 Eventi Passati"));
        if (pastEvents.isEmpty()) {
            contentContainer.getChildren().add(createEmptyState("🕰", "Nessun evento passato recente."));
        } else {
            for (Evento e : pastEvents) {
                EventCard card = new EventCard(e, utenteLoggato);
                card.setOpacity(0.7); // Visualizzazione "disattivata"
                contentContainer.getChildren().add(card);
            }
        }
    }
}
