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
        // Soft-delete: Cancella likes/followers e anonimizza l'utente
        String deleteLikes = "DELETE FROM likes WHERE username = ?";
        String deleteFollower = "DELETE FROM followers WHERE follower_username = ?";
        String deleteFollowed = "DELETE FROM followers WHERE followed_username = ?";
        // Rimuoviamo anche post salvati? Beh, se l'utente non esiste più, il suo
        // archivio non serve.
        String deleteSaved = "DELETE FROM saved_posts WHERE username = ?";

        // Propagate keys manually since FKs might be disabled or ON UPDATE CASCADE is
        // missing
        String updatePosts = "UPDATE post SET autore_username = ? WHERE autore_username = ?";
        String updateComments = "UPDATE commenti SET username = ? WHERE username = ?";

        String anonymizeUser = "UPDATE utenti SET username = ?, nome = ?, cognome = ?, password_hash = ?, foto_uri = NULL WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // 1. Delete Likes
                try (PreparedStatement ps = conn.prepareStatement(deleteLikes)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
                // 2. Delete relationships
                try (PreparedStatement ps = conn.prepareStatement(deleteFollower)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(deleteFollowed)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
                // 3. Delete saved posts
                try (PreparedStatement ps = conn.prepareStatement(deleteSaved)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }

                String newUsername = "deleted_" + java.util.UUID.randomUUID().toString().substring(0, 8);
                // 4. Update References (Posts & Comments)
                try (PreparedStatement ps = conn.prepareStatement(updatePosts)) {
                    ps.setString(1, newUsername);
                    ps.setString(2, username);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateComments)) {
                    ps.setString(1, newUsername);
                    ps.setString(2, username);
                    ps.executeUpdate();
                }

                // 5. Anonymize User
                try (PreparedStatement ps = conn.prepareStatement(anonymizeUser)) {
                    // Generiamo un hash valido casuale per impedire login ma evitare errori di
                    // formato
                    String dummyHash = BCrypt.hashpw(java.util.UUID.randomUUID().toString(), BCrypt.gensalt());

                    ps.setString(1, newUsername);
                    ps.setString(2, "Utente");
                    ps.setString(3, "eliminato");
                    ps.setString(4, dummyHash);
                    ps.setString(5, username);
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
        if (tipoStr != null) {
            try {
                u.setTipo(TipoUtente.valueOf(tipoStr));
            } catch (IllegalArgumentException e) {
                u.setTipo(TipoUtente.APPASSIONATO); // Default
            }
        } else {
            u.setTipo(TipoUtente.APPASSIONATO); // Default if null
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
    public java.util.List<Utente> getFollowers(String username) throws SQLException {
        java.util.List<Utente> list = new java.util.ArrayList<>();
        String sql = "SELECT u.* FROM utenti u JOIN followers f ON u.username = f.follower_username WHERE f.followed_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUtente(rs));
                }
            }
        }
        return list;
    }

    @Override
    public java.util.List<Utente> getFollowing(String username) throws SQLException {
        java.util.List<Utente> list = new java.util.ArrayList<>();
        String sql = "SELECT u.* FROM utenti u JOIN followers f ON u.username = f.followed_username WHERE f.follower_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUtente(rs));
                }
            }
        }
        return list;
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
    public int getNumSavedPosts(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM saved_posts WHERE username = ?";
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
        String sql = "SELECT * FROM utenti WHERE username LIKE ? AND username NOT LIKE 'deleted_%'";

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

    @Override
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utenti";
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
    public java.util.List<Utente> findTopActiveUsers(int limit) throws SQLException {
        // Optimization: Replaced heavy aggregation (COUNT posts) which caused timeouts
        // on large datasets.
        // Now returning a simple list of users.
        String sql = "SELECT * FROM utenti WHERE username NOT LIKE 'deleted_%' LIMIT ?";

        java.util.List<Utente> list = new java.util.ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUtente(rs));
                }
            }
        }
        return list;
    }
}
