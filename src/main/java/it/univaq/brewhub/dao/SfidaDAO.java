package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Sfida;
import java.sql.SQLException;
import java.util.List;

public interface SfidaDAO {
    void create(Sfida sfida) throws SQLException;

    List<Sfida> findAll() throws SQLException;

    Sfida findById(int id) throws SQLException;

    void addPartecipante(int sfidaId, String username) throws SQLException;

    void removePartecipante(int sfidaId, String username) throws SQLException;

    boolean isPartecipante(int sfidaId, String username) throws SQLException;

    int getPartecipantiCount(int sfidaId) throws SQLException;

    void delete(int id) throws SQLException;
}
