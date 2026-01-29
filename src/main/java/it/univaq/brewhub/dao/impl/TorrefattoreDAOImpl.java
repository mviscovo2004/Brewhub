package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.model.Torrefattore;
import it.univaq.brewhub.model.Utente.TipoUtente;
import it.univaq.brewhub.dao.TorrefattoreDAO;
import it.univaq.brewhub.dao.UtenteDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementazione DAO specifica per i {@link Torrefattore}.
 * <p>
 * Estende le funzionalità di base dell'utente utilizzando la composizione con
 * {@link UtenteDAO}
 * e gestisce i dati aziendali specifici nella tabella 'torrefattori'.
 * </p>
 */
public class TorrefattoreDAOImpl implements TorrefattoreDAO {

    private final UtenteDAO utenteDAO = new UtenteDAOImpl();

    /**
     * {@inheritDoc}
     * <p>
     * Crea prima l'utente base nella tabella 'utenti' e poi inserisce i dettagli
     * aziendali
     * nella tabella 'torrefattori'. In caso di errore nel secondo passaggio, esegue
     * un rollback manuale.
     * </p>
     */
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
                // Log o ignora, il danno principale è l'eccezione originale
            }
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Mappa il ResultSet in un oggetto Torrefattore.
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String username) throws SQLException {
        // Elimina i dettagli specifici
        String sql = "DELETE FROM torrefattori WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
        // Elimina anche l'utente base per completare l'eliminazione
        utenteDAO.delete(username);
    }
}
