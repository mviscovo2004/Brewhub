package it.univaq.brewhub.model;
import it.univaq.brewhub.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.model.Post.TipoPost;
import it.univaq.brewhub.model.Utente.TipoUtente;

/**
 * Test unitari per la classe {@link Post}.
 *
 * Verifica i costruttori per i diversi tipi di post (TESTO, FOTO, VIDEO),
 * i metodi getter/setter, le operazioni CRUD sul database e l'integrazione
 * con {@link MediaManager} per la gestione dei file multimediali.
 *
 */
public class PostTest extends BaseTest {
    /**
     * Verifica il costruttore per post di tipo TESTO.
     *
     * Controlla che un post testuale venga creato correttamente
     * senza campo media.
     *
     */
    @Test
    public void testCostruttoreTesto() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.TESTO;
        String media = null;
        Post p = new Post(titolo, contenuto, u, tipo, media);
        assertEquals(titolo, p.getTitolo(), "Il titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Il contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertNull(p.getMedia(), "Il campo media del post deve essere null per tipo TESTO");
    }

    /**
     * Verifica il costruttore per post di tipo FOTO.
     *
     * Controlla che un post con foto venga creato correttamente
     * con il percorso del file immagine.
     *
     */
    @Test
    public void testCostruttoreFoto() {
        String titolo = "Test post";
        String contenuto = "Contenuto test post";
        Utente u = new Utente();
        TipoPost tipo = TipoPost.FOTO;
        String foto = "media/foto.jpg";
        Post p = new Post(titolo, contenuto, u, tipo, foto);
        assertEquals(titolo, p.getTitolo(), "Il titolo del post non corrisponde");
        assertEquals(contenuto, p.getContenuto(), "Il contenuto del post non corrisponde");
        assertEquals(u, p.getAutore(), "L'autore del post non corrisponde");
        assertEquals(tipo, p.getTipo(), "Il tipo del post non corrisponde");
        assertEquals(foto, p.getMedia(), "Il percorso della foto non corrisponde");
    }

    /**
     * Verifica il costruttore per post di tipo VIDEO.
     *
     * Controlla che un post con video venga creato correttamente
     * con il percorso del file video.
     *
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
     * Verifica i metodi setter e getter della classe Post.
     *
     * Testa tutti i setter e getter inclusi quelli per liste di like,
     * commenti e data di creazione.
     *
     */
    @Test
    public void testSetterGetter() {
        Post p = new Post();
        p.setTitolo("Test post");
        assertEquals("Test post", p.getTitolo());
        p.setContenuto("Contenuto test post");
        assertEquals("Contenuto test post", p.getContenuto());
        Utente u = new Utente();
        p.setAutore(u);
        assertEquals(u, p.getAutore());
        p.setTipo(TipoPost.TESTO);
        assertEquals(TipoPost.TESTO, p.getTipo());
        p.setMedia("media/foto.jpg");
        assertEquals("media/foto.jpg", p.getMedia());
        List<Utente> likes = new ArrayList<>();
        p.setMiPiace(likes);
        assertEquals(likes, p.getMiPiace());
        List<Commento> comments = new ArrayList<>();
        p.setCommenti(comments);
        assertEquals(comments, p.getCommenti());
        LocalDateTime now = LocalDateTime.now();
        p.setDataCreazione(now);
        assertEquals(now, p.getDataCreazione());
    }

