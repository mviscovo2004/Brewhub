package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Evento;
import it.univaq.brewhub.dao.EventoDAO;
import it.univaq.brewhub.utility.Log;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventoDAOImpl implements EventoDAO {

    @Override
    public void create(Evento evento) throws SQLException {
        String sql = "INSERT INTO eventi (nome, descrizione, data, luogo, organizzatore) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, evento.getNome());
            pstmt.setString(2, evento.getDescrizione());
            pstmt.setString(3, evento.getData());
            pstmt.setString(4, evento.getLuogo());
            pstmt.setString(5, evento.getOrganizzatore());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating event failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evento.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating event failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Evento> findAll() throws SQLException {
        String sql = "SELECT * FROM eventi ORDER BY data ASC";
        List<Evento> eventi = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Evento e = new Evento();
                e.setId(rs.getInt("id"));
                e.setNome(rs.getString("nome"));
                e.setDescrizione(rs.getString("descrizione"));
                e.setData(rs.getString("data"));
                e.setLuogo(rs.getString("luogo"));
                e.setOrganizzatore(rs.getString("organizzatore"));
                e.setPartecipantiCount(getPartecipantiCount(e.getId())); // N+1 query problem, acceptable for small
                                                                         // scale
                eventi.add(e);
            }
        }
        return eventi;
    }

    @Override
    public Evento findById(int id) throws SQLException {
        String sql = "SELECT * FROM eventi WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Evento e = new Evento();
                    e.setId(rs.getInt("id"));
                    e.setNome(rs.getString("nome"));
                    e.setDescrizione(rs.getString("descrizione"));
                    e.setData(rs.getString("data"));
                    e.setLuogo(rs.getString("luogo"));
                    e.setOrganizzatore(rs.getString("organizzatore"));
                    e.setPartecipantiCount(getPartecipantiCount(e.getId()));
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public void addPartecipante(int eventoId, String username) throws SQLException {
        String sql = "INSERT OR IGNORE INTO partecipazioni (evento_id, utente_username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventoId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void removePartecipante(int eventoId, String username) throws SQLException {
        String sql = "DELETE FROM partecipazioni WHERE evento_id = ? AND utente_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventoId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean isPartecipante(int eventoId, String username) throws SQLException {
        String sql = "SELECT 1 FROM partecipazioni WHERE evento_id = ? AND utente_username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventoId);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int getPartecipantiCount(int eventoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM partecipazioni WHERE evento_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventoId);
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
        String sql = "DELETE FROM eventi WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
