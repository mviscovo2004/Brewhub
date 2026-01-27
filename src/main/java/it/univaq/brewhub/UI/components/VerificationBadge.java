package it.univaq.brewhub.UI.components;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.Tooltip;

/**
 * Badge di verifica per utenti verificati.
 *
 * Componente UI che mostra un'icona di spunta sovrapposta a un sigillo
 * per indicare che un utente è verificato (tipicamente un Torrefattore).
 * Il badge è personalizzabile in dimensione e include un tooltip esplicativo.
 *
 */
public class VerificationBadge extends StackPane {
    private static final String SEAL_PATH = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z";
    private static final String CHECK_PATH = "M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z";

    /**
     * Costruttore con dimensione predefinita (14 pixel).
     */
    public VerificationBadge() {
        this(14);
    }

    /**
     * Costruttore con dimensione personalizzata.
     *
     * Crea un badge composto da un sigillo circolare e una spunta,
     * scalati proporzionalmente alla dimensione specificata.
     *
     * 
     * @param size Dimensione del badge in pixel
     */
    public VerificationBadge(double size) {
        SVGPath seal = new SVGPath();
        seal.setContent(SEAL_PATH);
        seal.getStyleClass().add("verification-seal");
        double scale = size / 24.0;
        seal.setScaleX(scale);
        seal.setScaleY(scale);
        SVGPath check = new SVGPath();
        check.setContent(CHECK_PATH);
        check.getStyleClass().add("verification-check");
        check.setScaleX(scale * 0.7);
        check.setScaleY(scale * 0.7);
        this.getChildren().addAll(seal, check);
        Tooltip.install(this, new Tooltip("Utente Verificato (Torrefattore)"));
        this.setMinWidth(size);
        this.setMinHeight(size);
        this.setPrefSize(size, size);
        this.setMaxSize(size, size);
    }
}
