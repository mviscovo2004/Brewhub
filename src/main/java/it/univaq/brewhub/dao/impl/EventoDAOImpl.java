package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.dao.EventoDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia {@link EventoDAO}.
 * Gestisce la persistenza degli eventi, le iscrizioni dei partecipanti e
 * l'invio di notifiche agli organizzatori.
 */
public class EventoDAOImpl implements EventoDAO {

    private final it.univaq.brewhub.dao.impl.NotificaDAOImpl notificaDAO = new it.univaq.brewhub.dao.impl.NotificaDAOImpl();

    /**
     * {@inheritDoc}
     */
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
                throw new SQLException("Creazione evento fallita, nessuna riga modificata.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evento.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creazione evento fallita, nessun ID ottenuto.");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
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
                e.setPartecipantiCount(getPartecipantiCount(e.getId()));
                eventi.add(e);
            }
        }
        return eventi;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Se l'iscrizione avviene con successo, viene inviata una notifica
     * all'organizzatore dell'evento.
     * </p>
     */
    @Override
    public void addPartecipante(int eventoId, String username) throws SQLException {
        boolean added = false;
        String sql = "INSERT OR IGNORE INTO partecipazioni (evento_id, utente_username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventoId);
            pstmt.setString(2, username);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                added = true;
            }
        }
        if (added) {
            sendPartecipazioneNotification(eventoId, username);
        }
    }

    /**
     * Helper per inviare la notifica di nuova partecipazione all'organizzatore.
     *
     * @param eventoId L'ID dell'evento.
     * @param username L'username del nuovo partecipante.
     */
    private void sendPartecipazioneNotification(int eventoId, String username) {
        String owner = null;
        String nomeEvento = null;
        String sql = "SELECT organizzatore, nome FROM eventi WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    owner = rs.getString("organizzatore");
                    nomeEvento = rs.getString("nome");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero evento per notifica: " + e.getMessage());
        }
        if (owner != null && nomeEvento != null && !owner.equals(username)) {
            try {
                it.univaq.brewhub.model.Utente u = new it.univaq.brewhub.model.Utente();
                u.setUsername(owner);
                String msg = username + " si è iscritto al tuo evento \"" + nomeEvento + "\"";
                notificaDAO.create(new it.univaq.brewhub.model.Notifica(u, msg));
            } catch (SQLException e) {
                System.err.println("Errore creazione notifica evento: " + e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
