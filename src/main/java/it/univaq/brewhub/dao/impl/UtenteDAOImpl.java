package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Utente.TipoUtente;
import it.univaq.brewhub.dao.UtenteDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementazione dell'interfaccia {@link UtenteDAO}.
 * <p>
 * Gestisce le operazioni CRUD sugli utenti, l'autenticazione sicura tramite
 * BCrypt,
 * le relazioni sociali (follower/following) e l'archivio dei post salvati.
 * </p>
 */
public class UtenteDAOImpl implements UtenteDAO {

    /**
     * {@inheritDoc}
     * <p>
     * La password viene hashata con BCrypt prima di essere salvata nel database.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Se l'oggetto Utente contiene una password diversa dall'hash memorizzato,
     * viene generato un nuovo hash e aggiornato nel database.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una cancellazione logica "Soft Delete" e anonimizzazione dei dati per
     * mantenere l'integrità referenziale.
     * </p>
     * <ol>
     * <li>Elimina relazioni, like e post salvati.</li>
     * <li>Anonimizza post e commenti assegnandoli a un utente fittizio
     * ('deleted_...').</li>
     * <li>Anonimizza i dati personali dell'utente rendendolo inattivo.</li>
     * </ol>
     */
    @Override
    public void delete(String username) throws SQLException {
        String deleteLikes = "DELETE FROM likes WHERE username = ?";
        String deleteFollower = "DELETE FROM followers WHERE follower_username = ?";
        String deleteFollowed = "DELETE FROM followers WHERE followed_username = ?";
        String deleteSaved = "DELETE FROM saved_posts WHERE username = ?";

        String anonymizeUser = "UPDATE utenti SET username = ?, nome = ?, cognome = ?, password_hash = ?, foto_uri = NULL WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // 1. Pulizia relazioni
                try (PreparedStatement ps = conn.prepareStatement(deleteLikes)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(deleteFollower)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(deleteFollowed)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(deleteSaved)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }

                String newUsername = "deleted_" + java.util.UUID.randomUUID().toString().substring(0, 8);

                // 2. Anonimizzazione utente (e cascata automatica su post, commenti, ecc. via
                // ON UPDATE CASCADE)
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

    /**
     * {@inheritDoc}
     */
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
     * Metodo helper per convertire una riga del ResultSet in un oggetto Utente.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void follow(String follower, String followed) throws SQLException {
        if (follower.equals(followed))
            return;

        it.univaq.brewhub.model.Notifica notificationToSend = null;
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
                notificationToSend = new it.univaq.brewhub.model.Notifica(ricevente,
                        follower + " ha iniziato a seguirti.");
            }
        }

        // Invia notifica dopo aver chiuso la connessione precedente
        if (notificationToSend != null) {
            notificaDAO.create(notificationToSend);
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.List<it.univaq.brewhub.model.Post> getArchive(String username) throws SQLException {
        it.univaq.brewhub.dao.PostDAO postDAO = new it.univaq.brewhub.dao.impl.PostDAOImpl();
        return postDAO.findSavedBy(username);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.List<Utente> findTopActiveUsers(int limit) throws SQLException {
        // Find users with most posts
        String sql = "SELECT u.*, COUNT(p.id) as post_count " +
                "FROM utenti u " +
                "LEFT JOIN post p ON u.username = p.autore_username " +
                "WHERE u.username NOT LIKE 'deleted_%' " +
                "GROUP BY u.username " +
                "ORDER BY post_count DESC " +
                "LIMIT ?";

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
