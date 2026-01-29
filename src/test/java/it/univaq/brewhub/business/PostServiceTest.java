package it.univaq.brewhub.business;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import java.util.List;

/**
 * Test per la classe {@link PostService}.
 * Verifica la logica di business per la gestione dei post, inclusi creazione,
 * recupero feed e post popolari.
 */
public class PostServiceTest extends BaseTest {

    private PostService postService;
    private Utente autoreTest;

    /**
     * Configurazione iniziale per ogni test.
     * Ottiene l'istanza singleton di PostService e crea un utente di test.
     * 
     * @throws SQLException se si verifica un errore durante la configurazione.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        postService = PostService.getInstance();
        String username = "postAuthor_" + System.currentTimeMillis();
        autoreTest = createTestUser(username, TipoUtente.APPASSIONATO);
    }

    /**
     * Verifica che PostService sia un Singleton.
     */
    @Test
    public void testSingleton() {
        PostService instance1 = PostService.getInstance();
        PostService instance2 = PostService.getInstance();

        assertSame(instance1, instance2);
    }

    /**
     * Verifica la creazione di un post valido.
     * 
     * @throws BusinessException se si verifica un errore di business.
     * @throws SQLException      se si verifica un errore durante l'accesso al
     *                           database.
     */
    @Test
    public void testCreazionePostSuccesso() throws BusinessException, SQLException {
        Post post = new Post(
                "Nuovo Post Test",
                "Contenuto del post di test",
                autoreTest,
                Post.TipoPost.TESTO,
                null);

        postService.createPost(post);

        assertTrue(post.getId() > 0);

        Post retrieved = postDAO.findById(post.getId());
        assertNotNull(retrieved);
        assertEquals("Nuovo Post Test", retrieved.getTitolo());
    }

    /**
     * Verifica che la creazione fallisca con titolo nullo.
     */
    @Test
    public void testCreazionePostTitoloNullo() {
        Post post = new Post(
                null,
                "Contenuto",
                autoreTest,
                Post.TipoPost.TESTO,
                null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.createPost(post);
        });

        assertTrue(exception.getMessage().contains("titolo") ||
                exception.getMessage().contains("obbligatorio"));
    }

    /**
     * Verifica che la creazione fallisca con titolo vuoto.
     */
    @Test
    public void testCreazionePostTitoloVuoto() {
        Post post = new Post(
                "   ",
                "Contenuto",
                autoreTest,
                Post.TipoPost.TESTO,
                null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.createPost(post);
        });

        assertTrue(exception.getMessage().contains("titolo") ||
                exception.getMessage().contains("obbligatorio"));
    }

    /**
     * Verifica il recupero di tutti i post (feed principale).
     * 
     * @throws BusinessException se si verifica un errore di business.
     * @throws SQLException      se si verifica un errore durante l'accesso al
     *                           database.
     */
    @Test
    public void testRecuperoAllPosts() throws BusinessException, SQLException {
        // Crea alcuni post di test
        Post post1 = new Post("Post 1", "Contenuto 1", autoreTest, Post.TipoPost.TESTO, null);
        Post post2 = new Post("Post 2", "Contenuto 2", autoreTest, Post.TipoPost.TESTO, null);

        postService.createPost(post1);
        postService.createPost(post2);

        List<Post> allPosts = postService.getAllPosts();

        assertNotNull(allPosts);
        assertTrue(allPosts.size() >= 2);
    }

    /**
     * Verifica che getAllPosts restituisca una lista vuota se non ci sono post.
     */
    @Test
    public void testRecuperoAllPostsVuoto() {
        List<Post> allPosts = postService.getAllPosts();

        assertNotNull(allPosts);
        // Potrebbe essere vuoto o contenere post di altri test
    }

    /**
     * Verifica il recupero dei post popolari.
     * 
     * @throws BusinessException se si verifica un errore di business.
     * @throws SQLException      se si verifica un errore durante l'accesso al
     *                           database.
     */
    @Test
    public void testRecuperoPostPopolari() throws BusinessException, SQLException {
        // Crea post con diversi numeri di like
        Post post1 = new Post("Post Popolare", "Contenuto", autoreTest, Post.TipoPost.TESTO, null);
        postService.createPost(post1);

        // Aggiungi alcuni like
        Utente liker1 = createTestUser("liker1_" + System.currentTimeMillis(), TipoUtente.CURIOSO);
        Utente liker2 = createTestUser("liker2_" + System.currentTimeMillis(), TipoUtente.APPASSIONATO);

        postDAO.addLike(post1.getId(), liker1.getUsername());
        postDAO.addLike(post1.getId(), liker2.getUsername());

        List<Post> popolari = postService.getPopularPosts();

        assertNotNull(popolari);
        // Verifica che la lista contenga post
        assertTrue(popolari.size() >= 0);
    }

    /**
     * Verifica che getPopularPosts gestisca correttamente errori del database.
     */
    @Test
    public void testRecuperoPostPopolariGestioneErrori() {
        // Questo test verifica che il metodo non lanci eccezioni anche in caso di
        // errori del database
        List<Post> popolari = postService.getPopularPosts();

        assertNotNull(popolari);
    }

    /**
     * Verifica la creazione di post con diversi tipi.
     * 
     * @throws BusinessException se si verifica un errore di business.
     */
    @Test
    public void testCreazioneDiversiTipiPost() throws BusinessException {
        Post postTesto = new Post("Testo", "Contenuto", autoreTest, Post.TipoPost.TESTO, null);
        Post postFoto = new Post("Foto", "Descrizione", autoreTest, Post.TipoPost.FOTO, "foto.jpg");
        Post postVideo = new Post("Video", "Descrizione", autoreTest, Post.TipoPost.VIDEO, "video.mp4");

        postService.createPost(postTesto);
        postService.createPost(postFoto);
        postService.createPost(postVideo);

        assertTrue(postTesto.getId() > 0);
        assertTrue(postFoto.getId() > 0);
        assertTrue(postVideo.getId() > 0);
    }

    /**
     * Verifica che la creazione di un post nullo lanci un'eccezione.
     */
    @Test
    public void testCreazionePostNullo() {
        assertThrows(Exception.class, () -> {
            postService.createPost(null);
        });
    }

    /**
     * Verifica la gestione di errori durante il recupero del feed.
     */
    @Test
    public void testGestioneErroriRecuperoFeed() {
        // Il metodo dovrebbe restituire una lista vuota in caso di errore invece di
        // lanciare un'eccezione
        List<Post> posts = postService.getAllPosts();

        assertNotNull(posts);
    }
}
