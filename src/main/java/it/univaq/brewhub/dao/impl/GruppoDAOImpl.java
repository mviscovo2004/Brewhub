package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Gruppo;
import it.univaq.brewhub.dao.GruppoDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GruppoDAOImpl implements GruppoDAO {

    @Override
    public int createGruppo(String nome, String creatore, List<String> membri) {
        String sql = "INSERT INTO gruppi(nome, creatore) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, nome);
            pstmt.setString(2, creatore);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGruppo = generatedKeys.getInt(1);
                    // Add Members
                    addMembro(idGruppo, creatore); // Creator is a member
                    if (membri != null) {
                        for (String m : membri) {
                            if (!m.equals(creatore)) {
                                addMembro(idGruppo, m);
                            }
                        }
                    }
                    return idGruppo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Gruppo getGruppo(int id) {
        String sql = "SELECT * FROM gruppi WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Gruppo g = new Gruppo(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("creatore"));
                    return g;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Gruppo> getGruppiUtente(String username) {
        List<Gruppo> gruppi = new ArrayList<>();
        // Join with membri_gruppo to find groups where user is a member
        String sql = "SELECT g.* FROM gruppi g JOIN membri_gruppo mg ON g.id = mg.id_gruppo WHERE mg.username = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Gruppo g = new Gruppo(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("creatore"));
                    gruppi.add(g);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gruppi;
    }

    @Override
    public void addMembro(int idGruppo, String username) {
        String sql = "INSERT OR IGNORE INTO membri_gruppo(id_gruppo, username) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idGruppo);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMembro(int idGruppo, String username) {
        String sql = "DELETE FROM membri_gruppo WHERE id_gruppo = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idGruppo);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renameGruppo(int id, String nuovoNome) {
        String sql = "UPDATE gruppi SET nome = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuovoNome);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGruppo(int id) {
        // Manually delete dependencies first to be safe (membri_gruppo, messaggi)
        // Note: messaggi table has id_gruppo
        String sqlMembers = "DELETE FROM membri_gruppo WHERE id_gruppo = ?";
        String sqlMsgs = "DELETE FROM messaggi WHERE id_gruppo = ?";
        String sqlGroup = "DELETE FROM gruppi WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            // Delete members
            try (PreparedStatement p1 = conn.prepareStatement(sqlMembers)) {
                p1.setInt(1, id);
                p1.executeUpdate();
            }
            // Delete messages
            try (PreparedStatement p2 = conn.prepareStatement(sqlMsgs)) {
                p2.setInt(1, id);
                p2.executeUpdate();
            }
            // Delete group
            try (PreparedStatement p3 = conn.prepareStatement(sqlGroup)) {
                p3.setInt(1, id);
                p3.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
