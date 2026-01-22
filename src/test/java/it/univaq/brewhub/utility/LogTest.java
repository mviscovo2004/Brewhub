package it.univaq.brewhub.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogTest {

    @Test
    void testLoggingDoesNotThrow() {
        assertDoesNotThrow(() -> Log.info("Test Info Message"));
        assertDoesNotThrow(() -> Log.warning("Test Warning Message"));
        assertDoesNotThrow(() -> Log.error("Test Error Message", new RuntimeException("Test Ex")));
    }
}
