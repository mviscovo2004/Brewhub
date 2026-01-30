package it.univaq.brewhub.dao.impl;

import it.univaq.brewhub.model.Categoria;
import it.univaq.brewhub.utility.DatabaseManager;
import it.univaq.brewhub.dao.CategoriaDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia {@link CategoriaDAO}.
 * Gestisce l'interazione con il database per le operazioni CRUD sulle
 * categorie.
 */
public class CategoriaDAOImpl implements CategoriaDAO {

    /**
     * {@inheritDoc}
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
                throw new SQLException("Creazione categoria fallita, nessuna riga modificata.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    c.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
