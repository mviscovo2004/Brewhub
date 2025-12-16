package it.univaq.brewhub.UI;

// Importazioni JavaFX
import javafx.scene.Scene;

// Gestione del Tema Caffè per l'applicazione
public class ThemeManager {
    
    // Risorsa CSS del tema
    private static final String CSS_RESOURCE = "style.css";
    
    // Applica il tema caffè a una scena JavaFX
    public static void applyTheme(Scene scene) {

        // Caricamento del file CSS
        try {

            // Aggiungi il foglio di stile alla scena
            String cssResource = ThemeManager.class.getResource("/" + CSS_RESOURCE).toExternalForm();
            scene.getStylesheets().add(cssResource);
        } catch (Exception e) {

            // Gestione errore caricamento CSS
            System.err.println("Errore nel caricamento del tema CSS: " + e.getMessage());
        }
    }
    
    // Ottiene i colori principali del tema caffè
    public static String getCoffeeThemeColors() {
        return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif;";
    }
    
    // Definizione dei colori del tema caffè
    public static class Colors {
        public static final String DARK_COFFEE = "#2C1810";      // Nero caffè
        public static final String MEDIUM_COFFEE = "#5D4037";    // Marrone medio
        public static final String LIGHT_COFFEE = "#8D6E63";     // Marrone chiaro
        public static final String PALE_COFFEE = "#A1887F";      // Marrone pallido
        public static final String CREAM = "#F5E6D3";            // Crema
        public static final String WHITE_CREAM = "#FFF8F3";      // Bianco panna
        public static final String COPPER = "#D4A574";           // Rame/Beige
        public static final String GOLD = "#C9A876";             // Oro caffè
        public static final String ACCENT_GREEN = "#6B8E23";     // Verde salvia
        public static final String ACCENT_BROWN = "#A0522D";     // Marrone scuro accento
    }
    
    // Definizione degli stili CSS per i componenti UI
    public static class Styles {
        
        // Stili per bottoni primari
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
                Colors.MEDIUM_COFFEE, Colors.WHITE_CREAM
            );
        }
        
        // Stili per bottoni di successo
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
                Colors.ACCENT_GREEN, Colors.WHITE_CREAM
            );
        }
        
        // Stili per bottoni secondari
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
                Colors.PALE_COFFEE, Colors.WHITE_CREAM
            );
        }
        
        // Stili per bottoni di pericolo
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
                Colors.ACCENT_BROWN, Colors.WHITE_CREAM
            );
        }
        
        // Stili per campi di testo
        public static String textField() {
            return String.format(
                "-fx-padding: 10; " +
                "-fx-font-size: 13; " +
                "-fx-border-color: %s; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-control-inner-background: %s; " +
                "-fx-text-fill: %s;",
                Colors.COPPER, Colors.WHITE_CREAM, Colors.DARK_COFFEE
            );
        }
        
        // Stili per header
        public static String header() {
            return String.format(
                "-fx-background-color: %s; " +
                "-fx-padding: 12 20 12 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                Colors.MEDIUM_COFFEE
            );
        }
        
        // Stili per card
        public static String card() {
            return String.format(
                "-fx-background-color: %s; " +
                "-fx-border-color: %s; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-padding: 18; " +
                "-fx-effect: dropshadow(gaussian, rgba(93, 64, 55, 0.15), 3, 0, 0, 1);",
                Colors.WHITE_CREAM, Colors.COPPER
            );
        }
        
        // Stili per sidebar
        public static String sidebar() {
            return String.format(
                "-fx-background-color: %s; " +
                "-fx-border-color: %s; " +
                "-fx-border-width: 0 1 0 0; " +
                "-fx-padding: 15;",
                Colors.CREAM, Colors.COPPER
            );
        }
    }
}
