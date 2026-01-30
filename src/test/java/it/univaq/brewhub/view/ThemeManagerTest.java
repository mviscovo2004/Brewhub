package it.univaq.brewhub.view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per la classe di gestione del tema {@link ThemeManager}.
 * Verifica la correttezza delle costanti di colore e degli stili CSS generati.
 */
class ThemeManagerTest {

    /**
     * Verifica che le costanti di colore non siano nulle o vuote.
     */
    @Test
    void testColors() {
        assertNotNull(ThemeManager.Colors.DARK_COFFEE);
        assertFalse(ThemeManager.Colors.DARK_COFFEE.isEmpty());
        assertNotNull(ThemeManager.Colors.MEDIUM_COFFEE);
        assertNotNull(ThemeManager.Colors.LIGHT_COFFEE);
        assertNotNull(ThemeManager.Colors.CREAM);
        assertNotNull(ThemeManager.Colors.WHITE_CREAM);
    }

    /**
     * Verifica che gli stili CSS generati contengano le proprietà corrette.
     */
    @Test
    void testStyles() {
        String btnStyle = ThemeManager.Styles.buttonPrimary();
        assertNotNull(btnStyle);
        assertTrue(btnStyle.contains("-fx-background-color"));
        assertTrue(btnStyle.contains("-fx-text-fill"));
        String dangerStyle = ThemeManager.Styles.buttonDanger();
        assertNotNull(dangerStyle);
        assertTrue(dangerStyle.contains("-fx-background-color"));
        String headerStyle = ThemeManager.Styles.header();
        assertNotNull(headerStyle);
        assertTrue(headerStyle.contains("-fx-effect"));
    }

    /**
     * Verifica la definizione globale del tema (font).
     */
    @Test
    void testThemeColors() {
        String font = ThemeManager.getCoffeeThemeColors();
        assertNotNull(font);
        assertTrue(font.contains("Segoe UI"));
    }
}
