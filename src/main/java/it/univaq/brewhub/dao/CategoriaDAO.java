package it.univaq.brewhub.dao;

import it.univaq.brewhub.Categoria;
import java.sql.SQLException;
import java.util.List;

public interface CategoriaDAO {
    void create(Categoria c) throws SQLException;

    List<Categoria> findAll() throws SQLException;

    void update(Categoria c) throws SQLException;

    void delete(int id) throws SQLException;

    Categoria findById(int id) throws SQLException;

    Categoria findByName(String name) throws SQLException;
}
