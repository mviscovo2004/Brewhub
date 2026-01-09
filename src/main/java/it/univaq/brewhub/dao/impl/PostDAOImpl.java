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
        String sql = "INSERT INTO post(autore_username, titolo, contenuto, tipo, data_creazione, media_uri) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, post.getAutore().getUsername());
            pstmt.setString(2, post.getTitolo());
            pstmt.setString(3, post.getContenuto());
            pstmt.setString(4, post.getTipo().name());
            pstmt.setString(5, post.getDataCreazione().toString());
            pstmt.setString(6, post.getMedia() != null ? post.getMedia().replace('\\', '/') : null);

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
            String sql = "DELETE FROM post WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        }
    }

    @Override
    public List<Post> findAll() throws SQLException {
        String sql = "SELECT * FROM post ORDER BY data_creazione DESC";
        return executeQuery(sql, null);
    }

    @Override
    public List<Post> search(String query) throws SQLException {
        String sql = "SELECT * FROM post WHERE titolo LIKE ? OR contenuto LIKE ? ORDER BY data_creazione DESC";
        return executeQuery(sql, "%" + query + "%");
    }

    /**
     * Esegue la query e mappa il result set in una lista di oggetti Post.
     *
     * @param sql         La query SQL da eseguire.
     * @param searchParam Il parametro di ricerca da associare, o null se non
     *                    applicabile.
     * @return Una lista di oggetti Post.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    private List<Post> executeQuery(String sql, String searchParam) throws SQLException {
        List<Post> posts = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (searchParam != null) {
                pstmt.setString(1, searchParam);
                pstmt.setString(2, searchParam);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
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

                    // Caricamento Commenti tramite DAO
                    post.setCommenti(commentoDAO.findByPost(post));

                    posts.add(post);
                }
            }
        }
        return posts;
    }

    // --- Gestione Like ---

    @Override
    public void addLike(int postId, String username) throws SQLException {
        String sql = "INSERT OR IGNORE INTO likes (post_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
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
}
