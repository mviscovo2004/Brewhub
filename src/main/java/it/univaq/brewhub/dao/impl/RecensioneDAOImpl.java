package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.dao.RecensioneDAO;
import it.univaq.brewhub.model.Recensione;
import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.model.Utente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia {@link RecensioneDAO}.
 * Gestisce la persistenza delle recensioni e l'invio di notifiche all'autore
 * del post recensito.
 */
public class RecensioneDAOImpl implements RecensioneDAO {

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();
    private final PostDAOImpl postDAO = new PostDAOImpl();

    /**
     * {@inheritDoc}
     * <p>
     * Se la recensione viene creata con successo e il recensore non è l'autore del
     * post,
     * viene inviata una notifica all'autore del post.
     * </p>
     */
    @Override
    public void create(Recensione recensione) throws SQLException {
        boolean added = false;
        String sql = "INSERT INTO recensioni(post_id, username, voto, testo, data_creazione) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, recensione.getPost().getId());
            pstmt.setString(2, recensione.getAutore().getUsername());
            pstmt.setInt(3, recensione.getVoto());
            pstmt.setString(4, recensione.getTesto());
            pstmt.setString(5, recensione.getDataCreazione());
            try {
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            recensione.setId(generatedKeys.getInt(1));
                        }
                    }
                    added = true;
                }
            } catch (SQLException e) {
                // Probabile violazione vincolo UNIQUE (utente ha già recensito)
                throw e;
            }
        }

        if (added) {
            sendRecensioneNotification(recensione);
        }
    }

    /**
    * {@inheritDoc}
    */

    @Override
    public void update(Recensione recensione) throws SQLException {
        String sql = "UPDATE recensioni SET voto = ?, testo = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recensione.getVoto());
            pstmt.setString(2, recensione.getTesto());
            pstmt.setInt(3, recensione.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Aggiornamento della recensione fallito, nessuna riga modificata.");
            }
        }
    }

    /**
     * Notifica l'autore del post di una nuova recensione.
     *
     * @param r recensione appena creata.
     */
    private void sendRecensioneNotification(Recensione r) {
        try {
            it.univaq.brewhub.model.Post p = r.getPost();
            // Recupera il post completo se necessario per avere l'autore
            if (p != null) {
                String authorUsername = null;
                if (p.getAutore() != null) {
                    authorUsername = p.getAutore().getUsername();
                } else {
                    it.univaq.brewhub.model.Post fullPost = postDAO.findById(p.getId());
                    if (fullPost != null && fullPost.getAutore() != null) {
                        authorUsername = fullPost.getAutore().getUsername();
                    }
                }

                if (authorUsername != null && !authorUsername.equals(r.getAutore().getUsername())) {
                    it.univaq.brewhub.model.Utente dest = new it.univaq.brewhub.model.Utente();
                    dest.setUsername(authorUsername);
                    String msg = r.getAutore().getUsername() + " ha recensito il tuo post \"" + p.getTitolo() + "\"";
                    notificaDAO.create(new it.univaq.brewhub.model.Notifica(dest, msg));
                }
            }
        } catch (Exception e) {
            System.err.println("Errore notifica recensione: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Recensione> findByPost(int postId) throws SQLException {
        List<Recensione> recensioni = new ArrayList<>();
        String sql = "SELECT * FROM recensioni WHERE post_id = ? ORDER BY data_creazione DESC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                UtenteDAOImpl utenteDAO = new UtenteDAOImpl();
                PostDAOImpl postDAO = new PostDAOImpl();
                Post p = postDAO.findById(postId);

                while (rs.next()) {
                    Recensione r = new Recensione();
                    r.setId(rs.getInt("id"));
                    r.setPost(p);

                    String username = rs.getString("username");
                    Utente u = utenteDAO.findByUsername(username);
                    if (u == null) {
                        // Fallback per utenti eliminati se non gestito da cascade
                        u = new Utente();
                        u.setUsername("deleted_user");
                    }
                    r.setAutore(u);

                    r.setVoto(rs.getInt("voto"));
                    r.setTesto(rs.getString("testo"));
                    r.setDataCreazione(rs.getString("data_creazione"));
                    recensioni.add(r);
                }
            }
        }
        return recensioni;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM recensioni WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAverageRating(int postId) throws SQLException {
        String sql = "SELECT AVG(voto) FROM recensioni WHERE post_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUserReviewed(int postId, String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM recensioni WHERE post_id = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
