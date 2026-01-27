package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.dao.SfidaDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione DAO per le Sfide.
 * <p>Gestisce la creazione di contest, l'adesione dei partecipanti e le relative notifiche.</p>
 */
public class SfidaDAOImpl implements SfidaDAO {

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    @Override
    public void create(Sfida sfida) throws SQLException {
        String sql = "INSERT INTO sfide (titolo, descrizione, premio, scadenza, creatore, partecipanti_count) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, sfida.getTitolo());
            pstmt.setString(2, sfida.getDescrizione());
            pstmt.setString(3, sfida.getPremio());
            pstmt.setString(4, sfida.getScadenza());
            pstmt.setString(5, sfida.getCreatore());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating challenge failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    sfida.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating challenge failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Sfida> findAll() throws SQLException {
        String sql = "SELECT * FROM sfide ORDER BY scadenza ASC";
        List<Sfida> sfide = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Sfida s = new Sfida();
                s.setId(rs.getInt("id"));
                s.setTitolo(rs.getString("titolo"));
                s.setDescrizione(rs.getString("descrizione"));
                s.setPremio(rs.getString("premio"));
                s.setScadenza(rs.getString("scadenza"));
                s.setCreatore(rs.getString("creatore"));
                s.setPartecipantiCount(getPartecipantiCount(s.getId()));
                sfide.add(s);
            }
        }
        return sfide;
    }

    @Override
    public Sfida findById(int id) throws SQLException {
        String sql = "SELECT * FROM sfide WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Sfida s = new Sfida();
                    s.setId(rs.getInt("id"));
                    s.setTitolo(rs.getString("titolo"));
                    s.setDescrizione(rs.getString("descrizione"));
                    s.setPremio(rs.getString("premio"));
                    s.setScadenza(rs.getString("scadenza"));
                    s.setCreatore(rs.getString("creatore"));
                    s.setPartecipantiCount(getPartecipantiCount(s.getId()));
                    return s;
                }
            }
        }
        return null;
    }

    @Override
    public void addPartecipante(int sfidaId, String username) throws SQLException {
        boolean added = false;
        String sql = "INSERT OR IGNORE INTO partecipazioni_sfide (sfida_id, utente_username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sfidaId);
            pstmt.setString(2, username);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                added = true;
            }
        }
        if (added) {
            sendPartecipazioneNotification(sfidaId, username);
        }
    }

    private void sendPartecipazioneNotification(int sfidaId, String username) {
        String owner = null;
        String title = null;
        String sql = "SELECT creatore, titolo FROM sfide WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sfidaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    owner = rs.getString("creatore");
                    title = rs.getString("titolo");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore fetch sfida per notifica: " + e.getMessage());
        }
        if (owner != null && title != null && !owner.equals(username)) {
            try {
                it.univaq.brewhub.Utente u = new it.univaq.brewhub.Utente();
                u.setUsername(owner);
                String msg = username + " ha accettato la tua sfida \"" + title + "\"";
                notificaDAO.create(new it.univaq.brewhub.Notifica(u, msg));
            } catch (SQLException e) {
                System.err.println("Errore creazione notifica sfida: " + e.getMessage());
            }
        }
    }

    @Override
    public void removePartecipante(int sfidaId, String username) throws SQLException {
        String sql = "DELETE FROM partecipazioni_sfide WHERE sfida_id = ? AND utente_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sfidaId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean isPartecipante(int sfidaId, String username) throws SQLException {
        String sql = "SELECT 1 FROM partecipazioni_sfide WHERE sfida_id = ? AND utente_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sfidaId);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int getPartecipantiCount(int sfidaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM partecipazioni_sfide WHERE sfida_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sfidaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM sfide WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}