package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Torrefattore;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.dao.TorrefattoreDAO;
import it.univaq.brewhub.dao.UtenteDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementazione DAO specifica per i Torrefattori.
 * <p>
 * Estende le funzionalità di base dell'utente gestendo i dati aggiuntivi
 * aziendali
 * nella tabella 'torrefattori'. Utilizza la composizione con {@link UtenteDAO}.
 * </p>
 */
public class TorrefattoreDAOImpl implements TorrefattoreDAO {

    private final UtenteDAO utenteDAO = new UtenteDAOImpl();

    @Override
    public void create(Torrefattore t) throws SQLException {
        // 1. Crea l'utente base
        t.setTipo(TipoUtente.TORREFATTORE);
        utenteDAO.create(t);

        // 2. Inserisci i dettagli specifici
        String sql = "INSERT INTO torrefattori(username, nome_azienda, partita_iva, indirizzo, descrizione) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getUsername());
            pstmt.setString(2, t.getNomeAzienda());
            pstmt.setString(3, t.getPartitaIva());
            pstmt.setString(4, t.getIndirizzo());
            pstmt.setString(5, t.getDescrizione());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Rollback manuale: se fallisce l'inserimento dettagli, elimina l'utente creato
            try {
                utenteDAO.delete(t.getUsername());
            } catch (SQLException ex) {
                // Log o ignora, il danno è già fatto
            }
            throw e;
        }
    }

    @Override
    public Torrefattore findByUsername(String username) throws SQLException {
        // Query in JOIN per recuperare tutti i dati (base + estesi)
        String sql = "SELECT u.*, t.nome_azienda, t.partita_iva, t.indirizzo, t.descrizione " +
                "FROM utenti u " +
                "JOIN torrefattori t ON u.username = t.username " +
                "WHERE u.username = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTorrefattore(rs);
                }
            }
        }
        return null;
    }

    private Torrefattore mapResultSetToTorrefattore(ResultSet rs) throws SQLException {
        Torrefattore t = new Torrefattore();
        // Mappatura campi base
        t.setUsername(rs.getString("username"));
        t.setNome(rs.getString("nome"));
        t.setCognome(rs.getString("cognome"));
        t.setPasswordCrypto(rs.getString("password_hash"));
        t.setFotoProfilo(rs.getString("foto_uri"));
        t.setTipo(TipoUtente.TORREFATTORE);

        // Mappatura campi specifici
        t.setNomeAzienda(rs.getString("nome_azienda"));
        t.setPartitaIva(rs.getString("partita_iva"));
        t.setIndirizzo(rs.getString("indirizzo"));
        t.setDescrizione(rs.getString("descrizione"));

        return t;
    }

    @Override
    public void update(Torrefattore t) throws SQLException {
        // Aggiorna tabella base
        utenteDAO.update(t);

        // Aggiorna tabella specifica
        String sql = "UPDATE torrefattori SET nome_azienda = ?, partita_iva = ?, indirizzo = ?, descrizione = ? WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getNomeAzienda());
            pstmt.setString(2, t.getPartitaIva());
            pstmt.setString(3, t.getIndirizzo());
            pstmt.setString(4, t.getDescrizione());
            pstmt.setString(5, t.getUsername());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(String username) throws SQLException {
        // Elimina i dettagli specifici
        String sql = "DELETE FROM torrefattori WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
        // Elimina anche l'utente base per garantire consistenza (e passare il test di
        // eliminazione)
        utenteDAO.delete(username);
    }
}