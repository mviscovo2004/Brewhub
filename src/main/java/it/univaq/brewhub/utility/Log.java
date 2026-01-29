package it.univaq.brewhub.utility;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Classe di utilità per il logging centralizzato.
 * Configura un Logger statico per l'applicazione "BrewHub" e fornisce metodi
 * helper
 * per registrare messaggi a diversi livelli di severità.
 */
public class Log {

    private static final Logger LOGGER = Logger.getLogger("BrewHub");

    // Configurazione statica del logger all'avvio della classe
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
    }

    /**
     * Registra un messaggio di livello INFO.
     *
     * @param msg Il messaggio informativo.
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * Registra un messaggio di errore (livello SEVERE) associato a un'eccezione.
     *
     * @param msg Il messaggio descrittivo dell'errore.
     * @param e   L'eccezione catturata.
     */
    public static void error(String msg, Throwable e) {
        LOGGER.log(Level.SEVERE, msg, e);
    }

    /**
     * Registra un messaggio di avviso (livello WARNING).
     *
     * @param msg Il messaggio di warning.
     */
    public static void warning(String msg) {
        LOGGER.warning(msg);
    }
}
