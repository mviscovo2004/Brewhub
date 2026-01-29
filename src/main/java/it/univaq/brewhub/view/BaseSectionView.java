package it.univaq.brewhub.view;

import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.view.utils.UiUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Classe base astratta per le viste di sezione (es. Eventi, Sfide).
 * Gestisce il layout comune caratterizzato da:
 * <ul>
 * <li>Header con titolo e pulsante opzionale di azione</li>
 * <li>Area di contenuto principale scrollabile</li>
 * <li>Helper per la creazione di sezioni e stati vuoti</li>
 * </ul>
 */
public abstract class BaseSectionView extends BorderPane {

    protected final Utente utenteLoggato;
    protected VBox contentContainer;

    /**
     * Costruisce la vista di base.
     *
     * @param utenteLoggato L'utente che sta visualizzando la sezione.
     */
    public BaseSectionView(Utente utenteLoggato) {
        this.utenteLoggato = utenteLoggato;
        initBaseUI();
    }

    /**
     * Inizializza l'interfaccia utente comune.
     * Imposta l'header e il contenitore scrollabile.
     */
    private void initBaseUI() {
        this.setPadding(new Insets(20));

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(getSectionTitle());
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer);

        // Action Button (Optional)
        Button actionButton = createActionButton();
        if (actionButton != null) {
            header.getChildren().add(actionButton);
        }

        this.setTop(header);

        // Scrollable Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        contentContainer = new VBox(15);
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setPadding(new Insets(10));

        scrollPane.setContent(contentContainer);
        this.setCenter(scrollPane);
    }

    /**
     * Restituisce il titolo della sezione.
     *
     * @return Stringa del titolo (es. "🗓 Eventi").
     */
    protected abstract String getSectionTitle();

    /**
     * Crea il pulsante di azione nell'header (es. "+ Crea Evento").
     *
     * @return Il pulsante configurato o null se non è prevista azione.
     */
    protected abstract Button createActionButton();

    /**
     * Metodo chiamato per caricare e aggiornare i dati nel contenitore principale.
     * Le implementazioni devono pulire {@code contentContainer.getChildren()} e
     * ripopolarlo.
     */
    protected abstract void refreshData();

    // --- Utility helpers for subclasses ---

    /**
     * Crea un'etichetta di intestazione per una sottosezione.
     *
     * @param text Testo dell'intestazione.
     * @return Label stilizzata.
     */
    protected Label createSectionHeader(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-header");
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-padding: 10 0 5 0;");
        return label;
    }

    /**
     * Crea un'etichetta di intestazione secondaria (es. per elementi passati).
     *
     * @param text Testo dell'intestazione.
     * @return Label stilizzata.
     */
    protected Label createSecondaryHeader(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5D4037; -fx-padding: 10 0 5 0; -fx-opacity: 0.8;");
        return label;
    }

    /**
     * Crea uno spaziatore verticale.
     *
     * @return Region usata come separatore.
     */
    protected Region createSeparator() {
        Region separator = new Region();
        separator.setMinHeight(20);
        return separator;
    }

    /**
     * Crea un nodo che rappresenta uno stato vuoto.
     * Delegato a {@link UiUtils#createEmptyState(String)}.
     * NB: Attualmente UiUtils accetta solo text, qui usiamo emoji+messaggio in
     * stringa singola.
     *
     * @param emoji   Emoji rappresentativa.
     * @param message Messaggio descrittivo.
     * @return Nodo UI.
     */
    protected Node createEmptyState(String emoji, String message) {
        // Uniamo emoji e messaggio poiché UiUtils.createEmptyState prende solo una
        // stringa
        return UiUtils.createEmptyState(emoji + " " + message);
    }
}
