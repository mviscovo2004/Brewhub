package it.univaq.brewhub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import it.univaq.brewhub.utility.Log;

/**
 * Gestore principale per la connessione e l'inizializzazione del database SQLite.
 * <p>
 * Questa classe fornisce metodi statici per ottenere connessioni al database,
 * configurare l'ambiente di test e inizializzare lo schema del database creando
 * le tabelle necessarie se non esistono.
 * </p>
 */
public class DatabaseManager {

    /** URL di connessione predefinito per il database di produzione. */
    private static String connectionUrl = "jdbc:sqlite:brewhub.db";

    /**
     * Configura il DatabaseManager per utilizzare un database specifico (es. per i test).
     * 
     * @param dbPath Il percorso del file del database da utilizzare.
     */
    public static void configureTestDatabase(String dbPath) {
        connectionUrl = "jdbc:sqlite:" + dbPath;
    }

    /**
     * Ottiene una nuova connessione al database.
     * 
     * @return Un oggetto {@link Connection} attivo.
     * @throws SQLException Se si verifica un errore durante la connessione.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl);
    }

    /**
     * Inizializza lo schema del database.
     * <p>
     * Crea tutte le tabelle necessarie (utenti, post, commenti, ecc.) se non esistono già.
     * Abilita inoltre il supporto per le chiavi esterne (Foreign Keys) in SQLite.
     * </p>
     */
    public static void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Abilita il supporto alle Foreign Keys
            stmt.execute("PRAGMA foreign_keys = ON");

            // Tabella Utenti
            String sqlUtenti = "CREATE TABLE IF NOT EXISTS utenti (" +
                    "username TEXT PRIMARY KEY, " +
                    "nome TEXT NOT NULL, " +
                    "cognome TEXT NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "tipo TEXT NOT NULL, " +
                    "foto_uri TEXT)";
            stmt.execute(sqlUtenti);

            // Tabella Post
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

            // Tabella Commenti
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

