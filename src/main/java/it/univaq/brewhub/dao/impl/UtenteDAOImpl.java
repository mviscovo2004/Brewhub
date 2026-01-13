package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.dao.UtenteDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementazione dell'interfaccia UtenteDAO.
 */
public class UtenteDAOImpl implements UtenteDAO {

    @Override
    public void create(Utente u) throws SQLException {
        if (findByUsername(u.getUsername()) != null) {
            throw new SQLException("Username esistente");
        }

        String sql = "INSERT INTO utenti(username, nome, cognome, password_hash, tipo, foto_uri) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getUsername());
            pstmt.setString(2, u.getNome());
            pstmt.setString(3, u.getCognome());

            String hash = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());

            pstmt.setString(4, hash);
            pstmt.setString(5, u.getTipo().name());
            pstmt.setString(6, u.getFotoProfilo());

            pstmt.executeUpdate();
        }
    }

    @Override
    public Utente login(String username, String passwordInserita) throws SQLException {
        String sql = "SELECT * FROM utenti WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (BCrypt.checkpw(passwordInserita, storedHash)) {
                        return mapResultSetToUtente(rs);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void update(Utente u) throws SQLException {
        String sql;
        boolean cambioPassword = u.getPassword() != null && !u.getPassword().equals(u.getPasswordCrypto());

        if (cambioPassword) {
            sql = "UPDATE utenti SET nome = ?, cognome = ?, tipo = ?, password_hash = ?, foto_uri = ? WHERE username = ?";
        } else {
            sql = "UPDATE utenti SET nome = ?, cognome = ?, tipo = ?, foto_uri = ? WHERE username = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getNome());
            pstmt.setString(2, u.getCognome());
            pstmt.setString(3, u.getTipo().name());

            if (cambioPassword) {
                String hash = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());
                u.setPasswordCrypto(hash);
                pstmt.setString(4, hash);
                pstmt.setString(5, u.getFotoProfilo());
                pstmt.setString(6, u.getUsername());
            } else {
                pstmt.setString(4, u.getFotoProfilo());
                pstmt.setString(5, u.getUsername());
            }

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(String username) throws SQLException {
        String sql = "DELETE FROM utenti WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public Utente findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM utenti WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtente(rs);
                }
            }
        }
        return null;
    }

    /**
     * Mappa una riga del ResultSet in un oggetto Utente.
     *
     * @param rs Il ResultSet.
     * @return L'oggetto Utente.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    private Utente mapResultSetToUtente(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        u.setUsername(rs.getString("username"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setPasswordCrypto(rs.getString("password_hash")); // Setta hash

        String tipoStr = rs.getString("tipo");
        try {
            u.setTipo(TipoUtente.valueOf(tipoStr));
        } catch (IllegalArgumentException e) {
            u.setTipo(TipoUtente.APPASSIONATO); // Default
        }

        u.setFotoProfilo(rs.getString("foto_uri"));
        return u;
    }

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    @Override
    public void follow(String follower, String followed) throws SQLException {
        if (follower.equals(followed))
            return;

        it.univaq.brewhub.Notifica notificationToSend = null;
        String sql = "INSERT OR IGNORE INTO followers(follower_username, followed_username) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, follower);
            pstmt.setString(2, followed);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                // Prepara notifica
                Utente ricevente = new Utente();
                ricevente.setUsername(followed);
                notificationToSend = new it.univaq.brewhub.Notifica(ricevente,
                        follower + " ha iniziato a seguirti.");
            }
        }

        // Invia notifica dopo aver chiuso la connessione precedente
        if (notificationToSend != null) {
            notificaDAO.create(notificationToSend);
        }
    }

    @Override
    public void unfollow(String follower, String followed) throws SQLException {
        String sql = "DELETE FROM followers WHERE follower_username = ? AND followed_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, follower);
            pstmt.setString(2, followed);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean isFollowing(String follower, String followed) throws SQLException {
        String sql = "SELECT 1 FROM followers WHERE follower_username = ? AND followed_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, follower);
            pstmt.setString(2, followed);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int getFollowersCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM followers WHERE followed_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getFollowingCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM followers WHERE follower_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public void addToArchive(String username, int postId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO saved_posts(username, post_id) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void removeFromArchive(String username, int postId) throws SQLException {
        String sql = "DELETE FROM saved_posts WHERE username = ? AND post_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean isArchived(String username, int postId) throws SQLException {
        String sql = "SELECT 1 FROM saved_posts WHERE username = ? AND post_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public java.util.List<it.univaq.brewhub.Post> getArchive(String username) throws SQLException {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        String sql = "SELECT post_id FROM saved_posts WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    ids.add(rs.getInt("post_id"));
            }
        }

        java.util.List<it.univaq.brewhub.Post> posts = new java.util.ArrayList<>();
        it.univaq.brewhub.dao.PostDAO postDAO = new it.univaq.brewhub.dao.impl.PostDAOImpl();
        for (int id : ids) {
            it.univaq.brewhub.Post p = postDAO.findById(id);
            if (p != null)
                posts.add(p);
        }
        return posts;
    }

    @Override
    public java.util.List<Utente> searchByUsername(String partialUsername) throws SQLException {
        java.util.List<Utente> results = new java.util.ArrayList<>();
        String sql = "SELECT * FROM utenti WHERE username LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + partialUsername + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToUtente(rs));
                }
            }
        }
        return results;
    }
}
