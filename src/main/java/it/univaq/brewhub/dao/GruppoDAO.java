package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Gruppo;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei Gruppi (Chat/Community).
 */
public interface GruppoDAO {

    /**
     * Crea un nuovo gruppo.
     * @param nome Nome del gruppo.
     * @param creatore Username del creatore.
     * @param membri Lista iniziale dei membri.
     * @return L'ID del gruppo creato.
     */
    int createGruppo(String nome, String creatore, List<String> membri);

    /**
     * Recupera un gruppo per ID.
     * @param id ID del gruppo.
     * @return L'oggetto Gruppo.
     */
    Gruppo getGruppo(int id);

    /**
     * Recupera i gruppi di cui un utente fa parte.
     * @param username Username dell'utente.
     * @return Lista di gruppi.
     */
    List<Gruppo> getGruppiUtente(String username);

    /**
     * Aggiunge un membro a un gruppo.
     * @param idGruppo ID del gruppo.
     * @param username Username da aggiungere.
     */
    void addMembro(int idGruppo, String username);

    /**
     * Rimuove un membro da un gruppo.
     * @param idGruppo ID del gruppo.
     * @param username Username da rimuovere.
     */
    void removeMembro(int idGruppo, String username);

    /**
     * Rinomina un gruppo.
     * @param id ID del gruppo.
     * @param nuovoNome Nuovo nome.
     */
    void renameGruppo(int id, String nuovoNome);

    /**
     * Elimina un gruppo.
     * @param id ID del gruppo.
     */
    void deleteGruppo(int id);
}
