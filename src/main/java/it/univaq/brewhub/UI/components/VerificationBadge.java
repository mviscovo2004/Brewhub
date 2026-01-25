package it.univaq.brewhub.UI.components;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.Tooltip;

/**
 * Componente grafico che mostra un badge di verifica in stile "seal" con
 * spunta.
 * Il colore è personalizzabile (default: color caffè/oro #D4A574).
 */
public class VerificationBadge extends StackPane {

    // SVG Path per una forma a sigillo (scalloped circle o stellata)
    private static final String SEAL_PATH = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z";
    // Spunta interna
    private static final String CHECK_PATH = "M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z";

    public VerificationBadge() {
        this(14); // Default size
    }

    public VerificationBadge(double size) {
        // Base: Sigillo
        SVGPath seal = new SVGPath();
        seal.setContent(SEAL_PATH);
        seal.getStyleClass().add("verification-seal");

        // Scaling
        double scale = size / 24.0; // Assumendo viewBox 0 0 24 24
        seal.setScaleX(scale);
        seal.setScaleY(scale);

        // Icona: Spunta
        SVGPath check = new SVGPath();
        check.setContent(CHECK_PATH);
        check.getStyleClass().add("verification-check");
        check.setScaleX(scale * 0.7); // Leggermente più piccola
        check.setScaleY(scale * 0.7);

        this.getChildren().addAll(seal, check);

        // Tooltip default
        Tooltip.install(this, new Tooltip("Utente Verificato (Torrefattore)"));

        // Margini minimi per evitare clipping dello scaling
        this.setMinWidth(size);
        this.setMinHeight(size);
        this.setPrefSize(size, size);
        this.setMaxSize(size, size);
    }
}
