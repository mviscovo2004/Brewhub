package it.univaq.brewhub.dao;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.impl.PostDAOImpl;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminStatsTest {

    private static final UtenteDAOImpl utenteDAO = new UtenteDAOImpl();
    private static final PostDAOImpl postDAO = new PostDAOImpl();

    @BeforeAll
    static void initDB() {
        DatabaseManager.init();
        // Clean DB for tests ideally, or just be robust
        // For simplicity, we'll try to keep data distinct or clean up
    }

    @BeforeEach
    void cleanup() throws SQLException {
        // Simple cleanup of test data
        // NOTE: In a real env we might use a separate test DB
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
            // We won't wipe everything to avoid breaking other things,
            // but we will delete specific test users if they exist.
            stmt.executeUpdate("DELETE FROM post WHERE autore_username LIKE 'test_admin_%'");
            stmt.executeUpdate("DELETE FROM utenti WHERE username LIKE 'test_admin_%'");
        }
    }

    @Test
    @Order(1)
    void testCountAllUsers() throws SQLException {
        int initialCount = utenteDAO.countAll();

        Utente u1 = new Utente("test_admin_1", "Test", "test_admin_1", "password", Utente.TipoUtente.UTENTE_MEDIO,
                null);
        Utente u2 = new Utente("test_admin_2", "Test", "test_admin_2", "password", Utente.TipoUtente.UTENTE_MEDIO,
                null);

        utenteDAO.create(u1);
        utenteDAO.create(u2);

        assertEquals(initialCount + 2, utenteDAO.countAll());
    }

    @Test
    @Order(2)
    void testCountPosts() throws SQLException {
        Utente u = new Utente("test_admin_p", "Post", "test_admin_p", "pass", Utente.TipoUtente.UTENTE_MEDIO, null);
        utenteDAO.create(u);

        int initialPosts = postDAO.countAll();

        Post p1 = new Post("Title1", "Content1", u, Post.TipoPost.TESTO, null);
        postDAO.create(p1);
        Post p2 = new Post("Title2", "Content2", u, Post.TipoPost.TESTO, null);
        postDAO.create(p2);

        assertEquals(initialPosts + 2, postDAO.countAll());
    }

    @Test
    @Order(3)
    void testCountPostsLast24h() throws SQLException {
        Utente u = new Utente("test_admin_time", "Time", "test_admin_time", "pass", Utente.TipoUtente.UTENTE_MEDIO,
                null);
        utenteDAO.create(u);

        int initial24h = postDAO.countPostsLast24h();

        // Created now (default)
        Post pNew = new Post("New", "Content", u, Post.TipoPost.TESTO, null);
        postDAO.create(pNew);

        // We can't easily insert old dates via DAO create() as it uses
        // LocalDateTime.now() inside or passed?
        // Let's check PostDAOImpl.create: it uses post.getDataCreazione().toString()
        // So we can manipulate it.

        Post pOld = new Post("Old", "Content", u, Post.TipoPost.TESTO, null);
        pOld.setDataCreazione(LocalDateTime.now().minusDays(2)); // 2 days ago
        postDAO.create(pOld);

        assertEquals(initial24h + 1, postDAO.countPostsLast24h(), "Should only count the new post");
    }

    @Test
    @Order(4)
    void testFindTopActiveUsers() throws SQLException {
        // Create 2 users
        Utente top = new Utente("test_top", "Top", "test_top", "pass", Utente.TipoUtente.UTENTE_MEDIO, null);
        Utente bottom = new Utente("test_bottom", "Bot", "test_bottom", "pass", Utente.TipoUtente.UTENTE_MEDIO, null);

        utenteDAO.create(top);
        utenteDAO.create(bottom);

        // Top has 2 posts
        postDAO.create(new Post("T1", "C", top, Post.TipoPost.TESTO, null));
        postDAO.create(new Post("T2", "C", top, Post.TipoPost.TESTO, null));

        // Bottom has 0 posts (or we can give 1 later)

        List<Utente> toplist = utenteDAO.findTopActiveUsers(10);
        // Find our test users in the list and verify order

        // Filter mainly to avoid noise from existing DB data
        long topRank = -1;
        long botRank = -1;

        for (int i = 0; i < toplist.size(); i++) {
            if (toplist.get(i).getUsername().equals("test_top"))
                topRank = i;
            if (toplist.get(i).getUsername().equals("test_bottom"))
                botRank = i;
        }

        assertTrue(topRank != -1, "Top user should be in list");
        // Only assert relation if both are present (bottom might be excluded if limit
        // is small and db matches)
        // But with limit 10 and cleanish db it should be fine.

        if (botRank != -1) {
            assertTrue(topRank < botRank, "Top user should be ranked higher (lower index) than bottom user");
        }
    }
}