    /**
     * Verifica le operazioni CRUD sul database per i post.
     *
     * Testa creazione ed eliminazione di un post, verificando
     * che il conteggio dei post sia corretto.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testMetodiDB() throws SQLException {
        Utente autore = createTestUser("testUserPost", TipoUtente.APPASSIONATO);
        Post p = new Post();
        p.setAutore(autore);
        p.setTitolo("Titolo Test DB");
        p.setContenuto("Contenuto Test DB");
        p.setTipo(TipoPost.TESTO);
        p.setDataCreazione(LocalDateTime.now());
        int initialSize = postDAO.findAll().size();
        postDAO.create(p);
        List<Post> post = postDAO.findAll();
        assertEquals(initialSize + 1, post.size(), "Il numero di post dovrebbe aumentare di 1 dopo il salvataggio");
        postDAO.delete(p.getId());
        post = postDAO.findAll();
        assertEquals(initialSize, post.size(),
                "Il numero di post dovrebbe tornare quello iniziale dopo l'eliminazione");
        utenteDAO.delete(autore.getUsername());
    }

    /**
     * Verifica la copia e il recupero di file multimediali.
     *
     * Testa {@link MediaManager#copyMediaToFolder(java.io.File)} e
     * {@link MediaManager#getMediaFile(String)} per assicurarsi che i file
     * vengano copiati e recuperati correttamente.
     *
     * 
     * @throws java.io.IOException se si verifica un errore I/O
     */
    @Test
    public void testMediaManagerCopyAndRetrieve() throws java.io.IOException {
        it.univaq.brewhub.utility.MediaManager.initMediaFolder();
        java.nio.file.Path tempPath = java.nio.file.Files.createTempFile("test_media", ".txt");
        java.nio.file.Files.write(tempPath, "test content".getBytes());
        java.io.File tempSourceFile = tempPath.toFile();
        String copiedRelativePath = it.univaq.brewhub.utility.MediaManager.copyMediaToFolder(tempSourceFile);
        assertNotNull(copiedRelativePath, "Il percorso copiato non deve essere null");
        assertTrue(copiedRelativePath.startsWith("/media/"), "Il percorso deve iniziare con /media/");
        assertTrue(copiedRelativePath.endsWith(".txt"), "L'estensione deve essere mantenuta");
        java.io.File retrievedFile = it.univaq.brewhub.utility.MediaManager.getMediaFile(copiedRelativePath);
        assertNotNull(retrievedFile, "Il file recuperato non deve essere null");
        assertTrue(retrievedFile.exists(), "Il file copiato deve esistere");
        String calcPath = it.univaq.brewhub.utility.MediaManager.getRelativePath(retrievedFile);
        assertEquals(copiedRelativePath, calcPath, "Il percorso relativo calcolato deve corrispondere");
        assertNull(it.univaq.brewhub.utility.MediaManager.getMediaFile("/media/non_existent_file_12345.xyz"));
        if (tempSourceFile.exists())
            tempSourceFile.delete();
        if (retrievedFile.exists())
            retrievedFile.delete();
    }

    /**
     * Verifica il conteggio totale dei post.
     *
     * Controlla che il metodo {@link it.univaq.brewhub.dao.PostDAO#countAll()}
     * restituisca il numero corretto di post.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testCountPosts() throws SQLException {
        Utente u = createTestUser("test_admin_p", TipoUtente.CURIOSO);
        int initialPosts = postDAO.countAll();
        Post p1 = createTestPost("Title1", u);
        Post p2 = createTestPost("Title2", u);
        assertEquals(initialPosts + 2, postDAO.countAll());
        postDAO.delete(p1.getId());
        postDAO.delete(p2.getId());
        utenteDAO.delete(u.getUsername());
    }

    /**
     * Verifica il conteggio dei post creati nelle ultime 24 ore.
     *
     * Controlla che solo i post recenti vengano conteggiati,
     * escludendo quelli più vecchi di 24 ore.
     *
     * 
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    @Test
    public void testCountPostsLast24h() throws SQLException {
        Utente u = createTestUser("test_admin_time", TipoUtente.CURIOSO);
        int initial24h = postDAO.countPostsLast24h();
        Post pNew = createTestPost("New", u);
        assertTrue(pNew.getId() > 0);
        Post pOld = new Post("Old", "Content", u, TipoPost.TESTO, null);
        pOld.setDataCreazione(LocalDateTime.now().minusDays(2));
        postDAO.create(pOld);
        assertTrue(pOld.getId() > 0);
        assertEquals(initial24h + 1, postDAO.countPostsLast24h(), "Should only count the new post");
        postDAO.delete(pNew.getId());
        postDAO.delete(pOld.getId());
        utenteDAO.delete(u.getUsername());
    }
}
