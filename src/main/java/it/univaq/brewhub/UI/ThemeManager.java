package it.univaq.brewhub.UI;

import javafx.scene.Scene;

/**
 * Gestore centralizzato del tema e degli stili grafici dell'applicazione.
 *
 * Definisce la palette colori (Coffee Theme) e fornisce metodi per applicare
 * stili CSS coerenti ai componenti JavaFX.
 *
 */
public class ThemeManager {

        private static final String CSS_RESOURCE = "style.css";

        /**
         * Applica il foglio di stile CSS alla scena specificata.
         * 
         * @param scene La scena JavaFX a cui applicare il tema.
         */
        public static void applyTheme(Scene scene) {
                try {
                        String cssResource = ThemeManager.class.getResource("/" + CSS_RESOURCE).toExternalForm();
                        scene.getStylesheets().add(cssResource);
                } catch (Exception e) {
                        System.err.println("Errore nel caricamento del tema CSS: " + e.getMessage());
                }
        }

        /**
         * Restituisce lo stile CSS base per il font dell'applicazione.
         * 
         * @return Stringa CSS per font-family.
         */
        public static String getCoffeeThemeColors() {
                return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif;";
        }

        /**
         * Palette colori definita come costanti statiche.
         * I colori sono ispirati alle tonalità del caffè, dal marrone scuro al crema.
         */
        public static class Colors {
                /** Marrone molto scuro, quasi nero (Espresso). Usato per testo principale. */
                public static final String DARK_COFFEE = "#2C1810";
                /** Marrone medio (Moka). Usato per header e sfondi scuri. */
                public static final String MEDIUM_COFFEE = "#5D4037";
                /** Marrone chiaro (Cappuccino). Usato per elementi secondari. */
                public static final String LIGHT_COFFEE = "#8D6E63";
                /** Beige scuro (Latte Macchiato). Usato per bordi o sfondi tenui. */
                public static final String PALE_COFFEE = "#A1887F";
                /** Crema chiaro. Sfondo principale dell'app. */
                public static final String CREAM = "#F5E6D3";
                /** Bianco panna. Sfondo delle card e aree di contenuto. */
                public static final String WHITE_CREAM = "#FFF8F3";
                /** Color Rame/Bronzo. Usato per accenti e focus. */
                public static final String COPPER = "#D4A574";
                /** Oro spento. Usato per elementi premium o badge. */
                public static final String GOLD = "#C9A876";
                /** Verde Oliva. Colore di accento positivo (Successo/Conferma). */
                public static final String ACCENT_GREEN = "#6B8E23";
                /** Ruggine/Marrone rossiccio. Colore di accento negativo (Errore/Elimina). */
                public static final String ACCENT_BROWN = "#A0522D";
        }

        /**
         * Generatori di stili CSS inline per componenti comuni.
         * Utili quando lo stile tramite classe CSS non è sufficiente o per stili
         * dinamici.
         */
        public static class Styles {

                /** @return Stile per bottone primario. */
                public static String buttonPrimary() {
                        return String.format(
                                        "-fx-padding: 10 30 10 30; " +
                                                        "-fx-font-size: 14; " +
                                                        "-fx-font-weight: bold; " +
                                                        "-fx-background-color: %s; " +
                                                        "-fx-text-fill: %s; " +
                                                        "-fx-border-radius: 5; " +
                                                        "-fx-background-radius: 5; " +
                                                        "-fx-cursor: hand;",
                                        Colors.MEDIUM_COFFEE, Colors.WHITE_CREAM);
                }

                /** @return Stile per bottone di successo (es. Conferma). */
                public static String buttonSuccess() {
                        return String.format(
                                        "-fx-padding: 10 30 10 30; " +
                                                        "-fx-font-size: 14; " +
                                                        "-fx-font-weight: bold; " +
                                                        "-fx-background-color: %s; " +
                                                        "-fx-text-fill: %s; " +
                                                        "-fx-border-radius: 5; " +
                                                        "-fx-background-radius: 5; " +
                                                        "-fx-cursor: hand;",
                                        Colors.ACCENT_GREEN, Colors.WHITE_CREAM);
                }

                /** @return Stile per bottone secondario (es. Annulla). */
                public static String buttonSecondary() {
                        return String.format(
                                        "-fx-padding: 10 30 10 30; " +
                                                        "-fx-font-size: 14; " +
                                                        "-fx-font-weight: bold; " +
                                                        "-fx-background-color: %s; " +
                                                        "-fx-text-fill: %s; " +
                                                        "-fx-border-radius: 5; " +
                                                        "-fx-background-radius: 5; " +
                                                        "-fx-cursor: hand;",
                                        Colors.PALE_COFFEE, Colors.WHITE_CREAM);
                }

                /** @return Stile per bottone di pericolo (es. Elimina). */
                public static String buttonDanger() {
                        return String.format(
                                        "-fx-padding: 10 30 10 30; " +
                                                        "-fx-font-size: 14; " +
                                                        "-fx-font-weight: bold; " +
                                                        "-fx-background-color: %s; " +
                                                        "-fx-text-fill: %s; " +
                                                        "-fx-border-radius: 5; " +
                                                        "-fx-background-radius: 5; " +
                                                        "-fx-cursor: hand;",
                                        Colors.ACCENT_BROWN, Colors.WHITE_CREAM);
                }

                /** @return Stile base per campi di testo. */
                public static String textField() {
                        return String.format(
                                        "-fx-padding: 10; " +
                                                        "-fx-font-size: 13; " +
                                                        "-fx-border-color: %s; " +
                                                        "-fx-border-radius: 5; " +
                                                        "-fx-background-radius: 5; " +
                                                        "-fx-control-inner-background: %s; " +
                                                        "-fx-text-fill: %s;",
                                        Colors.COPPER, Colors.WHITE_CREAM, Colors.DARK_COFFEE);
                }

                /** @return Stile per header delle sezioni. */
                public static String header() {
                        return String.format(
                                        "-fx-background-color: %s; " +
                                                        "-fx-padding: 12 20 12 20; " +
                                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                                        Colors.MEDIUM_COFFEE);
                }

                /** @return Stile per card (contenitori). */
                public static String card() {
                        return String.format(
                                        "-fx-background-color: %s; " +
                                                        "-fx-border-color: %s; " +
                                                        "-fx-background-radius: 8; " +
                                                        "-fx-border-radius: 8; " +
                                                        "-fx-padding: 18; " +
                                                        "-fx-effect: dropshadow(gaussian, rgba(93, 64, 55, 0.15), 3, 0, 0, 1);",
                                        Colors.WHITE_CREAM, Colors.COPPER);
                }

                /** @return Stile per sidebar laterale. */
                public static String sidebar() {
                        return String.format(
                                        "-fx-background-color: %s; " +
                                                        "-fx-border-color: %s; " +
                                                        "-fx-border-width: 0 1 0 0; " +
                                                        "-fx-padding: 15;",
                                        Colors.CREAM, Colors.COPPER);
                }
        }
}