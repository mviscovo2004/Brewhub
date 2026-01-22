package it.univaq.brewhub;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;

/**
 * Classe di test per la gestione dei Post (Post.java).
 * Verifica il corretto funzionamento dei costruttori, dei metodi getter/setter
 * e delle interazioni con il database (CRUD).
 */
public class PostTest {

    /**
     * Test del costruttore per post di tipo TESTO.
     * Verifica che l'oggetto venga creato correttamente con tutti i parametri
     * passati.
     */
    @Test
    public void testCostruttoreTesto() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.TESTO;
        String media = null; // Nessun media per post di testo

        Post p = new Post(titolo, contenuto, u, tipo, media);

        // Asserzioni per verificare la corrispondenza dei campi
        assertEquals(titolo, p.getTitolo(), "Il titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Il contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertNull(p.getMedia(), "Il campo media del post deve essere null per tipo TESTO");
    }

    /**
     * Test del costruttore per post di tipo FOTO.
     * Verifica l'assegnazione corretta del percorso media.
     */
    @Test
    public void testCostruttoreFoto() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.FOTO;
        String foto = "media/foto.jpg";

        Post p = new Post(titolo, contenuto, u, tipo, foto);

        // Asserzioni standard
        assertEquals(titolo, p.getTitolo(), "Il titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Il contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertEquals(foto, p.getMedia(), "Il percorso della foto non corrisponde");
    }

    /**
     * Test del costruttore per post di tipo VIDEO.
     */
    @Test
    public void testCostruttoreVideo() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.VIDEO;
        String video = "media/video.mp4";

        Post p = new Post(titolo, contenuto, u, tipo, video);

        assertEquals(titolo, p.getTitolo(), "Il titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Il contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertEquals(video, p.getMedia(), "Il percorso del video non corrisponde");
    }

    /**
     * Test completo di tutti i metodi Setter e Getter.
     * Verifica che ogni proprietà possa essere impostata e letta correttamente.
     */
    @Test
    public void testSetterGetter() {
        Post p = new Post();

        // Titolo
        p.setTitolo("Test post");
        assertEquals("Test post", p.getTitolo());

        // Contenuto
        p.setContenuto("Contenuto test post");
        assertEquals("Contenuto test post", p.getContenuto());

        // Autore
        Utente u = new Utente();
        p.setAutore(u);
        assertEquals(u, p.getAutore());

        // Tipo
        p.setTipo(TipoPost.TESTO);
        assertEquals(TipoPost.TESTO, p.getTipo());

        // Media
        p.setMedia("media/foto.jpg");
        assertEquals("media/foto.jpg", p.getMedia());

        // Mi Piace (Lista)
        List<Utente> likes = new ArrayList<>();
        p.setMiPiace(likes);
        assertEquals(likes, p.getMiPiace());

        // Commenti (Lista)
        List<Commento> comments = new ArrayList<>();
        p.setCommenti(comments);
        assertEquals(comments, p.getCommenti());

        // Data Creazione
        LocalDateTime now = LocalDateTime.now();
        p.setDataCreazione(now);
        assertEquals(now, p.getDataCreazione());
    }

    /**
     * Test di integrazione con il Database.
     * Verifica il ciclo di vita di un Post:
     * 1. Lettura stato iniziale
     * 2. Salvataggio (INSERT)
     * 3. Verifica presenza (SELECT)
     * 4. Eliminazione (DELETE)
     * 5. Verifica pulizia
     * 
     * @throws SQLException in caso di errori di connessione o query.
     */
    @Test
    public void testMetodiDB() throws SQLException {
        PostDAOImpl postDAO = new PostDAOImpl();
        UtenteDAOImpl utenteDAO = new UtenteDAOImpl();

        // Setup autore fittizio per soddisfare i vincoli FK se presenti
        Utente autore = new Utente("NomeTest", "CognomeTest", "testUserPost", "password",
                Utente.TipoUtente.APPASSIONATO, null);
        try {
            utenteDAO.create(autore);
        } catch (SQLException e) {
            // Ignora se già esiste
        }

        // Creazione oggetto Post da testare
        Post p = new Post();
        p.setAutore(autore);
        p.setTitolo("Titolo Test DB");
        p.setContenuto("Contenuto Test DB");
        p.setTipo(TipoPost.TESTO);
        p.setDataCreazione(LocalDateTime.now()); // Data attuale

        // 1. Setup preliminare: conta quanti post esistono
        int initialSize = postDAO.findAll().size();

        // 2. Azione: Salva il post nel database
        postDAO.create(p);

        // 3. Verifica: Controlla che il numero totale di post sia aumentato di 1
        List<Post> post = postDAO.findAll();
        assertEquals(initialSize + 1, post.size(), "Il numero di post dovrebbe aumentare di 1 dopo il salvataggio");

        // 4. Azione: Elimina il post appena creato (Cleanup)
        postDAO.delete(p.getId());

        // 5. Verifica: Controlla che il numero totale sia tornato quello iniziale
        post = postDAO.findAll();
        assertEquals(initialSize, post.size(),
                "Il numero di post dovrebbe tornare quello iniziale dopo l'eliminazione");

        // Cleanup utente
        utenteDAO.delete(autore.getUsername());

    }

    @Test
    public void testMediaManagerCopyAndRetrieve() throws java.io.IOException {
        // Init Media Folder
        it.univaq.brewhub.MediaManager.initMediaFolder();

        // Create a temp source file
        java.nio.file.Path tempPath = java.nio.file.Files.createTempFile("test_media", ".txt");
        java.nio.file.Files.write(tempPath, "test content".getBytes());
        java.io.File tempSourceFile = tempPath.toFile();

        // Test Copy
        String copiedRelativePath = it.univaq.brewhub.MediaManager.copyMediaToFolder(tempSourceFile);
        assertNotNull(copiedRelativePath, "Il percorso copiato non deve essere null");
        assertTrue(copiedRelativePath.startsWith("/media/"), "Il percorso deve iniziare con /media/");
        assertTrue(copiedRelativePath.endsWith(".txt"), "L'estensione deve essere mantenuta");

        // Test Get File
        java.io.File retrievedFile = it.univaq.brewhub.MediaManager.getMediaFile(copiedRelativePath);
        assertNotNull(retrievedFile, "Il file recuperato non deve essere null");
        assertTrue(retrievedFile.exists(), "Il file copiato deve esistere");

        // Test Relative Path Calculation
        String calcPath = it.univaq.brewhub.MediaManager.getRelativePath(retrievedFile);
        assertEquals(copiedRelativePath, calcPath, "Il percorso relativo calcolato deve corrispondere");

        // Retrieve non-existent
        assertNull(it.univaq.brewhub.MediaManager.getMediaFile("/media/non_existent_file_12345.xyz"));

        // Cleanup
        if (tempSourceFile.exists())
            tempSourceFile.delete();
        if (retrievedFile.exists())
            retrievedFile.delete();
    }
}