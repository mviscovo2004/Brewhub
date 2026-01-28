package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.model.Commento;
import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
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
 * Implementazione DAO per i Commenti.
 * <p>Gestisce anche l'invio automatico delle notifiche all'autore del post.</p>
 */
public class CommentoDAOImpl implements CommentoDAO {

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    @Override
    public void create(Commento commento) throws SQLException {
        if (commento.getPost() == null || commento.getPost().getId() == 0) {
            throw new SQLException("Impossibile salvare il commento: Post non valido o non salvato.");
        }
        if (commento.getUtente() == null || commento.getUtente().getUsername() == null) {
            throw new SQLException("Impossibile salvare il commento: Utente non valido.");
        }

        String sql = "INSERT INTO commenti(post_id, username, contenuto, data_creazione) VALUES(?, ?, ?, ?)";
        it.univaq.brewhub.model.Notifica notificationToSend = null;

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

            // Preparazione notifica per l'autore del post
            try (PreparedStatement psSel = conn
                    .prepareStatement("SELECT autore_username, titolo FROM post WHERE id = ?")) {
                psSel.setInt(1, commento.getPost().getId());
                try (ResultSet rs = psSel.executeQuery()) {
                    if (rs.next()) {
                        String author = rs.getString("autore_username");
                        String title = rs.getString("titolo");
                        // Invia solo se l'autore del commento non è l'autore del post
                        if (!author.equals(commento.getUtente().getUsername())) {
                            Utente ricevente = new Utente();
                            ricevente.setUsername(author);
                            String snippet = commento.getContenuto();
                            if (snippet.length() > 20)
                                snippet = snippet.substring(0, 20) + "...";
                            String msg = commento.getUtente().getUsername() + " ha commentato il tuo post \"" + title
                                    + "\": " + snippet;
                            notificationToSend = new it.univaq.brewhub.model.Notifica(ricevente, msg);
                        }
                    }
                }
            }
        }

        // Invia notifica
        if (notificationToSend != null) {
            notificaDAO.create(notificationToSend);
        }
    }

    @Override
    public List<Commento> findByPostId(int postId) throws SQLException {
        Post dummyPost = new Post();
        dummyPost.setId(postId);
        return findByPost(dummyPost);
    }

    /**
     * Metodo helper (o overloaded) per trovare commenti dato un oggetto Post.
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
                    
                    // Mappa l'utente in modo lazy (solo username)
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
