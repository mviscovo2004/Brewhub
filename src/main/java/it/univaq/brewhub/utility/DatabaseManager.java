package it.univaq.brewhub.utility;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Gestore centralizzato per la connessione e la configurazione del database
 * SQLite.
 * Gestisce il pool di connessioni, l'inizializzazione dello schema (DDL) e
 * le migrazioni automatiche per correggere eventuali incongruenze strutturali.
 */
public class DatabaseManager {

    private static String connectionUrl = "jdbc:sqlite:brewhub.db?foreign_keys=on";
    private static HikariDataSource dataSource;

    /**
     * Configura il gestore per utilizzare un database di test specifico.
     * Chiude eventuali connessioni attive prima di riconfigurare.
     *
     * @param dbPath Il percorso del file database di test.
     */
    public static void configureTestDatabase(String dbPath) {
        connectionUrl = "jdbc:sqlite:" + dbPath + "?foreign_keys=on";
        shutdown();
    }

    /**
     * Chiude il pool di connessioni (DataSource) e rilascia le risorse.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    /**
     * Configura e restituisce il DataSource HikariCP.
     * Implementa il pattern Singleton per il DataSource.
     *
     * @return L'istanza di HikariDataSource configurata.
     */
    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(connectionUrl);
            config.setDriverClassName("org.sqlite.JDBC");

