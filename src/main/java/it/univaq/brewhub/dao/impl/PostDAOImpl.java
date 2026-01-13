package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Post.TipoPost;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.PostDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia PostDAO per le operazioni database relative
 * ai Post.
 */
public class PostDAOImpl implements PostDAO {

    /** DAO per gestione dei commenti associati ai post. */
    private CommentoDAOImpl commentoDAO = new CommentoDAOImpl();

    @Override
    public void create(Post post) throws SQLException {
        String sql = "INSERT INTO post(autore_username, titolo, contenuto, tipo, data_creazione, media_uri, category_id) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, post.getAutore().getUsername());
            pstmt.setString(2, post.getTitolo());
            pstmt.setString(3, post.getContenuto());
            pstmt.setString(4, post.getTipo().name());
            pstmt.setString(5, post.getDataCreazione().toString());
            pstmt.setString(6, post.getMedia() != null ? post.getMedia().replace('\\', '/') : null);

            if (post.getCategoria() != null) {
                pstmt.setInt(7, post.getCategoria().getId());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creazione post fallita, nessuna riga aggiunta.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creazione post fallita, nessun ID ottenuto.");
                }
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        if (id > 0) {
            String deleteSaved = "DELETE FROM saved_posts WHERE post_id = ?";
            String deleteLikes = "DELETE FROM likes WHERE post_id = ?";
            String deleteComments = "DELETE FROM commenti WHERE post_id = ?";
            String deletePost = "DELETE FROM post WHERE id = ?";

            try (Connection conn = DatabaseManager.getConnection()) {
                boolean originalAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);

                try {
                    // 1. Delete from saved_posts (Archive)
                    try (PreparedStatement ps = conn.prepareStatement(deleteSaved)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    // 2. Delete likes
                    try (PreparedStatement ps = conn.prepareStatement(deleteLikes)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    // 3. Delete comments
                    try (PreparedStatement ps = conn.prepareStatement(deleteComments)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    // 4. Delete the post itself
                    try (PreparedStatement ps = conn.prepareStatement(deletePost)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(originalAutoCommit);
                }
            }
        }
    }

    @Override
    public List<Post> findAll() throws SQLException {
        String sql = "SELECT * FROM post ORDER BY data_creazione DESC";
        return executeQuery(sql);
    }

    @Override
    public List<Post> search(String query) throws SQLException {
        String sql = "SELECT * FROM post WHERE titolo LIKE ? OR contenuto LIKE ? ORDER BY data_creazione DESC";
        String p = "%" + query + "%";
        return executeQuery(sql, p, p);
    }

    /**
     * Esegue la query e mappa il result set in una lista di oggetti Post.
     *
     * @param sql    La query SQL da eseguire.
     * @param params I parametri opzionali per la query.
     * @return Una lista di oggetti Post.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    private List<Post> executeQuery(String sql, Object... params) throws SQLException {
        List<Post> posts = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    // --- Gestione Like ---

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    @Override
    public void addLike(int postId, String username) throws SQLException {
        it.univaq.brewhub.Notifica notificationToSend = null;

        String sql = "INSERT OR IGNORE INTO likes (post_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                // Recupera autore del post per notifica
                try (PreparedStatement psSel = conn.prepareStatement("SELECT autore_username FROM post WHERE id = ?")) {
                    psSel.setInt(1, postId);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (rs.next()) {
                            String author = rs.getString("autore_username");
                            if (!author.equals(username)) { // Non notificare se ti metti like da solo
                                Utente ricevente = new Utente();
                                ricevente.setUsername(author);
                                notificationToSend = new it.univaq.brewhub.Notifica(ricevente,
                                        username + " ha messo mi piace al tuo post.");
                            }
                        }
                    }
                }
            }
        }

        // Invia notifica dopo aver chiuso la connessione precedente per evitare lock
        // SQLite
        if (notificationToSend != null) {
            notificaDAO.create(notificationToSend);
        }
    }

    @Override
    public void removeLike(int postId, String username) throws SQLException {
        String sql = "DELETE FROM likes WHERE post_id = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean isLiked(int postId, String username) throws SQLException {
        String sql = "SELECT 1 FROM likes WHERE post_id = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int getLikesCount(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public List<Post> findByAuthor(String username) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post WHERE autore_username = ? ORDER BY data_creazione DESC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    @Override
    public Post findById(int id) throws SQLException {
        String sql = "SELECT * FROM post WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Post> findByCategory(int categoryId) throws SQLException {
        String sql = "SELECT * FROM post WHERE category_id = ? ORDER BY data_creazione DESC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Post> posts = new ArrayList<>();
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
                return posts;
            }
        }
    }

    @Override
    public List<Post> findByUserType(String userType) throws SQLException {
        // Supponendo che userType sia una stringa che corrisponde al campo 'tipo' nella
        // tabella utenti
        String sql = "SELECT p.*, c.nome as categoria_nome FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "WHERE u.tipo = ? ORDER BY p.data_creazione DESC";

        // Note: mapResultSetToPost might need adjustment if using explicit columns or
        // trying to map joined cols
        // But mapResultSetToPost expects "id", "autore_username", etc. which are
        // present in p.*
        // Check if mapResultSetToPost handles potential ambiguity or not.
        // With p.* we get all post columns. u.username is joined on autore_username.
        // Let's use a simpler query if column names overlap.
        // Actually, mapResultSetToPost uses column names like "id". "post.id" and
        // "notifiche.id" may conflict if joined.
        // Here we join with Utenti. Utenti has "username", "nome", "cognome", "tipo",
        // "password_hash", "foto_uri".
        // Post has "id", "autore_username", "titolo" ...
        // No strict overlap on "id" usually since users have username PK.
        // Wait, "foto_uri" might be in both if I added it to Post? No, Post has
        // media_uri.
        // So SELECT * should be fine mostly, but cleaner to specify p.*

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userType);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Post> posts = new ArrayList<>();
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
                return posts;
            }
        }
    }

    @Override
    public List<Post> findPopular() throws SQLException {
        // Ordina per numero di like (join con tabella likes)
        String sql = "SELECT p.*, COUNT(l.post_id) as like_count " +
                "FROM post p " +
                "LEFT JOIN likes l ON p.id = l.post_id " +
                "GROUP BY p.id " +
                "ORDER BY like_count DESC, p.data_creazione DESC " +
                "LIMIT 50";
        // Nota: executeQuery gestisce SELECT * ma qui abbiamo una colonna in più.
        // mapResultSetToPost ignorerà la colonna extra senza problemi.
        return executeQuery(sql);
    }

    @Override
    public List<Post> findLikedBy(String username) throws SQLException {
        String sql = "SELECT p.* " +
                "FROM post p " +
                "JOIN likes l ON p.id = l.post_id " +
                "WHERE l.username = ? " +
                "ORDER BY p.data_creazione DESC";
        return executeQuery(sql, username);
    }

    @Override
    public List<Post> findFeedForUser(String username) throws SQLException {
        // Seleziona post degli utenti seguiti dall'utente corrente
        String sql = "SELECT p.* " +
                "FROM post p " +
                "JOIN followers f ON p.autore_username = f.followed_username " +
                "WHERE f.follower_username = ? " +
                "ORDER BY p.data_creazione DESC";
        return executeQuery(sql, username);
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));

        Utente autore = new Utente();
        autore.setUsername(rs.getString("autore_username"));
        post.setAutore(autore);

        post.setTitolo(rs.getString("titolo"));
        post.setContenuto(rs.getString("contenuto"));

        try {
            post.setTipo(TipoPost.valueOf(rs.getString("tipo")));
        } catch (IllegalArgumentException e) {
            post.setTipo(TipoPost.TESTO);
        }

        post.setDataCreazione(LocalDateTime.parse(rs.getString("data_creazione")));
        post.setMedia(rs.getString("media_uri"));

        // Mappa Categoria - requires JOIN or lazy load.
        // Better to use JOIN in queries, but for now let's lazy load or just check if
        // column exists/is populated
        // The simple find methods select * from post.
        // We can do a quick lookup if category_id > 0
        int catId = rs.getInt("category_id");
        if (!rs.wasNull() && catId > 0) {
            it.univaq.brewhub.Categoria c = new it.univaq.brewhub.Categoria();
            c.setId(catId);
            // We can fetch name quickly or do a JOIN.
            // JOIN is better performance wise but requires changing all SQL queries.
            // Let's do a sub-query fetch here for simplicity given existing structure
            // OR update findAll/search queries to use JOIN.
            // Let's just fetch name via DAO helper or simple query.
            // Actually, let's keep it simple: just ID is enough? No, UI needs name.
            // Ideally we change all SELECT * FROM post to SELECT p.*, c.nome as cat_nome
            // FROM post p LEFT JOIN categorie c ON p.category_id = c.id
            // But let's simplify: lazy fetch.
            // No, lazy fetch in a loop is N+1.
            // Let's assume we can fetch it.
            // For now, I'll instantiate a simple DAO here or use a helper query.
            try (PreparedStatement psCat = rs.getStatement().getConnection()
                    .prepareStatement("SELECT nome FROM categorie WHERE id = ?")) {
                psCat.setInt(1, catId);
                try (ResultSet rsCat = psCat.executeQuery()) {
                    if (rsCat.next()) {
                        c.setNome(rsCat.getString("nome"));
                    }
                }
            }
            post.setCategoria(c);
        }

        // Caricamento Commenti tramite DAO
        post.setCommenti(commentoDAO.findByPost(post));

        return post;
    }
}