            // Tabella Likes
            String sqlLikes = "CREATE TABLE IF NOT EXISTS likes (" +
                    "post_id INTEGER NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "PRIMARY KEY (post_id, username), " +
                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlLikes);

            // Tabella Followers
            String sqlFollowers = "CREATE TABLE IF NOT EXISTS followers (" +
                    "follower_username TEXT NOT NULL, " +
                    "followed_username TEXT NOT NULL, " +
                    "PRIMARY KEY (follower_username, followed_username), " +
                    "FOREIGN KEY(follower_username) REFERENCES utenti(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(followed_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlFollowers);

            // Tabella Post Salvati (Archivio)
            String sqlSaved = "CREATE TABLE IF NOT EXISTS saved_posts (" +
                    "username TEXT NOT NULL, " +
                    "post_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (username, post_id), " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlSaved);

            // Tabella Notifiche
            String sqlNotifiche = "CREATE TABLE IF NOT EXISTS notifiche (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "utente_username TEXT NOT NULL, " +
                    "messaggio TEXT NOT NULL, " +
                    "letto BOOLEAN DEFAULT 0, " +
                    "data_creazione TEXT NOT NULL, " +
                    "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlNotifiche);

            // Tabella Categorie
            String sqlCategorie = "CREATE TABLE IF NOT EXISTS categorie (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL UNIQUE, " +
                    "icona TEXT" +
                    ")";
            stmt.execute(sqlCategorie);

            // Migrazioni schema: Aggiunta colonne se mancanti
            try {
                stmt.execute("ALTER TABLE post ADD COLUMN category_id INTEGER REFERENCES categorie(id) ON DELETE SET NULL");
            } catch (SQLException e) {
                // Colonna già esistente
            }
            try {
                stmt.execute("ALTER TABLE categorie ADD COLUMN icona TEXT");
            } catch (SQLException e) {
                // Colonna già esistente
            }

            // Popolamento dati iniziali Categorie
            try (java.sql.ResultSet rsCat = stmt.executeQuery("SELECT COUNT(*) FROM categorie")) {
                if (rsCat.next() && rsCat.getInt(1) == 0) {
                    stmt.execute("INSERT INTO categorie(nome) VALUES('Torrefattori')");
                    stmt.execute("INSERT INTO categorie(nome) VALUES('Miscele')");
                    stmt.execute("INSERT INTO categorie(nome) VALUES('Eventi')");
                }
            }

            // Indici per performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_post_autore ON post(autore_username)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_commenti_post ON commenti(post_id)");

            Log.info("Database inizializzato correttamente");

            // Tabella Dettagli Torrefattore
            String sqlTorrefattori = "CREATE TABLE IF NOT EXISTS torrefattori (" +
                    "username TEXT PRIMARY KEY, " +
                    "nome_azienda TEXT, " +
                    "partita_iva TEXT, " +
                    "indirizzo TEXT, " +
                    "descrizione TEXT, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlTorrefattori);

            // Tabella Messaggi (Chat)
            String sqlMessaggi = "CREATE TABLE IF NOT EXISTS messaggi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender TEXT NOT NULL, " +
                    "receiver TEXT, " +
                    "id_gruppo INTEGER, " +
                    "contenuto TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "letto BOOLEAN DEFAULT 0, " +
                    "FOREIGN KEY(sender) REFERENCES utenti(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(receiver) REFERENCES utenti(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlMessaggi);

            // Tabella Gruppi
            String sqlGruppi = "CREATE TABLE IF NOT EXISTS gruppi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "creatore TEXT NOT NULL, " +
                    "FOREIGN KEY(creatore) REFERENCES utenti(username) ON DELETE SET NULL" +
                    ")";
            stmt.execute(sqlGruppi);

            // Tabella Membri Gruppo
            String sqlMembriGruppo = "CREATE TABLE IF NOT EXISTS membri_gruppo (" +
                    "id_gruppo INTEGER NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "PRIMARY KEY (id_gruppo, username), " +
                    "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlMembriGruppo);

            // Migrazione Torrefattori
            try {
                stmt.execute("ALTER TABLE torrefattori ADD COLUMN nome_azienda TEXT");
            } catch (SQLException e) {
            }

            // Tabella Eventi
            String sqlEventi = "CREATE TABLE IF NOT EXISTS eventi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "descrizione TEXT, " +
                    "data TEXT NOT NULL, " +
                    "luogo TEXT NOT NULL, " +
                    "organizzatore TEXT NOT NULL, " +
                    "FOREIGN KEY(organizzatore) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlEventi);

            // Tabella Partecipazioni Eventi
            String sqlPartecipazioni = "CREATE TABLE IF NOT EXISTS partecipazioni (" +
                    "evento_id INTEGER NOT NULL, " +
                    "utente_username TEXT NOT NULL, " +
                    "PRIMARY KEY (evento_id, utente_username), " +
                    "FOREIGN KEY(evento_id) REFERENCES eventi(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlPartecipazioni);

            // Tabella Sfide
            String sqlSfide = "CREATE TABLE IF NOT EXISTS sfide (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "titolo TEXT NOT NULL, " +
                    "descrizione TEXT, " +
                    "premio TEXT, " +
                    "scadenza TEXT NOT NULL, " +
                    "creatore TEXT NOT NULL, " +
                    "partecipanti_count INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(creatore) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlSfide);

            // Tabella Partecipazioni Sfide
            String sqlPartecipazioniSfide = "CREATE TABLE IF NOT EXISTS partecipazioni_sfide (" +
                    "sfida_id INTEGER NOT NULL, " +
                    "utente_username TEXT NOT NULL, " +
                    "PRIMARY KEY (sfida_id, utente_username), " +
                    "FOREIGN KEY(sfida_id) REFERENCES sfide(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlPartecipazioniSfide);

            // Tabella Recensioni
            String sqlRecensioni = "CREATE TABLE IF NOT EXISTS recensioni (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "post_id INTEGER NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "voto INTEGER NOT NULL CHECK(voto >= 1 AND voto <= 5), " +
                    "testo TEXT, " +
                    "data_creazione TEXT NOT NULL, " +
                    "UNIQUE(post_id, username), " +
                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlRecensioni);

        } catch (SQLException e) {
            System.err.println("Errore inizializzazione DB: " + e.getMessage());
        }

        // Verifica ed esegue migrazioni complesse
        migrateMessaggiTableIfRequired();
    }

    /**
     * Controlla se la tabella 'messaggi' necessita di migrazione per correggere vincoli NOT NULL.
     * Esegue una migrazione sicura tramite tabella temporanea se necessario.
     */
    private static void migrateMessaggiTableIfRequired() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            boolean needsMigration = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("PRAGMA table_info(messaggi)")) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    if ("receiver".equalsIgnoreCase(name)) {
                        if (rs.getInt("notnull") == 1) {
                            needsMigration = true;
                        }
                        break;
                    }
                }
            }

            if (needsMigration) {
                Log.info("Starting schema migration for 'messaggi' table...");
                conn.setAutoCommit(false);
                try {
                    stmt.execute("PRAGMA foreign_keys=OFF");
                    stmt.execute("ALTER TABLE messaggi RENAME TO messaggi_old");

                    String sqlMessaggi = "CREATE TABLE messaggi (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "sender TEXT NOT NULL, " +
                            "receiver TEXT, " +
                            "id_gruppo INTEGER, " +
                            "contenuto TEXT NOT NULL, " +
                            "timestamp TEXT NOT NULL, " +
                            "letto BOOLEAN DEFAULT 0, " +
                            "FOREIGN KEY(sender) REFERENCES utenti(username) ON DELETE CASCADE, " +
                            "FOREIGN KEY(receiver) REFERENCES utenti(username) ON DELETE CASCADE, " +
                            "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE" +
                            ")";
                    stmt.execute(sqlMessaggi);

                    stmt.execute("INSERT INTO messaggi (id, sender, receiver, id_gruppo, contenuto, timestamp, letto) " +
                            "SELECT id, sender, receiver, id_gruppo, contenuto, timestamp, letto FROM messaggi_old");

                    stmt.execute("DROP TABLE messaggi_old");
                    stmt.execute("PRAGMA foreign_keys=ON");
                    conn.commit();
                    Log.info("Schema migration for 'messaggi' completed successfully.");
                } catch (Exception e) {
                    conn.rollback();
                    Log.error("Migration failed, rolling back.", e);
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            Log.error("Error checking/migrating schema", e);
        }
    }

    /**
     * Esegue il backup del database corrente.
     * <p>Utilizza il comando SQLite `VACUUM INTO` per creare una copia sicura.</p>
     * 
     * @param destinationFile Il file di destinazione per il backup.
     * @throws SQLException In caso di errori SQL.
     */
    public static void backup(java.io.File destinationFile) throws SQLException {
        String destPath = destinationFile.getAbsolutePath();
        destPath = destPath.replace("'", "''"); // Escape semplice per SQL string
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("VACUUM INTO '" + destPath + "'");
        }
    }

    /**
     * Ripristina il database da un file di backup.
     * <p>Sovrascrive il file database attuale.</p>
     * 
     * @param backupFile Il file di backup da ripristinare.
     * @throws java.io.IOException In caso di errori di I/O (es. file in uso).
     */
    public static void restore(java.io.File backupFile) throws java.io.IOException {
        java.io.File dbFile = new java.io.File("brewhub.db");
        java.nio.file.Files.copy(backupFile.toPath(), dbFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
