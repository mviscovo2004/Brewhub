package it.univaq.brewhub.utility;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Classe di utilità per il logging centralizzato dell'applicazione.
 * Wrapper intorno a {@link java.util.logging.Logger} per semplificare l'invio
 * di messaggi di log.
 */
public class Log {

    private static final Logger LOGGER = Logger.getLogger("BrewHub");

    // Blocco statico per configurare il logger all'avvio
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
    }

    /**
     * Logga un messaggio informativo.
     * 
     * @param msg Il messaggio.
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * Logga un errore grave con relativa eccezione.
     * 
     * @param msg Il messaggio di errore.
     * @param e   L'eccezione catturata.
     */
    public static void error(String msg, Throwable e) {
        LOGGER.log(Level.SEVERE, msg, e);
    }

    /**
     * Logga un avvertimento.
     * 
     * @param msg Il messaggio di warning.
     */
    public static void warning(String msg) {
        LOGGER.warning(msg);
    }
}