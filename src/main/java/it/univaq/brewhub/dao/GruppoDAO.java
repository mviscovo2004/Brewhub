package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Gruppo;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei {@link Gruppo} di chat.
 * Gestisce la creazione, modifica e cancellazione dei gruppi, nonché la
 * gestione dei membri.
 */
public interface GruppoDAO {

    /**
     * Crea un nuovo gruppo di chat.
     *
     * @param nome     Il nome del gruppo.
     * @param creatore L'username dell'utente che crea il gruppo.
     * @param membri   Una lista iniziale di username da aggiungere come membri.
     * @return L'ID generato del nuovo gruppo.
     */
    int createGruppo(String nome, String creatore, List<String> membri) throws SQLException;

    /**
     * Recupera le informazioni di un gruppo tramite il suo ID.
     *
     * @param id L'identificativo del gruppo.
     * @return L'oggetto Gruppo trovato, o null se non esiste.
     */
    Gruppo getGruppo(int id) throws SQLException;

    /**
     * Recupera la lista di tutti i gruppi di cui un utente è membro.
     *
     * @param username L'username dell'utente.
     * @return Una lista di oggetti Gruppo.
     */
    List<Gruppo> getGruppiUtente(String username) throws SQLException;

    /**
     * Aggiunge un nuovo membro a un gruppo esistente.
     *
     * @param idGruppoL'identificativo del gruppo.
     * @param username                 L'username dell'utente da aggiungere.
     */
    void addMembro(int idGruppo, String username) throws SQLException;

    /**
     * Rimuove un membro da un gruppo.
     *
     * @param idGruppo L'identificativo del gruppo.
     * @param username L'username dell'utente da rimuovere.
     */
    void removeMembro(int idGruppo, String username) throws SQLException;

    /**
     * Rinomina un gruppo esistente.
     *
     * @param id        L'identificativo del gruppo.
     * @param nuovoNome Il nuovo nome da assegnare al gruppo.
     */
    void renameGruppo(int id, String nuovoNome) throws SQLException;

    /**
     * Elimina definitivamente un gruppo e tutti i suoi dati associati.
     *
     * @param id L'identificativo del gruppo da eliminare.
     */
    void deleteGruppo(int id) throws SQLException;
}
