package it.univaq.brewhub.utility;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Classe di utilità per la gestione del logging centralizzato.
 * Configura il logger di sistema e fornisce metodi di comodo per loggare
 * messaggi.
 */
public class Log {
    /** Istanza del Logger Java standard. */
    private static final Logger LOGGER = Logger.getLogger("BrewHub");

    static {
        // Configurazione base console
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
    }

    /**
     * Logga un messaggio informativo (Level.INFO).
     *
     * @param msg Il messaggio da loggare.
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * Logga un messaggio di errore (Level.SEVERE) con relativa eccezione.
     *
     * @param msg Il messaggio di errore.
     * @param e   L'eccezione causante (Throwable).
     */
    public static void error(String msg, Throwable e) {
        LOGGER.log(Level.SEVERE, msg, e);
    }

    /**
     * Logga un messaggio di avviso (Level.WARNING).
     *
     * @param msg Il messaggio di avviso.
     */
    public static void warning(String msg) {
        LOGGER.warning(msg);
    }
}
