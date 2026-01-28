package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Post.TipoPost;
import it.univaq.brewhub.model.Utente;
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
 * Implementazione DAO per i Post.
 * <p>Gestisce la persistenza dei post, le ricerche, i like e il recupero del feed.</p>
 */
public class PostDAOImpl implements PostDAO {

    private CommentoDAOImpl commentoDAO = new CommentoDAOImpl();
    private static final java.time.format.DateTimeFormatter DB_DATE_FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    @Override
    public void create(Post post) throws SQLException {
        String sql = "INSERT INTO post(autore_username, titolo, contenuto, tipo, data_creazione, media_uri, category_id) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, post.getAutore().getUsername());
            pstmt.setString(2, post.getTitolo());
            pstmt.setString(3, post.getContenuto());
            pstmt.setString(4, post.getTipo().name());
            pstmt.setString(5, post.getDataCreazione().format(DB_DATE_FORMATTER));
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
                    try (PreparedStatement ps = conn.prepareStatement(deleteSaved)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(deleteLikes)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(deleteComments)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
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

    @Override
    public void addLike(int postId, String username) throws SQLException {
        it.univaq.brewhub.model.Notifica notificationToSend = null;
        String sql = "INSERT OR IGNORE INTO likes (post_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Notifica all'autore
                try (PreparedStatement psSel = conn.prepareStatement("SELECT autore_username FROM post WHERE id = ?")) {
                    psSel.setInt(1, postId);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (rs.next()) {
                            String author = rs.getString("autore_username");
                            if (!author.equals(username)) { 
                                Utente ricevente = new Utente();
                                ricevente.setUsername(author);
                                notificationToSend = new it.univaq.brewhub.model.Notifica(ricevente,
                                        username + " ha messo mi piace al tuo post.");
                            }
                        }
                    }
                }
            }
        }
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
        // Popolarità basata sui Like
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
        // Post degli utenti seguiti
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
        
        try {
            autore.setFotoProfilo(rs.getString("user_foto"));
        } catch (SQLException e) {
        }
        post.setAutore(autore);
        
        post.setTitolo(rs.getString("titolo"));
        post.setContenuto(rs.getString("contenuto"));
        
        try {
            post.setTipo(TipoPost.valueOf(rs.getString("tipo")));
        } catch (IllegalArgumentException e) {
            post.setTipo(TipoPost.TESTO);
        }
        
        try {
            String dateStr = rs.getString("data_creazione");
            if (dateStr.contains("T")) {
                post.setDataCreazione(LocalDateTime.parse(dateStr));
            } else {
                post.setDataCreazione(LocalDateTime.parse(dateStr, DB_DATE_FORMATTER));
            }
        } catch (Exception e) {
            post.setDataCreazione(LocalDateTime.now());
        }
        
        post.setMedia(rs.getString("media_uri"));
        
        int catId = rs.getInt("category_id");
        if (!rs.wasNull() && catId > 0) {
            it.univaq.brewhub.model.Categoria c = new it.univaq.brewhub.model.Categoria();
            c.setId(catId);
            try {
                c.setNome(rs.getString("cat_nome"));
            } catch (SQLException e) {
                c.setNome("Categoria");
            }
            post.setCategoria(c);
        }
        
        // Caricamento commenti
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
        // Sintassi SQLite per tempo
        String sql = "SELECT COUNT(*) FROM post WHERE data_creazione >= datetime('now', '-1 day', 'localtime')";
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
