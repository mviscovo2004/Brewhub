package it.univaq.brewhub.utility;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test unitari per la classe {@link DatabaseManager}.
 * Verifica la configurazione del database di test e l'inizializzazione dello
 * schema con creazione delle tabelle.
 */
public class DatabaseManagerTest {
    @TempDir
    File tempDir;

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        DatabaseManager.shutdown();
        System.gc();

        // Pulizia file specifici creati dai test
        String[] testDbs = { "temp_test_db_config.db", "temp_test_db_init.db" };
        for (String name : testDbs) {
            File f = new File(tempDir, name);
            if (f.exists()) {
                deleteWithRetry(f);
                deleteWithRetry(new File(f.getAbsolutePath() + "-shm"));
                deleteWithRetry(new File(f.getAbsolutePath() + "-wal"));
            }
        }
    }

    private void deleteWithRetry(File file) {
        if (!file.exists())
            return;
        for (int i = 0; i < 10; i++) {
            if (file.delete())
                return;
            try {
                System.gc();
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Verifica la configurazione del database di test.
     */
    @Test
    public void testConfigureTestDatabase() {
        String testDbName = "temp_test_db_config.db";
        File dbFile = new File(tempDir, testDbName);
        DatabaseManager.configureTestDatabase(dbFile.getAbsolutePath());
        try (Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn);
            String url = conn.getMetaData().getURL();
            assertTrue(url.contains(dbFile.getName()) || url.replace("\\", "/").contains(dbFile.getName()));
        } catch (SQLException e) {
            fail("Impossibile ottenere connessione: " + e.getMessage());
        }
    }

    /**
     * Verifica l'inizializzazione del database (creazione tabelle).
     * 
     * @throws SQLException se si verifica un errore durante l'inizializzazione o
     *                      l'accesso al database.
     */
    @Test
    public void testInit() throws SQLException {
        // Usa un db dedicato per non interferire
        String testDbName = "temp_test_db_init.db";
        File dbFile = new File(tempDir, testDbName);
        DatabaseManager.configureTestDatabase(dbFile.getAbsolutePath());
        // Prima dell'init, le tabelle non dovrebbero esistere
        // Init crea tutto
        try {
            DatabaseManager.init();
        } catch (Exception e) {
            fail("Init ha lanciato eccezione: " + e.getMessage());
        }
        // Verifica che le tabelle esistano
        try (Connection conn = DatabaseManager.getConnection();
                java.sql.ResultSet rs = conn.getMetaData().getTables(null, null, "utenti", null)) {
            assertTrue(rs.next(), "La tabella 'utenti' dovrebbe esistere");
        }
        try (Connection conn = DatabaseManager.getConnection();
                java.sql.ResultSet rs = conn.getMetaData().getTables(null, null, "post", null)) {
            assertTrue(rs.next(), "La tabella 'post' dovrebbe esistere");
        }
    }
}
