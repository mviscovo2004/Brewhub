package it.univaq.brewhub.dao;

import it.univaq.brewhub.Evento;
import java.sql.SQLException;
import java.util.List;

public interface EventoDAO {
    void create(Evento evento) throws SQLException;

    List<Evento> findAll() throws SQLException;

    Evento findById(int id) throws SQLException;

    void addPartecipante(int eventoId, String username) throws SQLException;

    void removePartecipante(int eventoId, String username) throws SQLException;

    boolean isPartecipante(int eventoId, String username) throws SQLException;

    int getPartecipantiCount(int eventoId) throws SQLException;

    void delete(int id) throws SQLException;
}
