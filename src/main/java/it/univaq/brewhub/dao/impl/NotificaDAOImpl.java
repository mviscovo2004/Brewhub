package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Notifica;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.dao.NotificaDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificaDAOImpl implements NotificaDAO {

    @Override
    public void create(Notifica notifica) throws SQLException {
        String sql = "INSERT INTO notifiche(utente_username, messaggio, letto, data_creazione) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notifica.getUtente().getUsername());
            pstmt.setString(2, notifica.getMessaggio());
            pstmt.setBoolean(3, false);
            pstmt.setString(4, notifica.getDataCreazione().toString());

            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Notifica> findByUser(String username) throws SQLException {
        List<Notifica> result = new ArrayList<>();
        String sql = "SELECT * FROM notifiche WHERE utente_username = ? ORDER BY data_creazione DESC"; // Più recenti
                                                                                                       // prima

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notifica n = new Notifica();
                    n.setId(rs.getInt("id"));

                    Utente u = new Utente();
                    u.setUsername(rs.getString("utente_username"));
                    n.setUtente(u);

                    n.setMessaggio(rs.getString("messaggio"));
                    n.setLetto(rs.getBoolean("letto"));
                    n.setDataCreazione(LocalDateTime.parse(rs.getString("data_creazione")));

                    result.add(n);
                }
            }
        }
        return result;
    }

    @Override
    public void markAsRead(int id) throws SQLException {
        String sql = "UPDATE notifiche SET letto = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public int getUnreadCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifiche WHERE utente_username = ? AND letto = 0";
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
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM notifiche WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
