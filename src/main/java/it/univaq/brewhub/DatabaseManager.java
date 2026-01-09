package it.univaq.brewhub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import it.univaq.brewhub.utility.Log;

/**
 * Gestione della connessione e inizializzazione del database SQLite.
 * Fornisce metodi statici per ottenere connessioni e creare lo schema del DB.
 */
public class DatabaseManager {

    // Il file del DB verrà creato nella cartella del progetto
    /** URL di connessione JDBC per SQLite. */
    private static final String URL = "jdbc:sqlite:brewhub.db";

    /**
     * Ottiene una connessione attiva al database SQLite.
     * 
     * @return Connection oggetto connessione JDBC.
     * @throws SQLException In caso di errore di connessione.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Inizializza il database creando le tabelle necessarie se non esistono.
     * Abilita inoltre il supporto per le chiavi esterne.
     * Tabelle gestite: utenti, post, commenti.
     */
    public static void init() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Abilita le chiavi esterne per garantire integrità referenziale
            stmt.execute("PRAGMA foreign_keys = ON");

            // Creazione tabella Utenti
            String sqlUtenti = "CREATE TABLE IF NOT EXISTS utenti (" +
                    "username TEXT PRIMARY KEY, " +
                    "nome TEXT NOT NULL, " +
                    "cognome TEXT NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "tipo TEXT NOT NULL, " +
                    "foto_uri TEXT)";
            stmt.execute(sqlUtenti);

            // Creazione tabella Post
            String sqlPost = "CREATE TABLE IF NOT EXISTS post (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "titolo TEXT NOT NULL, " +
                    "contenuto TEXT, " +
                    "tipo TEXT NOT NULL, " +
                    "media_uri TEXT, " +
                    "autore_username TEXT NOT NULL, " +
                    "data_creazione TEXT NOT NULL, " +
                    "FOREIGN KEY(autore_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlPost);

            // Creazione tabella Commenti
            String sqlCommenti = "CREATE TABLE IF NOT EXISTS commenti (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "post_id INTEGER NOT NULL, " +
                    "username TEXT, " +
                    "contenuto TEXT NOT NULL, " +
                    "data_creazione TEXT NOT NULL, " +
                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE SET NULL" +
                    ")";
            stmt.execute(sqlCommenti);

            // Creazione tabella Likes
            String sqlLikes = "CREATE TABLE IF NOT EXISTS likes (" +
                    "post_id INTEGER NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "PRIMARY KEY (post_id, username), " +
                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlLikes);

            // Indici per ottimizzare le query frequenti
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_post_autore ON post(autore_username)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_commenti_post ON commenti(post_id)");

            // Log di successo
            Log.info("Database inizializzato correttamente (tabelle utenti, post, commenti).");

        } catch (SQLException e) {
            // Gestione errore inizializzazione DB
            System.err.println("Errore inizializzazione DB: " + e.getMessage());
        }
    }
}