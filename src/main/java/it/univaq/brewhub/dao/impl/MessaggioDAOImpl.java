package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Messaggio;
import it.univaq.brewhub.dao.MessaggioDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessaggioDAOImpl implements MessaggioDAO {

    @Override
    public void create(Messaggio messaggio) {
        String sql = "INSERT INTO messaggi(sender, receiver, contenuto, timestamp, letto, id_gruppo) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, messaggio.getSender());

            if (messaggio.getReceiver() != null) {
                pstmt.setString(2, messaggio.getReceiver());
            } else {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            }

            pstmt.setString(3, messaggio.getContenuto());
            pstmt.setString(4, messaggio.getTimestamp());
            pstmt.setBoolean(5, messaggio.isLetto());

            if (messaggio.getIdGruppo() != null) {
                pstmt.setInt(6, messaggio.getIdGruppo());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        messaggio.setId(generatedKeys.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Messaggio> getConversazione(String user1, String user2) {
        List<Messaggio> chat = new ArrayList<>();
        // Query specific for private messages (id_gruppo IS NULL)
        String sql = "SELECT * FROM messaggi WHERE id_gruppo IS NULL AND ((sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)) ORDER BY timestamp ASC, id ASC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chat.add(mapResultSetToMessaggio(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chat;
    }

    @Override
    public List<String> getUtentiConversazioni(String user) {
        List<String> utenti = new ArrayList<>();
        // Find users from private messages only
        String sql = "SELECT DISTINCT other_user FROM (" +
                "  SELECT receiver as other_user FROM messaggi WHERE sender = ? AND id_gruppo IS NULL " +
                "  UNION " +
                "  SELECT sender as other_user FROM messaggi WHERE receiver = ? AND id_gruppo IS NULL " +
                ")";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, user);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    utenti.add(rs.getString("other_user"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utenti;
    }

    @Override
    public void segnaComeLetto(int id) {
        String sql = "UPDATE messaggi SET letto = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int contaNonLetti(String receiver) {
        // Count unread for user (ignoring groups for now in general count, or
        // including?
        // Usually notifications are separate. Let's keep it simple.)
        String sql = "SELECT COUNT(*) FROM messaggi WHERE receiver = ? AND letto = 0";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, receiver);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Messaggio> getMessaggiGruppo(int idGruppo) {
        List<Messaggio> chat = new ArrayList<>();
        String sql = "SELECT * FROM messaggi WHERE id_gruppo = ? ORDER BY timestamp ASC, id ASC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idGruppo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chat.add(mapResultSetToMessaggio(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chat;
    }

    private Messaggio mapResultSetToMessaggio(ResultSet rs) throws SQLException {
        Messaggio m = new Messaggio();
        m.setId(rs.getInt("id"));
        m.setSender(rs.getString("sender"));
        m.setReceiver(rs.getString("receiver"));
        m.setContenuto(rs.getString("contenuto"));
        m.setTimestamp(rs.getString("timestamp"));
        m.setLetto(rs.getBoolean("letto"));
        int grp = rs.getInt("id_gruppo");
        if (!rs.wasNull()) {
            m.setIdGruppo(grp);
        }
        return m;
    }

    @Override
    public void deleteConversazione(String user1, String user2) {
        String sql = "DELETE FROM messaggi WHERE id_gruppo IS NULL AND ((sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?))";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
