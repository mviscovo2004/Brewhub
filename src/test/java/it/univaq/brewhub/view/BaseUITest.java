package it.univaq.brewhub.view;

import it.univaq.brewhub.BaseTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Classe base per i test dell'interfaccia utente (UI).
 *
 * Estende {@link BaseTest} per fornire funzionalità comuni ai test UI, come
 * l'inizializzazione del database
 * e la gestione del ciclo di vita dei test JavaFX.
 *
 */
public abstract class BaseUITest extends BaseTest {
    private boolean dbInitialized = false;

    /**
     * Configurazione iniziale per ogni test.
     * Inizializza il database se non è già stato fatto.
     * 
     * @throws SQLException se si verifica un errore durante l'inizializzazione del
     *                      database.
     */
    @Override
    @BeforeEach
    public void baseSetUp() throws SQLException {
        if (!dbInitialized) {
            super.baseSetUp();
            dbInitialized = true;
        }
    }

    /**
     * Assicura che il database sia pronto per l'uso nei test.
     * Se non è inizializzato, richiama {@link #baseSetUp()}.
     * 
     * @throws RuntimeException se l'inizializzazione del database fallisce.
     */
    protected void ensureDatabaseReady() {
        try {
            if (!dbInitialized) {
                baseSetUp();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize Database in UI Test", e);
        }
    }

    /**
     * Pulizia dopo ogni test UI.
     * Attende che tutti gli eventi JavaFX siano completati e procede alla pulizia
     * base.
     * 
     * @throws TimeoutException se l'attesa supera il tempo limite.
     */
    @AfterEach
    public void uiTearDown() throws TimeoutException {
        try {
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            super.baseTearDown();
            dbInitialized = false;
        }
    }
}
