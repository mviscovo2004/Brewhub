package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.Categoria;
import it.univaq.brewhub.DatabaseManager;
import it.univaq.brewhub.dao.CategoriaDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione DAO per le Categorie.
 * <p>Gestisce le operazioni CRUD per le categorie dei post.</p>
 */
public class CategoriaDAOImpl implements CategoriaDAO {

    /**
     * Crea una nuova categoria nel database.
     * @param c La categoria da creare.
     * @throws SQLException Errore SQL o se la creazione fallisce.
     */
    @Override
    public void create(Categoria c) throws SQLException {
        String sql = "INSERT INTO categorie(nome, icona) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, c.getNome());
            pstmt.setString(2, c.getIcona());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    c.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Recupera tutte le categorie presenti nel database.
     * @return Lista di categorie.
     * @throws SQLException Errore SQL.
     */
    @Override
    public List<Categoria> findAll() throws SQLException {
        List<Categoria> list = new ArrayList<>();
        String sql = "SELECT * FROM categorie";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Categoria(rs.getInt("id"), rs.getString("nome"), rs.getString("icona")));
            }
        }
        return list;
    }

    /**
     * Aggiorna i dati di una categoria esistente.
     * @param c La categoria con i dati aggiornati.
     * @throws SQLException Errore SQL.
     */
    @Override
    public void update(Categoria c) throws SQLException {
        String sql = "UPDATE categorie SET nome = ?, icona = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getNome());
            pstmt.setString(2, c.getIcona());
            pstmt.setInt(3, c.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Elimina una categoria dal database in base all'ID.
     * @param id L'ID della categoria da eliminare.
     * @throws SQLException Errore SQL.
     */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Cerca una categoria tramite il suo ID.
     * @param id L'ID della categoria.
     * @return La categoria trovata o null se non esiste.
     * @throws SQLException Errore SQL.
     */
    @Override
    public Categoria findById(int id) throws SQLException {
        String sql = "SELECT * FROM categorie WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id"), rs.getString("nome"), rs.getString("icona"));
                }
            }
        }
        return null;
    }

    /**
     * Cerca una categoria tramite il suo nome esatto.
     * @param name Il nome della categoria.
     * @return La categoria trovata o null se non esiste.
     * @throws SQLException Errore SQL.
     */
    @Override
    public Categoria findByName(String name) throws SQLException {
        String sql = "SELECT * FROM categorie WHERE nome = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id"), rs.getString("nome"), rs.getString("icona"));
                }
            }
        }
        return null;
    }
}