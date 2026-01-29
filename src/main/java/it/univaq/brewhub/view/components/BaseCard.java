package it.univaq.brewhub.view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Classe astratta che funge da base per le card dell'interfaccia utente (es.
 * Eventi, Sfide).
 * Fornisce metodi di utilità per la creazione standardizzata di elementi
 * grafici come etichette e layout.
 */
public abstract class BaseCard extends VBox {

    /**
     * Costruttore predefinito.
     * Inizializza lo stile di base della card.
     */
    public BaseCard() {
        initBaseStyle();
    }

    /**
     * Imposta lo stile di base, inclusa la spaziatura, la larghezza massima e la
     * classe CSS.
     */
    private void initBaseStyle() {
        this.setSpacing(10);
        this.setMaxWidth(700);
        this.getStyleClass().add("post-card");
    }

    /**
     * Crea un'etichetta per il titolo con lo stile appropriato.
     *
     * @param text Il testo del titolo.
     * @return L'oggetto Label configurato.
     */
    protected Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("post-title");
        return label;
    }

    /**
     * Crea un'etichetta per il contenuto principale (descrizione, testo) con
     * supporto al wrap del testo.
     *
     * @param text Il testo del contenuto.
     * @return L'oggetto Label configurato.
     */
    protected Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("post-content");
        return label;
    }

    /**
     * Crea un'etichetta per informazioni primarie evidenziate con un colore
     * specifico e grassetto.
     *
     * @param text  Il testo dell'etichetta.
     * @param color Il colore del testo (es. codice esadecimale o nome colore).
     * @return L'oggetto Label configurato.
     */
    protected Label createPrimaryInfoLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        return label;
    }

    /**
     * Crea un'etichetta per informazioni secondarie con un colore specifico.
     * Se il testo contiene la parola "Premio", applica automaticamente il
     * grassetto.
     *
     * @param text  Il testo dell'etichetta.
     * @param color Il colore del testo.
     * @return L'oggetto Label configurato.
     */
    protected Label createSecondaryInfoLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + color + ";");
        if (text.contains("Premio")) {
            label.setStyle(label.getStyle() + " -fx-font-weight: bold;");
        }
        return label;
    }

    /**
     * Crea un'etichetta per informazioni minori, con font ridotto e opacità
     * inferiore.
     *
     * @param text Il testo dell'etichetta.
     * @return L'oggetto Label configurato.
     */
    protected Label createSmallInfoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10px; -fx-opacity: 0.7;");
        return label;
    }

    /**
     * Crea un contenitore orizzontale (HBox) per il footer della card.
     *
     * @return L'oggetto HBox configurato.
     */
    protected HBox createFooterBox() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    /**
     * Crea uno spaziatore (Region) che cresce orizzontalmente per riempire lo
     * spazio disponibile.
     * Utile per allineare elementi a sinistra e a destra all'interno di un HBox.
     *
     * @return L'oggetto Region configurato come spaziatore.
     */
    protected Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
}
