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
 * Implementazione concreta del Data Access Object (DAO) per l'entità Utente.
 * <p>
 * Gestisce tutte le operazioni CRUD (Create, Read, Update, Delete) verso il database SQLite,
 * oltre alla gestione delle relazioni (follower/following), archivio post e autenticazione.
 * Utilizza BCrypt per l'hashing sicuro delle password.
 * </p>
 */
public class UtenteDAOImpl implements UtenteDAO {

    /**
     * Crea un nuovo utente nel database.
     * <p>La password viene automaticamente cifrata con BCrypt prima del salvataggio.</p>
     * 
     * @param u L'oggetto Utente da persistere.
     * @throws SQLException Se lo username esiste già o per altri errori SQL.
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
     * Verifica le credenziali di accesso.
     * 
     * @param username Lo username fornito.
     * @param passwordInserita La password in chiaro fornita.
     * @return L'oggetto {@link Utente} se le credenziali sono valide, altrimenti null.
     * @throws SQLException In caso di errore durante la query.
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
     * Aggiorna i dati di un utente esistente.
     * <p>Gestisce automaticamente l'aggiornamento della password (con nuovo hash) se modificata.</p>
     * 
     * @param u L'oggetto Utente con i dati aggiornati.
     * @throws SQLException In caso di errore SQL.
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
     * Elimina logicamente (Soft Delete) o fisicamente un utente.
     * <p>
     * Esegue le seguenti operazioni in transazione:
     * 1. Elimina Like e Relazioni (Follower/Following).
     * 2. Elimina i post salvati.
     * 3. Anonimizza i post e i commenti dell'utente (assegnandoli a un utente 'deleted_...').
     * 4. Anonimizza il record utente stesso rendendolo inattivo.
     * </p>
     * 
     * @param username Lo username dell'utente da eliminare.
     * @throws SQLException In caso di errore durante la transazione.
     */
    @Override
    public void delete(String username) throws SQLException {
        String deleteLikes = "DELETE FROM likes WHERE username = ?";
        String deleteFollower = "DELETE FROM followers WHERE follower_username = ?";
        String deleteFollowed = "DELETE FROM followers WHERE followed_username = ?";
        String deleteSaved = "DELETE FROM saved_posts WHERE username = ?";

        String updatePosts = "UPDATE post SET autore_username = ? WHERE autore_username = ?";
        String updateComments = "UPDATE commenti SET username = ? WHERE username = ?";

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
                
                // 2. Anonimizzazione contenuti
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

                // 3. Anonimizzazione utente
                try (PreparedStatement ps = conn.prepareStatement(anonymizeUser)) {
                    // Generiamo un hash valido casuale per impedire login ma evitare errori di formato
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
     * Cerca un utente tramite username esatto.
     * 
     * @param username Lo username da cercare.
     * @return L'oggetto Utente se trovato, null altrimenti.
     * @throws SQLException Errore SQL.
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
     * Gestisce l'operazione di 'follow' tra due utenti.
     * <p>Crea inoltre una notifica per l'utente seguito.</p>
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
     * Rimuove il follow tra due utenti.
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
     * Verifica se un utente ne segue un altro.
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
     * Conta il numero di follower di un utente.
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
     * Conta il numero di utenti seguiti (following) da un utente.
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
     * Recupera la lista completa dei follower.
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
     * Recupera la lista completa degli utenti seguiti.
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
     * Salva un post nell'archivio dell'utente.
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
     * Rimuove un post dall'archivio dell'utente.
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
     * Verifica se un post è nell'archivio.
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
     * Conta i post salvati.
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
     * Recupera l'intero archivio dei post salvati.
     */
    @Override
    public java.util.List<it.univaq.brewhub.model.Post> getArchive(String username) throws SQLException {
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

        java.util.List<it.univaq.brewhub.model.Post> posts = new java.util.ArrayList<>();
        it.univaq.brewhub.dao.PostDAO postDAO = new it.univaq.brewhub.dao.impl.PostDAOImpl();
        for (int id : ids) {
            it.univaq.brewhub.model.Post p = postDAO.findById(id);
            if (p != null)
                posts.add(p);
        }
        return posts;
    }

    /**
     * Cerca utenti tramite corrispondenza parziale dello username.
     * <p>Esclude automaticamente gli utenti eliminati logicamente.</p>
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
     * Trova gli utenti più attivi basandosi sul numero di post pubblicati.
     * 
     * @param limit Numero massimo di utenti da restituire.
     * @return Lista degli utenti top contributors.
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
