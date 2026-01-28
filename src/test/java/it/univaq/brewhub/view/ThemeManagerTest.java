package it.univaq.brewhub.view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ThemeManagerTest {
    @Test
    void testColors() {
        assertNotNull(ThemeManager.Colors.DARK_COFFEE);
        assertFalse(ThemeManager.Colors.DARK_COFFEE.isEmpty());
        assertNotNull(ThemeManager.Colors.MEDIUM_COFFEE);
        assertNotNull(ThemeManager.Colors.LIGHT_COFFEE);
        assertNotNull(ThemeManager.Colors.CREAM);
        assertNotNull(ThemeManager.Colors.WHITE_CREAM);
    }
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
    @Test
    void testThemeColors() {
        String font = ThemeManager.getCoffeeThemeColors();
        assertNotNull(font);
        assertTrue(font.contains("Segoe UI"));
    }
}
