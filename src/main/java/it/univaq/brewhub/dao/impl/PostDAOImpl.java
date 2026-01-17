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
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "ORDER BY p.data_creazione DESC";
        return executeQuery(sql);
    }

    @Override
    public List<Post> search(String query) throws SQLException {
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "WHERE p.titolo LIKE ? OR p.contenuto LIKE ? " +
                "ORDER BY p.data_creazione DESC";
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
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "WHERE p.autore_username = ? ORDER BY p.data_creazione DESC";
        return executeQuery(sql, username);
    }

    @Override
    public Post findById(int id) throws SQLException {
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "WHERE p.id = ?";
        List<Post> results = executeQuery(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Post> findByCategory(int categoryId) throws SQLException {
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "WHERE p.category_id = ? ORDER BY p.data_creazione DESC";
        return executeQuery(sql, categoryId);
    }

    @Override
    public List<Post> findByUserType(String userType) throws SQLException {
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "WHERE u.tipo = ? ORDER BY p.data_creazione DESC";
        return executeQuery(sql, userType);
    }

    @Override
    public List<Post> findPopular() throws SQLException {
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome, COUNT(l.post_id) as like_count "
                +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "LEFT JOIN likes l ON p.id = l.post_id " +
                "GROUP BY p.id " +
                "ORDER BY like_count DESC, p.data_creazione DESC " +
                "LIMIT 50";
        return executeQuery(sql);
    }

    @Override
    public List<Post> findLikedBy(String username) throws SQLException {
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
                "JOIN likes l ON p.id = l.post_id " +
                "WHERE l.username = ? " +
                "ORDER BY p.data_creazione DESC";
        return executeQuery(sql, username);
    }

    @Override
    public List<Post> findFeedForUser(String username) throws SQLException {
        String sql = "SELECT p.*, u.tipo as user_type, u.foto_uri as user_foto, c.nome as cat_nome " +
                "FROM post p " +
                "JOIN utenti u ON p.autore_username = u.username " +
                "LEFT JOIN categorie c ON p.category_id = c.id " +
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

        // Mappatura UserType da JOIN
        try {
            String typeStr = rs.getString("user_type");
            if (typeStr != null) {
                autore.setTipo(Utente.TipoUtente.valueOf(typeStr));
            } else {
                autore.setTipo(Utente.TipoUtente.APPASSIONATO);
            }
        } catch (SQLException | IllegalArgumentException e) {
            autore.setTipo(Utente.TipoUtente.APPASSIONATO);
        }

        // Mappatura Foto Profilo da JOIN
        try {
            autore.setFotoProfilo(rs.getString("user_foto"));
        } catch (SQLException e) {
            // Ignora se colonna mancante (ma non dovrebbe)
        }

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

        // Mappa Categoria da JOIN
        int catId = rs.getInt("category_id");
        if (!rs.wasNull() && catId > 0) {
            it.univaq.brewhub.Categoria c = new it.univaq.brewhub.Categoria();
            c.setId(catId);
            try {
                c.setNome(rs.getString("cat_nome"));
            } catch (SQLException e) {
                c.setNome("Categoria");
            }
            post.setCategoria(c);
        }

        // Caricamento Commenti tramite DAO (rimane separato per ora)
        post.setCommenti(commentoDAO.findByPost(post));

        return post;
    }

    @Override
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM post";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int countPostsLast24h() throws SQLException {
        // SQLite uses 'now', '-1 day' for date math
        String sql = "SELECT COUNT(*) FROM post WHERE data_creazione >= datetime('now', '-1 day')";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
