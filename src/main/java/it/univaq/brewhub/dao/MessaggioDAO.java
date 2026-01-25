package it.univaq.brewhub.dao;

import it.univaq.brewhub.Messaggio;
import java.util.List;

public interface MessaggioDAO {

    /**
     * Crea un nuovo messaggio.
     * 
     * @param messaggio Il messaggio da salvare.
     */
    void create(Messaggio messaggio);

    /**
     * Recupera la conversazione tra due utenti, ordinata per data.
     * 
     * @param user1 Username del primo utente.
     * @param user2 Username del secondo utente.
     * @return Lista di messaggi scambiati tra i due utenti.
     */
    List<Messaggio> getConversazione(String user1, String user2);

    List<Messaggio> getMessaggiGruppo(int idGruppo);

    /**
     * Ritorna una lista di username con cui l'utente ha una conversazione attiva.
     * (Potrebbe ritornare oggetti più complessi in futuro per mostrare l'ultimo
     * messaggio).
     * 
     * @param user Username dell'utente loggato.
     * @return Lista di username unici.
     */
    List<String> getUtentiConversazioni(String user);

    /**
     * Segna un messaggio come letto.
     * 
     * @param id ID del messaggio.
     */
    void segnaComeLetto(int id);

    /**
     * Conta i messaggi non letti per un utente.
     * 
     * @param receiver Username del ricevente.
     * @return Numero di messaggi non letti.
     */
    int contaNonLetti(String receiver);

    void deleteConversazione(String user1, String user2);
}
