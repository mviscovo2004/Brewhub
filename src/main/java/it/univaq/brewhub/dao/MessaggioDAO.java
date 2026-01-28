package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Messaggio;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei Messaggi (Chat privata e di gruppo).
 */
public interface MessaggioDAO {

    /**
     * Invia un nuovo messaggio.
     * @param messaggio Il messaggio da salvare.
     */
    void create(Messaggio messaggio);

    /**
     * Recupera la cronologia messaggi tra due utenti.
     * @param user1 Primo utente.
     * @param user2 Secondo utente.
     * @return Lista dei messaggi scambiati.
     */
    List<Messaggio> getConversazione(String user1, String user2);

    /**
     * Recupera i messaggi di un gruppo.
     * @param idGruppo ID del gruppo.
     * @return Lista dei messaggi.
     */
    List<Messaggio> getMessaggiGruppo(int idGruppo);

    /**
     * Ottiene la lista di utenti con cui l'utente corrente ha una conversazione attiva.
     * @param user L'utente corrente.
     * @return Lista di username.
     */
    List<String> getUtentiConversazioni(String user);

    /**
     * Segna un messaggio come letto.
     * @param id ID del messaggio.
     */
    void segnaComeLetto(int id);

    /**
     * Conta i messaggi non letti per un utente.
     * @param receiver Username del destinatario.
     * @return Numero di messaggi non letti.
     */
    int contaNonLetti(String receiver);

    /**
     * Elimina l'intera conversazione tra due utenti.
     * @param user1 Primo utente.
     * @param user2 Secondo utente.
     */
    void deleteConversazione(String user1, String user2);
}
