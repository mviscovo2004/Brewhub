package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.Commento;
import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Post;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.CommentoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia CommentoDAO.
 */
public class CommentoDAOImpl implements CommentoDAO {

    @Override
    public void create(Commento commento) throws SQLException {
        if (commento.getPost() == null || commento.getPost().getId() == 0) {
            throw new SQLException("Impossibile salvare il commento: Post non valido o non salvato.");
        }
        if (commento.getUtente() == null || commento.getUtente().getUsername() == null) {
            throw new SQLException("Impossibile salvare il commento: Utente non valido.");
        }

        String sql = "INSERT INTO commenti(post_id, username, contenuto, data_creazione) VALUES(?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, commento.getPost().getId());
            pstmt.setString(2, commento.getUtente().getUsername());
            pstmt.setString(3, commento.getContenuto());
            pstmt.setString(4, commento.getDataCreazione().toString());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Salvataggio commento fallito, nessuna riga aggiunta.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commento.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Salvataggio commento fallito, nessun ID ottenuto.");
                }
            }
        }
    }

    @Override
    public List<Commento> findByPostId(int postId) throws SQLException {
        // Per ricostruire il commento serve l'oggetto Post con quell'ID (anche
        // parziale)
        Post dummyPost = new Post();
        dummyPost.setId(postId);
        return findByPost(dummyPost);
    }

    /**
     * Recupera i commenti per un oggetto Post specifico.
     *
     * @param post Il post per cui recuperare i commenti.
     * @return Una lista di commenti.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    public List<Commento> findByPost(Post post) throws SQLException {
        List<Commento> commenti = new ArrayList<>();
        if (post == null || post.getId() == 0)
            return commenti;

        String sql = "SELECT * FROM commenti WHERE post_id = ? ORDER BY data_creazione ASC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, post.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Commento c = new Commento();
                    c.setId(rs.getInt("id"));
                    c.setPost(post);
                    c.setContenuto(rs.getString("contenuto"));
                    c.setDataCreazione(LocalDateTime.parse(rs.getString("data_creazione")));

                    Utente u = new Utente();
                    u.setUsername(rs.getString("username"));
                    c.setUtente(u);

                    commenti.add(c);
                }
            }
        }
        return commenti;
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM commenti WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void update(Commento commento) throws SQLException {
        String sql = "UPDATE commenti SET contenuto = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, commento.getContenuto());
            pstmt.setInt(2, commento.getId());
            pstmt.executeUpdate();
        }
    }
}
