package it.univaq.brewhub.dao;
import it.univaq.brewhub.BaseTest;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.model.Recensione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per la classe {@link it.univaq.brewhub.dao.impl.RecensioneDAOImpl}.
 * <p>
 * Verifica le operazioni CRUD e le funzionalità specifiche come il calcolo della media
 * e la verifica delle recensioni utente.
 * </p>
 */
public class RecensioneDAOTest extends BaseTest {

    /**
     * Verifica la creazione di una recensione e il suo recupero tramite l'ID del post.
     * 
     * @throws SQLException se si verifica un errore durante l'interazione con il database.
     */
    @Test
    public void testCreateAndFind() throws SQLException {
        Utente u = new Utente("User", "Test", "user_test", "pass", Utente.TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u);
        Post p = new Post("Titolo", "Contenuto", u, Post.TipoPost.TESTO, null);
        postDAO.create(p);
        List<Post> posts = postDAO.findAll();
        p = posts.get(0);
        Recensione r = new Recensione(p, u, 5, "Ottimo!", "2023-10-27");
        recensioneDAO.create(r);
        List<Recensione> list = recensioneDAO.findByPost(p.getId());
        assertEquals(1, list.size());
        assertEquals("Ottimo!", list.get(0).getTesto());
        assertEquals(5, list.get(0).getVoto());
    }

    /**
     * Verifica il calcolo corretto della media dei voti per un post.
     * 
     * @throws SQLException se si verifica un errore durante l'interazione con il database.
     */
    @Test
    public void testAverageRating() throws SQLException {
        Utente u1 = new Utente("User1", "Test", "u1", "pass", Utente.TipoUtente.APPASSIONATO, null);
        Utente u2 = new Utente("User2", "Test", "u2", "pass", Utente.TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u1);
        utenteDAO.create(u2);
        Post p = new Post("Titolo", "Contenuto", u1, Post.TipoPost.TESTO, null);
        postDAO.create(p);
        List<Post> posts = postDAO.findAll();
        p = posts.get(0);
        Recensione r1 = new Recensione(p, u1, 3, "Ok", "2023-10-27");
        Recensione r2 = new Recensione(p, u2, 5, "Super", "2023-10-28");
        recensioneDAO.create(r1);
        recensioneDAO.create(r2);
        double avg = recensioneDAO.getAverageRating(p.getId());
        assertEquals(4.0, avg, 0.01);
    }

    /**
     * Verifica se un utente ha già recensito un determinato post.
     * 
     * @throws SQLException se si verifica un errore durante l'interazione con il database.
     */
    @Test
    public void testHasUserReviewed() throws SQLException {
        Utente u = new Utente("User", "Test", "u_check", "pass", Utente.TipoUtente.APPASSIONATO, null);
        utenteDAO.create(u);
        Post p = new Post("Titolo", "Contenuto", u, Post.TipoPost.TESTO, null);
        postDAO.create(p);
        List<Post> posts = postDAO.findAll();
        p = posts.get(0);
        assertFalse(recensioneDAO.hasUserReviewed(p.getId(), "u_check"));
        Recensione r = new Recensione(p, u, 4, "Good", "2023-10-27");
        recensioneDAO.create(r);
        assertTrue(recensioneDAO.hasUserReviewed(p.getId(), "u_check"));
    }
}
