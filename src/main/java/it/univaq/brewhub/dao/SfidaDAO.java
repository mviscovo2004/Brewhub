package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Sfida;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione delle {@link Sfida} (contest) tra utenti.
 * Gestisce la creazione delle sfide e le iscrizioni dei partecipanti.
 */
public interface SfidaDAO {

    /**
     * Crea una nuova sfida.
     *
     * @param sfida L'oggetto Sfida da salvare.
     * @throws SQLException Se si verifica un errore durante la creazione.
     */
    void create(Sfida sfida) throws SQLException;

    /**
     * Recupera l'elenco di tutte le sfide presenti nel sistema.
     *
     * @return Una lista di sfide.
     * @throws SQLException Se si verifica un errore durante il recupero.
     */
    List<Sfida> findAll() throws SQLException;

    /**
     * Cerca una sfida specifica tramite il suo ID.
     *
     * @param id L'identificativo della sfida.
     * @return La sfida trovata, oppure null se non esiste.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    Sfida findById(int id) throws SQLException;

    /**
     * Aggiunge un utente alla lista dei partecipanti di una sfida.
     *
     * @param sfidaId  L'identificativo della sfida.
     * @param username L'username del partecipante.
     * @throws SQLException Se l'utente è già iscritto o si verifica un errore.
     */
    void addPartecipante(int sfidaId, String username) throws SQLException;

    /**
     * Rimuove un utente dalla lista dei partecipanti di una sfida.
     *
     * @param sfidaId  L'identificativo della sfida.
     * @param username L'username del partecipante da rimuovere.
     * @throws SQLException Se si verifica un errore durante l'operazione.
     */
    void removePartecipante(int sfidaId, String username) throws SQLException;

    /**
     * Verifica se un utente è iscritto a una determinata sfida.
     *
     * @param sfidaId  L'identificativo della sfida.
     * @param username L'username dell'utente.
     * @return true se l'utente partecipa, false altrimenti.
     * @throws SQLException Se si verifica un errore durante la verifica.
     */
    boolean isPartecipante(int sfidaId, String username) throws SQLException;

    /**
     * Calcola il numero totale di partecipanti a una sfida.
     *
     * @param sfidaId L'identificativo della sfida.
     * @return Il numero di partecipanti.
     * @throws SQLException Se si verifica un errore durante il conteggio.
     */
    int getPartecipantiCount(int sfidaId) throws SQLException;

    /**
     * Elimina una sfida dal sistema.
     *
     * @param id L'identificativo della sfida da eliminare.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(int id) throws SQLException;
}
