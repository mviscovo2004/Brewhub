package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Messaggio;
import it.univaq.brewhub.dao.MessaggioDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione DAO per i {@link Messaggio}.
 * <p>
 * Gestisce la persistenza e il recupero dei messaggi per chat private e di
 * gruppo,
 * incluse le notifiche push per i nuovi messaggi privati.
 * </p>
 */
public class MessaggioDAOImpl implements MessaggioDAO {

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(Messaggio messaggio) throws SQLException {
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

                // Invia notifica se è un messaggio privato e il mittente non è il destinatario
                // (es. auto-invio)
                if (messaggio.getReceiver() != null && !messaggio.getSender().equals(messaggio.getReceiver())) {
                    it.univaq.brewhub.model.Utente u = new it.univaq.brewhub.model.Utente();
                    u.setUsername(messaggio.getReceiver());
                    String snippet = messaggio.getContenuto();
                    if (snippet.length() > 20)
                        snippet = snippet.substring(0, 20) + "...";
                    String notifMsg = "Nuovo messaggio da " + messaggio.getSender() + ": " + snippet;
                    it.univaq.brewhub.model.Notifica n = new it.univaq.brewhub.model.Notifica(u, notifMsg);
                    try {
                        notificaDAO.create(n);
                    } catch (SQLException ex) {
                        it.univaq.brewhub.utility.Log.error("Errore creazione notifica messaggio", ex);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Messaggio> getConversazione(String user1, String user2) throws SQLException {
        List<Messaggio> chat = new ArrayList<>();
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
        }
        return chat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUtentiConversazioni(String user) throws SQLException {
        List<String> utenti = new ArrayList<>();
        // Unione di chi ha inviato messaggi all'utente e di chi ne ha ricevuti
        // dall'utente
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
        }
        return utenti;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void segnaComeLetto(int id) throws SQLException {
        String sql = "UPDATE messaggi SET letto = 1 WHERE id = ?";
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
    public int contaNonLetti(String receiver) throws SQLException {
        String sql = "SELECT COUNT(*) FROM messaggi WHERE receiver = ? AND letto = 0";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, receiver);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Messaggio> getMessaggiGruppo(int idGruppo) throws SQLException {
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
        }
        return chat;
    }

    /**
     * Mappa un ResultSet in un oggetto Messaggio.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteConversazione(String user1, String user2) throws SQLException {
        String sql = "DELETE FROM messaggi WHERE id_gruppo IS NULL AND ((sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?))";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            pstmt.executeUpdate();
        }
    }
}
