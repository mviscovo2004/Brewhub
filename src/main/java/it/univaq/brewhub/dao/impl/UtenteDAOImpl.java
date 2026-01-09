package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.Utente;
import it.univaq.brewhub.Utente.TipoUtente;
import it.univaq.brewhub.dao.UtenteDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementazione dell'interfaccia UtenteDAO.
 */
public class UtenteDAOImpl implements UtenteDAO {

    @Override
    public void create(Utente u) throws SQLException {
        if (findByUsername(u.getUsername()) != null) {
            throw new SQLException("Username esistente");
        }

        String sql = "INSERT INTO utenti(username, nome, cognome, password, tipo, foto_profilo) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getUsername());
            pstmt.setString(2, u.getNome());
            pstmt.setString(3, u.getCognome());

            // Gestione password (se già hashata o no, qui assumiamo che il
            // service/controller passi oggetto pronto o che DAO faccia hash)
            // Dall'implementazione originale, Utente faceva hash nel costruttore o nel
            // metodo.
            // Qui assumiamo che la password nell'oggetto sia quella raw, e facciamo l'hash
            // qui, OPPURE usiamo field pwCrypto.
            // Guardando Utente.java, il costruttore fa l'hash in pwCrypto.
            // Ma il metodo registraUtente faceva BCrypt.hashpw(u.getPassword(), ...).
            // Replichiamo logicamente: u.getPassword() è clear text.
            String hash = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());

            pstmt.setString(4, hash);
            pstmt.setString(5, u.getTipo().name());
            pstmt.setString(6, u.getFotoProfilo());

            pstmt.executeUpdate();
        }
    }

    @Override
    public Utente login(String username, String passwordInserita) throws SQLException {
        String sql = "SELECT * FROM utenti WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(passwordInserita, storedHash)) {
                        return mapResultSetToUtente(rs);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void update(Utente u) throws SQLException {
        // Logica originale: check se password è diversa da hash.
        // Qui per semplicità aggiorniamo tutto. Se la password è cambiata, il chiamante
        // dovrebbe averla settata.
        // Tuttavia, nel metodo aggiornaProfilo originale c'era logica specifica per
        // rilevare il cambio pwd.
        // Replichiamo la logica base: se password != pwCrypto e password != null.

        String sql;
        boolean cambioPassword = u.getPassword() != null && !u.getPassword().equals(u.getPasswordCrypto());

        if (cambioPassword) {
            sql = "UPDATE utenti SET nome = ?, cognome = ?, tipo = ?, password = ?, foto_profilo = ? WHERE username = ?";
        } else {
            sql = "UPDATE utenti SET nome = ?, cognome = ?, tipo = ?, foto_profilo = ? WHERE username = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getNome());
            pstmt.setString(2, u.getCognome());
            pstmt.setString(3, u.getTipo().name());

            if (cambioPassword) {
                String hash = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());
                // Aggiorniamo anche l'oggetto in memoria per consistenza
                u.setPasswordCrypto(hash);
                pstmt.setString(4, hash);
                pstmt.setString(5, u.getFotoProfilo());
                pstmt.setString(6, u.getUsername());
            } else {
                pstmt.setString(4, u.getFotoProfilo());
                pstmt.setString(5, u.getUsername());
            }

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(String username) throws SQLException {
        String sql = "DELETE FROM utenti WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public Utente findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM utenti WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtente(rs);
                }
            }
        }
        return null;
    }

    /**
     * Mappa una riga del ResultSet in un oggetto Utente.
     *
     * @param rs Il ResultSet.
     * @return L'oggetto Utente.
     * @throws SQLException Se si verifica un errore di accesso al database.
     */
    private Utente mapResultSetToUtente(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        u.setUsername(rs.getString("username"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setPasswordCrypto(rs.getString("password")); // Setta hash

        String tipoStr = rs.getString("tipo");
        try {
            u.setTipo(TipoUtente.valueOf(tipoStr));
        } catch (IllegalArgumentException e) {
            u.setTipo(TipoUtente.APPASSIONATO); // Default
        }

        u.setFotoProfilo(rs.getString("foto_profilo"));
        return u;
    }
}
