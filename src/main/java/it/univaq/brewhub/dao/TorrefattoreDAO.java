package it.univaq.brewhub.dao;

import it.univaq.brewhub.model.Torrefattore;
import java.sql.SQLException;

/**
 * Interfaccia DAO per la gestione specifica dei dati relativi ai
 * {@link Torrefattore}.
 * Si occupa della persistenza delle informazioni aziendali aggiuntive.
 */
public interface TorrefattoreDAO {

    /**
     * Salva i dettagli di un nuovo torrefattore.
     *
     * @param torrefattore L'oggetto Torrefattore contenente i dati aziendali.
     * @throws SQLException Se si verifica un errore durante l'inserimento.
     */
    void create(Torrefattore torrefattore) throws SQLException;

    /**
     * Recupera i dettagli completi di un torrefattore dato il suo username.
     *
     * @param username L'username del torrefattore.
     * @return L'oggetto Torrefattore, o null se non trovato.
     * @throws SQLException Se si verifica un errore durante la ricerca.
     */
    Torrefattore findByUsername(String username) throws SQLException;

    /**
     * Aggiorna i dati aziendali di un torrefattore esistente.
     *
     * @param torrefattore L'oggetto con i dati aggiornati.
     * @throws SQLException Se si verifica un errore durante l'aggiornamento.
     */
    void update(Torrefattore torrefattore) throws SQLException;

    /**
     * Elimina i dati specifici del torrefattore.
     * (Nota: L'eliminazione dell'utente base avviene tramite {@link UtenteDAO}).
     *
     * @param username L'username del torrefattore da eliminare.
     * @throws SQLException Se si verifica un errore durante l'eliminazione.
     */
    void delete(String username) throws SQLException;
}
