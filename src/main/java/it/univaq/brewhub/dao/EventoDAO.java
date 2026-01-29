package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Evento;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione degli {@link Evento}.
 * Gestisce la creazione degli eventi, il recupero e la gestione delle
 * partecipazioni degli utenti.
 */
public interface EventoDAO {

    /**
     * Crea un nuovo evento nel database.
     *
     * @param evento L'oggetto Evento da salvare.
     * @throws SQLException Se si verifica un errore durante l'inserimento.
     */
    void create(Evento evento) throws SQLException;

    /**
     * Recupera la lista di tutti gli eventi disponibili.
     *
     * @return Una lista di oggetti Evento.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Evento> findAll() throws SQLException;

    /**
     * Cerca un evento specifico tramite il suo ID.
     *
     * @param id L'identificativo dell'evento.
     * @return L'oggetto Evento trovato, o null se non esiste.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    Evento findById(int id) throws SQLException;

    /**
     * Aggiunge un utente alla lista dei partecipanti di un evento.
     *
     * @param eventoId L'identificativo dell'evento.
     * @param username L'username dell'utente partecipante.
     * @throws SQLException Se l'utente è già iscritto o si verifica un errore.
     */
    void addPartecipante(int eventoId, String username) throws SQLException;

    /**
     * Rimuove un utente dalla lista dei partecipanti di un evento.
     *
     * @param eventoId L'identificativo dell'evento.
     * @param username L'username dell'utente da rimuovere.
     * @throws SQLException Se si verifica un errore durante l'operazione.
     */
    void removePartecipante(int eventoId, String username) throws SQLException;

    /**
     * Verifica se un utente è iscritto a un determinato evento.
     *
     * @param eventoId L'identificativo dell'evento.
     * @param username L'username dell'utente.
     * @return true se l'utente partecipa, false altrimenti.
     * @throws SQLException Se si verifica un errore durante la verifica.
     */
    boolean isPartecipante(int eventoId, String username) throws SQLException;

    /**
     * Restituisce il numero totale di partecipanti iscritti a un evento.
     *
     * @param eventoId L'identificativo dell'evento.
     * @return Il numero di partecipanti.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int getPartecipantiCount(int eventoId) throws SQLException;

    /**
     * Elimina un evento dal sistema.
     *
     * @param id L'identificativo dell'evento da eliminare.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(int id) throws SQLException;
}
