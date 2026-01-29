package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Messaggio;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei {@link Messaggio}.
 * Supporta sia le chat private (1-to-1) che le chat di gruppo.
 */
public interface MessaggioDAO {

    /**
     * Salva un nuovo messaggio nel database.
     *
     * @param messaggio L'oggetto Messaggio da persistere.
     */
    void create(Messaggio messaggio) throws SQLException;

    /**
     * Recupera lo storico della conversazione privata tra due utenti.
     *
     * @param user1 Username del primo utente.
     * @param user2 Username del secondo utente.
     * @return Una lista di messaggi ordinati cronologicamente.
     */
    List<Messaggio> getConversazione(String user1, String user2) throws SQLException;

    /**
     * Recupera tutti i messaggi inviati in un determinato gruppo.
     *
     * @param idGruppo L'identificativo del gruppo.
     * @return Una lista di messaggi del gruppo.
     */
    List<Messaggio> getMessaggiGruppo(int idGruppo) throws SQLException;

    /**
     * Ottiene la lista degli username con cui l'utente specificato ha scambiato
     * messaggi.
     *
     * @param user L'username dell'utente.
     * @return Una lista di username unici corrispondenti alle chat attive.
     */
    List<String> getUtentiConversazioni(String user) throws SQLException;

    /**
     * Segna un messaggio specifico come "letto".
     *
     * @param id L'identificativo del messaggio.
     */
    void segnaComeLetto(int id) throws SQLException;

    /**
     * Conta il numero totale di messaggi non letti destinati a un utente.
     *
     * @param receiver L'username del destinatario.
     * @return Il numero di messaggi non letti.
     */
    int contaNonLetti(String receiver) throws SQLException;

    /**
     * Elimina interamente la conversazione privata (storico messaggi) tra due
     * utenti.
     *
     * @param user1 Username del primo utente.
     * @param user2 Username del secondo utente.
     */
    void deleteConversazione(String user1, String user2) throws SQLException;
}
