package it.univaq.brewhub.UI;

import javafx.scene.Scene;

/**
 * Gestore del Tema UI per l'applicazione BrewHub.
 * Definisce i colori e gli stili "Coffee Theme" e fornisce metodi per
 * applicarli alle scene JavaFX.
 */
public class ThemeManager {

        /** Nome del file CSS risorsa per il tema (es. style.css). */
        private static final String CSS_RESOURCE = "style.css";

        /**
         * Applica il tema predefinito (Caffè) alla scena specificata.
         * Carica il foglio di stile CSS dalle risorse del classpath.
         *
         * @param scene La scena JavaFX a cui applicare il tema.
         */
        public static void applyTheme(Scene scene) {
                try {
                        // Ottiene l'URL della risorsa CSS e la aggiunge agli stylesheets della scena
                        String cssResource = ThemeManager.class.getResource("/" + CSS_RESOURCE).toExternalForm();
                        scene.getStylesheets().add(cssResource);
                } catch (Exception e) {
                        // Log dell'errore se il file CSS non viene trovato o caricato
                        System.err.println("Errore nel caricamento del tema CSS: " + e.getMessage());
                }
        }

        /**
         * Restituisce una stringa di stile CSS base per il font dell'applicazione.
         * 
         * @return Stringa stile CSS per il font-family.
         */
        public static String getCoffeeThemeColors() {
                return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif;";
        }

        /**
         * Costanti per i colori della palette "Coffee Theme".
         * Usati staticamente per definire stili o per riferimento.
         */
        public static class Colors {
                public static final String DARK_COFFEE = "#2C1810"; // Nero caffè
                public static final String MEDIUM_COFFEE = "#5D4037"; // Marrone medio
                public static final String LIGHT_COFFEE = "#8D6E63"; // Marrone chiaro
                public static final String PALE_COFFEE = "#A1887F"; // Marrone pallido
                public static final String CREAM = "#F5E6D3"; // Crema
                public static final String WHITE_CREAM = "#FFF8F3"; // Bianco panna
                public static final String COPPER = "#D4A574"; // Rame/Beige
                public static final String GOLD = "#C9A876"; // Oro caffè
                public static final String ACCENT_GREEN = "#6B8E23"; // Verde salvia
                public static final String ACCENT_BROWN = "#A0522D"; // Marrone scuro accento
        }

        /**
         * Definizione programmatica degli stili CSS per componenti UI specifici.
         * Utile quando si vuole applicare stile inline dinamico in
         * aggiunta/sostituzione al file .css.
         */
        public static class Styles {

                /**
                 * Stile per bottoni primari (azione principale).
                 * 
                 * @return Stringa CSS.
                 */
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

                /**
                 * Stile per bottoni di successo (es. conferma, salva).
                 * 
                 * @return Stringa CSS.
                 */
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

                /**
                 * Stile per bottoni secondari (es. annulla, info secondarie).
                 * 
                 * @return Stringa CSS.
                 */
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

                /**
                 * Stile per bottoni di pericolo (es. elimina, logout).
                 * 
                 * @return Stringa CSS.
                 */
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

                /**
                 * Stile per campi di testo (TextField).
                 * 
                 * @return Stringa CSS.
                 */
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

                /**
                 * Stile per l'header dell'applicazione.
                 * 
                 * @return Stringa CSS.
                 */
                public static String header() {
                        return String.format(
                                        "-fx-background-color: %s; " +
                                                        "-fx-padding: 12 20 12 20; " +
                                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                                        Colors.MEDIUM_COFFEE);
                }

                /**
                 * Stile generico per card/pannelli.
                 * 
                 * @return Stringa CSS.
                 */
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

                /**
                 * Stile per la sidebar di navigazione.
                 * 
                 * @return Stringa CSS.
                 */
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
