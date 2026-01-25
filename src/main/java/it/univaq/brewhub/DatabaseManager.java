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
        // Il file del DB verrà creato nella cartella del progetto
        /** URL di connessione JDBC per SQLite. */
        private static String connectionUrl = "jdbc:sqlite:brewhub.db";

        /**
         * Configura il database manager per usare un database di test.
         * 
         * @param dbPath Percorso del file db di test
         */
        public static void configureTestDatabase(String dbPath) {
                connectionUrl = "jdbc:sqlite:" + dbPath;
        }

        /**
         * Ottiene una connessione attiva al database SQLite.
         * 
         * @return Connection oggetto connessione JDBC.
         * @throws SQLException In caso di errore di connessione.
         */
        public static Connection getConnection() throws SQLException {
                return DriverManager.getConnection(connectionUrl);
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

                        // Creazione tabella Followers
                        String sqlFollowers = "CREATE TABLE IF NOT EXISTS followers (" +
                                        "follower_username TEXT NOT NULL, " +
                                        "followed_username TEXT NOT NULL, " +
                                        "PRIMARY KEY (follower_username, followed_username), " +
                                        "FOREIGN KEY(follower_username) REFERENCES utenti(username) ON DELETE CASCADE, "
                                        +
                                        "FOREIGN KEY(followed_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlFollowers);

                        // Creazione tabella Post Salvati (Archivio)
                        String sqlSaved = "CREATE TABLE IF NOT EXISTS saved_posts (" +
                                        "username TEXT NOT NULL, " +
                                        "post_id INTEGER NOT NULL, " +
                                        "PRIMARY KEY (username, post_id), " +
                                        "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE, " +
                                        "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlSaved);

                        // Creazione tabella Notifiche
                        String sqlNotifiche = "CREATE TABLE IF NOT EXISTS notifiche (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "utente_username TEXT NOT NULL, " +
                                        "messaggio TEXT NOT NULL, " +
                                        "letto BOOLEAN DEFAULT 0, " +
                                        "data_creazione TEXT NOT NULL, " +
                                        "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlNotifiche);

                        // Creazione tabella Categorie
                        String sqlCategorie = "CREATE TABLE IF NOT EXISTS categorie (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "nome TEXT NOT NULL UNIQUE, " +
                                        "icona TEXT" +
                                        ")";
                        stmt.execute(sqlCategorie);

                        // Alter table post per aggiungere category_id se non esiste (SQLite non ha IF
                        // NOT EXISTS per column add,
                        // quindi usiamo try-catch o controlliamo pragma, ma per semplicità qui facciamo
                        // try-catch mirato per l'alter)
                        try {
                                stmt.execute("ALTER TABLE post ADD COLUMN category_id INTEGER REFERENCES categorie(id) ON DELETE SET NULL");
                        } catch (SQLException e) {
                                // Colonna probabilmente già esistente
                        }

                        try {
                                stmt.execute("ALTER TABLE categorie ADD COLUMN icona TEXT");
                        } catch (SQLException e) {
                                // Colonna probabilmente già esistente
                        }

                        // Popolamento categorie default se vuota
                        try (java.sql.ResultSet rsCat = stmt.executeQuery("SELECT COUNT(*) FROM categorie")) {
                                if (rsCat.next() && rsCat.getInt(1) == 0) {
                                        stmt.execute("INSERT INTO categorie(nome) VALUES('Torrefattori')");
                                        stmt.execute("INSERT INTO categorie(nome) VALUES('Miscele')");
                                        stmt.execute("INSERT INTO categorie(nome) VALUES('Eventi')");
                                }
                        }

                        // Indici per ottimizzare le query frequenti
                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_post_autore ON post(autore_username)");
                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_commenti_post ON commenti(post_id)");

                        // Log di successo
                        Log.info("Database inizializzato correttamente");

                        // Creazione tabella Dettagli Torrefattore
                        String sqlTorrefattori = "CREATE TABLE IF NOT EXISTS torrefattori (" +
                                        "username TEXT PRIMARY KEY, " +
                                        "nome_azienda TEXT, " +
                                        "partita_iva TEXT, " +
                                        "indirizzo TEXT, " +
                                        "descrizione TEXT, " +
                                        "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlTorrefattori);

                        // Creazione tabella Messaggi
                        String sqlMessaggi = "CREATE TABLE IF NOT EXISTS messaggi (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "sender TEXT NOT NULL, " +
                                        "receiver TEXT, " + // Receiver can be null if it's a group message (or we use
                                                            // it for consistency)
                                        "id_gruppo INTEGER, " + // Nullable, for group messages
                                        "contenuto TEXT NOT NULL, " +
                                        "timestamp TEXT NOT NULL, " +
                                        "letto BOOLEAN DEFAULT 0, " +
                                        "FOREIGN KEY(sender) REFERENCES utenti(username) ON DELETE CASCADE, " +
                                        "FOREIGN KEY(receiver) REFERENCES utenti(username) ON DELETE CASCADE, " +
                                        "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlMessaggi);

                        // Creazione tabella Gruppi
                        String sqlGruppi = "CREATE TABLE IF NOT EXISTS gruppi (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "nome TEXT NOT NULL, " +
                                        "creatore TEXT NOT NULL, " +
                                        "FOREIGN KEY(creatore) REFERENCES utenti(username) ON DELETE SET NULL" +
                                        ")";
                        stmt.execute(sqlGruppi);

                        // Creazione tabella Membri Gruppo
                        String sqlMembriGruppo = "CREATE TABLE IF NOT EXISTS membri_gruppo (" +
                                        "id_gruppo INTEGER NOT NULL, " +
                                        "username TEXT NOT NULL, " +
                                        "PRIMARY KEY (id_gruppo, username), " +
                                        "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE, " +
                                        "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlMembriGruppo);

                        // Alter table per nome_azienda se non esiste (migrazione)
                        try {
                                stmt.execute("ALTER TABLE torrefattori ADD COLUMN nome_azienda TEXT");
                        } catch (SQLException e) {
                                // Colonna probabilmente già esistente
                        }

                        // Creazione tabella Eventi
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

                        // Creazione tabella Partecipazioni
                        String sqlPartecipazioni = "CREATE TABLE IF NOT EXISTS partecipazioni (" +
                                        "evento_id INTEGER NOT NULL, " +
                                        "utente_username TEXT NOT NULL, " +
                                        "PRIMARY KEY (evento_id, utente_username), " +
                                        "FOREIGN KEY(evento_id) REFERENCES eventi(id) ON DELETE CASCADE, " +
                                        "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlPartecipazioni);

                        // Creazione tabella Sfide
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

                        // Creazione tabella Partecipazioni Sfide
                        String sqlPartecipazioniSfide = "CREATE TABLE IF NOT EXISTS partecipazioni_sfide (" +
                                        "sfida_id INTEGER NOT NULL, " +
                                        "utente_username TEXT NOT NULL, " +
                                        "PRIMARY KEY (sfida_id, utente_username), " +
                                        "FOREIGN KEY(sfida_id) REFERENCES sfide(id) ON DELETE CASCADE, " +
                                        "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE" +
                                        ")";
                        stmt.execute(sqlPartecipazioniSfide);

                } catch (SQLException e) {
                        // Gestione errore inizializzazione DB
                        System.err.println("Errore inizializzazione DB: " + e.getMessage());
                }

                // Schema Migration Check for 'messaggi.receiver' NOT NULL constraint
                migrateMessaggiTableIfRequired();
        }

        private static void migrateMessaggiTableIfRequired() {
                try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                        boolean needsMigration = false;
                        try (java.sql.ResultSet rs = stmt.executeQuery("PRAGMA table_info(messaggi)")) {
                                while (rs.next()) {
                                        String name = rs.getString("name");
                                        if ("receiver".equalsIgnoreCase(name)) {
                                                // notnull: 1 if not null, 0 if nullable
                                                if (rs.getInt("notnull") == 1) {
                                                        needsMigration = true;
                                                }
                                                break;
                                        }
                                }
                        }

                        if (needsMigration) {
                                Log.info("Starting schema migration for 'messaggi' table...");
                                conn.setAutoCommit(false); // Transaction
                                try {
                                        stmt.execute("PRAGMA foreign_keys=OFF");

                                        // 1. Rename old table
                                        stmt.execute("ALTER TABLE messaggi RENAME TO messaggi_old");

                                        // 2. Create new table (Correct Schema)
                                        String sqlMessaggi = "CREATE TABLE messaggi (" +
                                                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                        "sender TEXT NOT NULL, " +
                                                        "receiver TEXT, " +
                                                        "id_gruppo INTEGER, " +
                                                        "contenuto TEXT NOT NULL, " +
                                                        "timestamp TEXT NOT NULL, " +
                                                        "letto BOOLEAN DEFAULT 0, " +
                                                        "FOREIGN KEY(sender) REFERENCES utenti(username) ON DELETE CASCADE, "
                                                        +
                                                        "FOREIGN KEY(receiver) REFERENCES utenti(username) ON DELETE CASCADE, "
                                                        +
                                                        "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE"
                                                        +
                                                        ")";
                                        stmt.execute(sqlMessaggi);

                                        // 3. Copy Data
                                        // We must be careful with columns if they match.
                                        // Assuming previous schema had id, sender, receiver, contenuto, timestamp,
                                        // letto, id_gruppo (if added)
                                        // If id_gruppo was missing in very old version, copy might fail?
                                        // But init() adds id_gruppo column via ALTER before this. So it should exist in
                                        // messaggi_old.

                                        stmt.execute("INSERT INTO messaggi (id, sender, receiver, id_gruppo, contenuto, timestamp, letto) "
                                                        +
                                                        "SELECT id, sender, receiver, id_gruppo, contenuto, timestamp, letto FROM messaggi_old");

                                        // 4. Drop old
                                        stmt.execute("DROP TABLE messaggi_old");

                                        stmt.execute("PRAGMA foreign_keys=ON");
                                        conn.commit();
                                        Log.info("Schema migration for 'messaggi' completed successfully.");
                                } catch (Exception e) {
                                        conn.rollback();
                                        Log.error("Migration failed, rolling back.", e);
                                        // If rollback, we might be in weird state.
                                } finally {
                                        conn.setAutoCommit(true);
                                }
                        }
                } catch (SQLException e) {
                        Log.error("Error checking/migrating schema", e);
                }
        }

        /**
         * Esegue il backup del database copiando il file .db nella destinazione
         * specificata.
         * Utilizza l'istruzione VACUUM INTO di SQLite per un backup sicuro a caldo.
         * 
         * @param destinationFile Il file di destinazione per il backup.
         * @throws SQLException In caso di errore SQL o di IO durante il backup.
         */
        public static void backup(java.io.File destinationFile) throws SQLException {
                String destPath = destinationFile.getAbsolutePath();
                // Escape path quote chars if needed, but PreparedStatement doesn't work well
                // with VACUUM INTO filename
                // SQLite strings are single-quoted. We should handle single quotes in path.
                // VACUUM INTO 'path/to/file'
                destPath = destPath.replace("'", "''");

                try (Connection conn = getConnection();
                                Statement stmt = conn.createStatement()) {
                        stmt.execute("VACUUM INTO '" + destPath + "'");
                }
        }

        /**
         * Ripristina il database da un file di backup.
         * ATTENZIONE: Sovrascrive il database corrente.
         * 
         * @param backupFile Il file di backup (.db) da ripristinare.
         * @throws java.io.IOException Se ci sono errori di IO (es. file lock).
         */
        public static void restore(java.io.File backupFile) throws java.io.IOException {
                java.io.File dbFile = new java.io.File("brewhub.db");
                // Tentiamo di sovrascrivere il file del database
                // Se il file è bloccato da connessioni aperte, questo potrebbe fallire su
                // Windows
                java.nio.file.Files.copy(backupFile.toPath(), dbFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
}