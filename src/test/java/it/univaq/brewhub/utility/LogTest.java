package it.univaq.brewhub.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per la classe di logging {@link Log}.
 *
 * Verifica che i metodi di logging non lancino eccezioni.
 *
 */
class LogTest {
    @Test
    void testLoggingDoesNotThrow() {
        assertDoesNotThrow(() -> Log.info("Test Info Message"));
        assertDoesNotThrow(() -> Log.warning("Test Warning Message"));
        assertDoesNotThrow(() -> Log.error("Test Error Message", new RuntimeException("Test Ex")));
    }
}
