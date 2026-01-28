package it.univaq.brewhub.utility;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per la classe di logging {@link Log}.
 * Verifica che i metodi di logging non lancino eccezioni durante l'esecuzione.
 */
class LogTest {

    /**
     * Verifica che i metodi di logging info, warning ed error vengano eseguiti
     * senza errori.
     */
    @Test
    void testLoggingDoesNotThrow() {
        assertDoesNotThrow(() -> Log.info("Test Info Message"));
        assertDoesNotThrow(() -> Log.warning("Test Warning Message"));
        assertDoesNotThrow(() -> Log.error("Test Error Message", new RuntimeException("Test Ex")));
    }
}