            // Impostazioni HikariCP ottimizzate per SQLite
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000); // 30 secondi
            config.setPoolName("BrewHubPool");

            // Ottimizzazioni specifiche per SQLite
            config.addDataSourceProperty("journal_mode", "WAL"); // Write-Ahead Logging
            config.addDataSourceProperty("synchronous", "NORMAL");

            // Abilita i vincoli di chiave esterna per ogni nuova connessione
            config.setConnectionInitSql("PRAGMA foreign_keys = ON;");

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    /**
     * Ottiene una connessione attiva dal pool.
     *
     * @return Un oggetto Connection.
     * @throws SQLException Se non è possibile ottenere una connessione.
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Inizializza il database creando tutte le tabelle necessarie se non esistono.
     * Esegue inoltre eventuali script di migrazione per aggiornare lo schema.
     */
    public static void init() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

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
                    "FOREIGN KEY(autore_username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
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
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlCommenti);

            // Tabella Likes
            String sqlLikes = "CREATE TABLE IF NOT EXISTS likes (" +
                    "post_id INTEGER NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "PRIMARY KEY (post_id, username), " +
                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlLikes);

            // Tabella Followers
            String sqlFollowers = "CREATE TABLE IF NOT EXISTS followers (" +
                    "follower_username TEXT NOT NULL, " +
                    "followed_username TEXT NOT NULL, " +
                    "PRIMARY KEY (follower_username, followed_username), " +
                    "FOREIGN KEY(follower_username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY(followed_username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlFollowers);

            // Tabella Post Salvati (Archivio)
            String sqlSaved = "CREATE TABLE IF NOT EXISTS saved_posts (" +
                    "username TEXT NOT NULL, " +
                    "post_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (username, post_id), " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE, " +
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
                    "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlNotifiche);

            // Tabella Categorie
            String sqlCategorie = "CREATE TABLE IF NOT EXISTS categorie (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL UNIQUE, " +
                    "icona TEXT" +
                    ")";
            stmt.execute(sqlCategorie);

            // Aggiornamento schema per colonne aggiunte successivamente
            try {
                stmt.execute(
                        "ALTER TABLE post ADD COLUMN category_id INTEGER REFERENCES categorie(id) ON DELETE SET NULL");
            } catch (SQLException e) {
                // Ignora se la colonna esiste già
            }
            try {
                stmt.execute("ALTER TABLE categorie ADD COLUMN icona TEXT");
            } catch (SQLException e) {
                // Ignora se la colonna esiste già
            }

            // Popolamento iniziale categorie
            stmt.execute("DELETE FROM categorie WHERE nome = 'Eventi'");
            stmt.execute("INSERT OR IGNORE INTO categorie(nome, icona) VALUES('Torrefattori', '☕')");
            stmt.execute("INSERT OR IGNORE INTO categorie(nome, icona) VALUES('Miscele', '☕')");
            stmt.execute("INSERT OR IGNORE INTO categorie(nome, icona) VALUES('Guide', '📖')");

            // Indici per ottimizzazione performance
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
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlTorrefattori);

            // Tabella Messaggi (Chat interna)
            String sqlMessaggi = "CREATE TABLE IF NOT EXISTS messaggi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender TEXT NOT NULL, " +
                    "receiver TEXT, " +
                    "id_gruppo INTEGER, " +
                    "contenuto TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "letto BOOLEAN DEFAULT 0, " +
                    "FOREIGN KEY(sender) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY(receiver) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlMessaggi);

            // Tabella Gruppi
            String sqlGruppi = "CREATE TABLE IF NOT EXISTS gruppi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "creatore TEXT NOT NULL, " +
                    "FOREIGN KEY(creatore) REFERENCES utenti(username) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlGruppi);

            // Tabella Membri Gruppo
            String sqlMembriGruppo = "CREATE TABLE IF NOT EXISTS membri_gruppo (" +
                    "id_gruppo INTEGER NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "PRIMARY KEY (id_gruppo, username), " +
                    "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlMembriGruppo);

            // Migrazione colonna nome_azienda per torrefattori
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
                    "FOREIGN KEY(organizzatore) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlEventi);

            // Tabella Partecipazioni Eventi
            String sqlPartecipazioni = "CREATE TABLE IF NOT EXISTS partecipazioni (" +
                    "evento_id INTEGER NOT NULL, " +
                    "utente_username TEXT NOT NULL, " +
                    "PRIMARY KEY (evento_id, utente_username), " +
                    "FOREIGN KEY(evento_id) REFERENCES eventi(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
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
                    "FOREIGN KEY(creatore) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlSfide);

            // Tabella Partecipazioni Sfide
            String sqlPartecipazioniSfide = "CREATE TABLE IF NOT EXISTS partecipazioni_sfide (" +
                    "sfida_id INTEGER NOT NULL, " +
                    "utente_username TEXT NOT NULL, " +
                    "PRIMARY KEY (sfida_id, utente_username), " +
                    "FOREIGN KEY(sfida_id) REFERENCES sfide(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(utente_username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
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
                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ")";
            stmt.execute(sqlRecensioni);

        } catch (SQLException e) {
            System.err.println("Errore inizializzazione DB: " + e.getMessage());
        }

        // Esegue i controlli e le migrazioni per lo schema
        migrateMessaggiTableIfRequired();
        migrateTablesReferencingWrongTableName();
    }

    /**
     * Verifica e corregge i riferimenti stranieri errati alla tabella 'posts'
     * (plurale)
     * invece di 'post' (singolare) nelle tabelle collegate.
     */
    private static void migrateTablesReferencingWrongTableName() {
        String[] tablesToCheck = { "commenti", "likes", "saved_posts" };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String tableName : tablesToCheck) {
                boolean needsFix = false;
                try (ResultSet rs = stmt.executeQuery("PRAGMA foreign_key_list(" + tableName + ")")) {
                    while (rs.next()) {
                        String refTable = rs.getString("table");
                        if ("posts".equalsIgnoreCase(refTable)) {
                            needsFix = true;
                            break;
                        }
                    }
                }

                if (needsFix) {
                    Log.info(
                            "Correzione schema: " + tableName + " referenzia 'posts', migrazione a 'post' in corso...");
                    conn.setAutoCommit(false);
                    try {
                        stmt.execute("PRAGMA foreign_keys=OFF");
                        stmt.execute("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old");

                        // Ricrea la tabella con la ForeignKey corretta
                        String createSql = "";
                        if ("commenti".equals(tableName)) {
                            createSql = "CREATE TABLE " + tableName + " (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "post_id INTEGER NOT NULL, " +
                                    "username TEXT, " +
                                    "contenuto TEXT NOT NULL, " +
                                    "data_creazione TEXT NOT NULL, " +
                                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE SET NULL ON UPDATE CASCADE"
                                    +
                                    ")";
                        } else if ("likes".equals(tableName)) {
                            createSql = "CREATE TABLE " + tableName + " (" +
                                    "post_id INTEGER NOT NULL, " +
                                    "username TEXT NOT NULL, " +
                                    "PRIMARY KEY (post_id, username), " +
                                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE"
                                    +
                                    ")";
                        } else if ("saved_posts".equals(tableName)) {
                            createSql = "CREATE TABLE " + tableName + " (" +
                                    "username TEXT NOT NULL, " +
                                    "post_id INTEGER NOT NULL, " +
                                    "PRIMARY KEY (username, post_id), " +
                                    "FOREIGN KEY(username) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE, "
                                    +
                                    "FOREIGN KEY(post_id) REFERENCES post(id) ON DELETE CASCADE" +
                                    ")";
                        }

                        stmt.execute(createSql);
                        stmt.execute("INSERT INTO " + tableName + " SELECT * FROM " + tableName + "_old");
                        stmt.execute("DROP TABLE " + tableName + "_old");
                        stmt.execute("PRAGMA foreign_keys=ON");
                        conn.commit();
                        Log.info("Migrazione schema per '" + tableName + "' completata.");
                    } catch (Exception e) {
                        conn.rollback();
                        Log.error("Migrazione fallita per " + tableName, e);
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            }
        } catch (SQLException e) {
            Log.error("Errore durante la verifica dei nomi tabella errati", e);
        }
    }

    /**
     * Controlla se la tabella 'messaggi' necessita di essere migrata per rimuovere
     * eventuali vincoli NOT NULL non più desiderati o per aggiornamenti di schema.
     * Utilizza una tabella temporanea per preservare i dati.
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
                Log.info("Avvio migrazione schema per la tabella 'messaggi'...");
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
                            "FOREIGN KEY(sender) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE, " +
                            "FOREIGN KEY(receiver) REFERENCES utenti(username) ON DELETE CASCADE ON UPDATE CASCADE, " +
                            "FOREIGN KEY(id_gruppo) REFERENCES gruppi(id) ON DELETE CASCADE" +
                            ")";
                    stmt.execute(sqlMessaggi);

                    stmt.execute("INSERT INTO messaggi (id, sender, receiver, id_gruppo, contenuto, timestamp, letto) "
                            +
                            "SELECT id, sender, receiver, id_gruppo, contenuto, timestamp, letto FROM messaggi_old");

                    stmt.execute("DROP TABLE messaggi_old");
                    stmt.execute("PRAGMA foreign_keys=ON");
                    conn.commit();
                    Log.info("Migrazione schema per 'messaggi' completata con successo.");
                } catch (Exception e) {
                    conn.rollback();
                    Log.error("Migrazione fallita, rollback eseguito.", e);
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            Log.error("Errore durante la verifica/migrazione dello schema", e);
        }
    }

    /**
     * Esegue un backup completo del database in un file specificato.
     * Utilizza la funzionalità VACUUM INTO di SQLite.
     *
     * @param destinationFile Il file in cui salvare il backup.
     * @throws SQLException Se si verifica un errore durante l'operazione.
     */
    public static void backup(java.io.File destinationFile) throws SQLException {
        String destPath = destinationFile.getAbsolutePath();
        destPath = destPath.replace("'", "''"); // Escape degli apici nel path
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("VACUUM INTO '" + destPath + "'");
        }
    }

    /**
     * Ripristina il database da un file di backup, sovrascrivendo quello attuale.
     *
     * @param backupFile Il file di backup da utilizzare.
     * @throws java.io.IOException Se si verificano errori di I/O (es. permessi).
     */
    public static void restore(java.io.File backupFile) throws java.io.IOException {
        java.io.File dbFile = new java.io.File("brewhub.db");
        java.nio.file.Files.copy(backupFile.toPath(), dbFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
