package it.univaq.brewhub;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import it.univaq.brewhub.model.Utente.TipoUtente;
import java.io.File;
import java.sql.SQLException;
import java.util.UUID;
import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.*;

/**
 * Classe base per tutti i test di integrazione che richiedono un database.
 *
 * Gestisce la creazione di un database SQLite temporaneo e univoco per ogni
 * test,
 * l'inizializzazione dei DAO e la pulizia (cancellazione del file DB) al
 * termine di ogni test.
 *
 */
public abstract class BaseTest {

    protected String testDbName;
    protected it.univaq.brewhub.dao.impl.UtenteDAOImpl utenteDAO;
    protected it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl torrefattoreDAO;
    protected it.univaq.brewhub.dao.impl.PostDAOImpl postDAO;
    protected it.univaq.brewhub.dao.impl.CommentoDAOImpl commentoDAO;
    protected it.univaq.brewhub.dao.impl.GruppoDAOImpl gruppoDAO;
    protected it.univaq.brewhub.dao.impl.MessaggioDAOImpl messaggioDAO;
    protected it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO;
    protected it.univaq.brewhub.dao.impl.EventoDAOImpl eventoDAO;
    protected it.univaq.brewhub.dao.impl.SfidaDAOImpl sfidaDAO;
    protected it.univaq.brewhub.dao.impl.RecensioneDAOImpl recensioneDAO;
    protected it.univaq.brewhub.dao.impl.CategoriaDAOImpl categoriaDAO;

    /**
     * Configurazione eseguita prima di ogni metodo di test.
     *
     * Crea un nome DB univoco, configura il DatabaseManager e istanzia tutti i DAO.
     *
     * 
     * @throws SQLException Se l'inizializzazione del DB fallisce.
     */
    @BeforeEach
    public void baseSetUp() throws SQLException {
        File testDir = new File("test_dbs");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        testDbName = "test_dbs/test_" + UUID.randomUUID().toString() + ".db";
        DatabaseManager.configureTestDatabase(testDbName);
        DatabaseManager.init();

        // DAO initialization (remains same)
        utenteDAO = new it.univaq.brewhub.dao.impl.UtenteDAOImpl();
        torrefattoreDAO = new it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl();
        postDAO = new it.univaq.brewhub.dao.impl.PostDAOImpl();
        commentoDAO = new it.univaq.brewhub.dao.impl.CommentoDAOImpl();
        gruppoDAO = new it.univaq.brewhub.dao.impl.GruppoDAOImpl();
        messaggioDAO = new it.univaq.brewhub.dao.impl.MessaggioDAOImpl();
        notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();
        eventoDAO = new it.univaq.brewhub.dao.impl.EventoDAOImpl();
        sfidaDAO = new it.univaq.brewhub.dao.impl.SfidaDAOImpl();
        recensioneDAO = new it.univaq.brewhub.dao.impl.RecensioneDAOImpl();
        categoriaDAO = new it.univaq.brewhub.dao.impl.CategoriaDAOImpl();
    }

    /**
     * Helper per creare e persistere un utente di test.
     * 
     * @param username Username dell'utente.
     * @param tipo     Tipo di utente.
     * @return L'oggetto Utente creato.
     */
    protected Utente createTestUser(String username, TipoUtente tipo) {
        Utente u = new Utente("Test", "User", username, "password", tipo, null);
        try {
            utenteDAO.create(u);
        } catch (SQLException e) {
        }
        return u;
    }

    /**
     * Helper per creare e persistere un torrefattore di test.
     * 
     * @param username Username del torrefattore.
     * @return L'oggetto Torrefattore creato.
     */
    protected Torrefattore createTestTorrefattore(String username) {
        Torrefattore t = new Torrefattore("Torre", "Fattore", username, "password", null, "12345678901", "Via Roma",
                "Desc", "Azienda Bio");
        try {
            torrefattoreDAO.create(t);
        } catch (SQLException e) {
        }
        return t;
    }

