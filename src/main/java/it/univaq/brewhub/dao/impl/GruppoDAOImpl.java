package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Gruppo;
import it.univaq.brewhub.dao.GruppoDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia {@link GruppoDAO}.
 * Gestisce la persistenza dei gruppi di chat e l'invio di notifiche ai membri
 * aggiunti.
 */
public class GruppoDAOImpl implements GruppoDAO {

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    /**
     * {@inheritDoc}
     * <p>
     * L'operazione è transazionale: crea il gruppo e associa i membri in un'unica
     * transazione.
     * Invia notifiche ai membri (escluso il creatore) dopo il commit.
     * </p>
     */
    @Override
    public int createGruppo(String nome, String creatore, List<String> membri) throws SQLException {
        String sql = "INSERT INTO gruppi(nome, creatore) VALUES(?, ?)";
        String sqlMember = "INSERT OR IGNORE INTO membri_gruppo(id_gruppo, username) VALUES(?, ?)";
        int idGruppo = -1;
        List<String> recipients = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Inizio transazione
            try {
                // 1. Crea il gruppo
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, nome);
                    pstmt.setString(2, creatore);
                    pstmt.executeUpdate();
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            idGruppo = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creazione gruppo fallita, nessun ID ottenuto.");
                        }
                    }
                }

                // 2. Aggiungi i membri
                if (idGruppo != -1) {
                    try (PreparedStatement pstmtMember = conn.prepareStatement(sqlMember)) {
                        // Aggiungi creatore
                        pstmtMember.setInt(1, idGruppo);
                        pstmtMember.setString(2, creatore);
                        pstmtMember.executeUpdate();

                        // Aggiungi altri membri
                        if (membri != null) {
                            for (String m : membri) {
                                if (!m.equals(creatore)) {
                                    pstmtMember.setInt(1, idGruppo);
                                    pstmtMember.setString(2, m);
                                    pstmtMember.executeUpdate();
                                    recipients.add(m);
                                }
                            }
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        // Notifiche (fuori dalla transazione DB principale)
        for (String recipient : recipients) {
            sendGroupNotification(recipient, nome);
        }
        return idGruppo;
    }

    /**
     * Invia una notifica a un utente aggiunto a un gruppo.
     *
     * @param username  L'username dell'utente.
     * @param groupName Il nome del gruppo.
     */
    private void sendGroupNotification(String username, String groupName) {
        try {
            it.univaq.brewhub.model.Utente u = new it.univaq.brewhub.model.Utente();
            u.setUsername(username);
            String msg = "Sei stato aggiunto al gruppo \"" + groupName + "\"";
            notificaDAO.create(new it.univaq.brewhub.model.Notifica(u, msg));
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore notifica gruppo", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Gruppo getGruppo(int id) throws SQLException {
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
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Gruppo> getGruppiUtente(String username) throws SQLException {
        List<Gruppo> gruppi = new ArrayList<>();
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
        }
        return gruppi;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMembro(int idGruppo, String username) throws SQLException {
        boolean added = false;
        String sql = "INSERT OR IGNORE INTO membri_gruppo(id_gruppo, username) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idGruppo);
            pstmt.setString(2, username);
            int rows = pstmt.executeUpdate();
            if (rows > 0)
                added = true;
        }
        if (added) {
            Gruppo g = getGruppo(idGruppo);
            if (g != null) {
                sendGroupNotification(username, g.getNome());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMembro(int idGruppo, String username) throws SQLException {
        String sql = "DELETE FROM membri_gruppo WHERE id_gruppo = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idGruppo);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renameGruppo(int id, String nuovoNome) throws SQLException {
        String sql = "UPDATE gruppi SET nome = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuovoNome);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteGruppo(int id) throws SQLException {
        String sqlMembers = "DELETE FROM membri_gruppo WHERE id_gruppo = ?";
        String sqlMsgs = "DELETE FROM messaggi WHERE id_gruppo = ?";
        String sqlGroup = "DELETE FROM gruppi WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement p1 = conn.prepareStatement(sqlMembers)) {
                    p1.setInt(1, id);
                    p1.executeUpdate();
                }
                try (PreparedStatement p2 = conn.prepareStatement(sqlMsgs)) {
                    p2.setInt(1, id);
                    p2.executeUpdate();
                }
                try (PreparedStatement p3 = conn.prepareStatement(sqlGroup)) {
                    p3.setInt(1, id);
                    p3.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
