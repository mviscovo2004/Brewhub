package it.univaq.brewhub.dao;

import it.univaq.brewhub.Notifica;
import java.sql.SQLException;
import java.util.List;

public interface NotificaDAO {
    void create(Notifica notifica) throws SQLException;

    List<Notifica> findByUser(String username) throws SQLException;

    void markAsRead(int id) throws SQLException;

    int getUnreadCount(String username) throws SQLException;

    void delete(int id) throws SQLException;

    void deleteAll(String username) throws SQLException;
}
