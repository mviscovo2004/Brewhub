package it.univaq.brewhub.dao;

import it.univaq.brewhub.Evento;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione degli Eventi.
 */
public interface EventoDAO {

    /**
     * Crea un nuovo evento.
     * @param evento L'evento da creare.
     * @throws SQLException Errore SQL.
     */
    void create(Evento evento) throws SQLException;

    /**
     * Recupera tutti gli eventi disponibili.
     * @return Lista di eventi.
     * @throws SQLException Errore SQL.
     */
    List<Evento> findAll() throws SQLException;

    /**
     * Cerca un evento per ID.
     * @param id L'ID dell'evento.
     * @return L'evento trovato o null.
     * @throws SQLException Errore SQL.
     */
    Evento findById(int id) throws SQLException;

    /**
     * Registra un utente come partecipante a un evento.
     * @param eventoId ID dell'evento.
     * @param username Username del partecipante.
     * @throws SQLException Errore SQL.
     */
    void addPartecipante(int eventoId, String username) throws SQLException;

    /**
     * Rimuove un utente dai partecipanti di un evento.
     * @param eventoId ID dell'evento.
     * @param username Username da rimuovere.
     * @throws SQLException Errore SQL.
     */
    void removePartecipante(int eventoId, String username) throws SQLException;

    /**
     * Verifica se un utente partecipa a un evento.
     * @param eventoId ID evento.
     * @param username Username.
     * @return true se partecipa, false altrimenti.
     * @throws SQLException Errore SQL.
     */
    boolean isPartecipante(int eventoId, String username) throws SQLException;

    /**
     * Conta i partecipanti a un evento.
     * @param eventoId ID evento.
     * @return Numero di partecipanti.
     * @throws SQLException Errore SQL.
     */
    int getPartecipantiCount(int eventoId) throws SQLException;

    /**
     * Elimina un evento.
     * @param id ID dell'evento.
     * @throws SQLException Errore SQL.
     */
    void delete(int id) throws SQLException;
}