    /**
     * Helper per creare e persistere un post di test.
     * 
     * @param titolo Titolo del post.
     * @param autore Autore del post.
     * @return L'oggetto Post creato.
     */
    protected Post createTestPost(String titolo, Utente autore) {
        Post p = new Post(titolo, "Contenuto test", autore, Post.TipoPost.TESTO, null);
        try {
            postDAO.create(p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    /**
     * Helper per creare e persistere una categoria di test.
     * 
     * @param nome Nome della categoria.
     * @return L'oggetto Categoria creato.
     */
    protected Categoria createTestCategoria(String nome) {
        Categoria c = new Categoria(nome);
        try {
            categoriaDAO.create(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * Helper per creare e persistere un evento di test.
     * 
     * @param titolo        Titolo dell'evento.
     * @param organizzatore Username dell'organizzatore.
     * @return L'oggetto Evento creato.
     */
    protected Evento createTestEvento(String titolo, String organizzatore) {
        Evento e = new Evento();
        e.setNome(titolo);
        e.setDescrizione("Descrizione test");
        e.setData(getCurrentDate());
        e.setLuogo("Luogo test");
        e.setOrganizzatore(organizzatore);
        try {
            eventoDAO.create(e);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return e;
    }

    /**
     * Helper per creare e persistere un gruppo di test.
     * 
     * @param nome     Nome del gruppo.
     * @param creatore Username del creatore.
     * @return L'oggetto Gruppo creato.
     */
    protected Gruppo createTestGruppo(String nome, String creatore) {
        Gruppo g = new Gruppo();
        g.setNome(nome);
        g.setCreatore(creatore);
        try {
            gruppoDAO.createGruppo(nome, creatore, java.util.List.of());
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return g;
    }

    /**
     * Helper per creare e persistere una recensione di test.
     * 
     * @param post   Post da recensire.
     * @param autore Autore della recensione.
     * @param voto   Voto (1-5).
     * @return L'oggetto Recensione creato.
     */
    protected it.univaq.brewhub.model.Recensione createTestRecensione(Post post, Utente autore, int voto) {
        it.univaq.brewhub.model.Recensione r = new it.univaq.brewhub.model.Recensione(
                post, autore, voto, "Recensione test", getCurrentDate());
        try {
            recensioneDAO.create(r);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * Ottiene la data corrente in formato stringa (YYYY-MM-DD).
     * 
     * @return La data corrente.
     */
    protected String getCurrentDate() {
        return java.time.LocalDate.now().toString();
    }

    /**
     * Pulizia eseguita dopo ogni metodo di test.
     *
     * Tenta di cancellare il file del database di test. Include un meccanismo di
     * retry per aggirare i lock sui file di Windows.
     *
     */
    @AfterEach
    public void baseTearDown() {
        if (testDbName != null) {
            // Chiude il pool di connessioni e rilascia i lock sui file
            DatabaseManager.shutdown();

            File dbFile = new File(testDbName);
            File shmFile = new File(testDbName + "-shm");
            File walFile = new File(testDbName + "-wal");

            System.gc(); // Primo tentativo di rilascio risorse

            deleteFileWithRetry(dbFile);
            deleteFileWithRetry(shmFile);
            deleteFileWithRetry(walFile);
        }
    }

    /**
     * Tenta di eliminare un file con un meccanismo di retry per gestire i lock di
     * Windows.
     * 
     * @param file Il file da eliminare.
     */
    private void deleteFileWithRetry(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        boolean deleted = false;
        for (int i = 0; i < 30 && !deleted; i++) {
            try {
                java.nio.file.Files.deleteIfExists(file.toPath());
                deleted = true;
            } catch (java.io.IOException e) {
                if (i < 29) {
                    try {
                        Thread.sleep(100);
                        System.gc();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        if (!deleted) {
            System.err.println("WARNING: Failed to delete test file: " + file.getAbsolutePath());
            file.deleteOnExit();
        }
    }
}
