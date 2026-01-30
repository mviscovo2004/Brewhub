package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.dao.SfidaDAO;
import it.univaq.brewhub.dao.impl.SfidaDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer per la gestione delle sfide (contest).
 * Gestisce la creazione delle sfide, la ricerca e la gestione delle
 * partecipazioni.
 * Implementa il pattern Singleton.
 */
public class SfidaService {

    private static SfidaService instance;
    private final SfidaDAO sfidaDAO;

    /**
     * Costruttore privato.
     */
    private SfidaService() {
        this.sfidaDAO = new SfidaDAOImpl();
    }

    /**
     * Restituisce l'istanza singleton del servizio.
     *
     * @return L'istanza di SfidaService.
     */
    public static synchronized SfidaService getInstance() {
        if (instance == null) {
            instance = new SfidaService();
        }
        return instance;
    }

    /**
     * Recupera tutte le sfide attualmente attive e disponibili.
     *
     * @return Una lista di oggetti Sfida.
     */
    public List<Sfida> getAllChallenges() {
        try {
            return sfidaDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore durante il recupero delle sfide", e);
            return Collections.emptyList();
        }
    }

    /**
     * Cerca le sfide basandosi su una query testuale (titolo o descrizione).
     *
     * @param query La stringa chiave per la ricerca.
     * @return Una lista di sfide che corrispondono ai criteri.
     */
    public List<Sfida> searchChallenges(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String lowerQuery = query.toLowerCase();
        List<Sfida> all = getAllChallenges();
        List<Sfida> result = new java.util.ArrayList<>();
        for (Sfida s : all) {
            boolean matchTitolo = s.getTitolo() != null && s.getTitolo().toLowerCase().contains(lowerQuery);
            boolean matchDesc = s.getDescrizione() != null && s.getDescrizione().toLowerCase().contains(lowerQuery);
            if (matchTitolo || matchDesc) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Recupera i dettagli di una specifica sfida tramite ID.
     *
     * @param id L'identificativo della sfida.
     * @return L'oggetto Sfida, o null in caso di errore.
     */
    public Sfida getChallengeById(int id) {
        try {
            return sfidaDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero della sfida con ID: " + id, e);
            return null;
        }
    }

    /**
     * Crea una nuova sfida.
     *
     * @param sfida L'oggetto Sfida da salvare.
     * @throws BusinessException Se mancano titolo o descrizione, o se si verifica
     *                           un errore.
     */
    public void createChallenge(Sfida sfida) throws BusinessException {
        if (sfida == null || sfida.getTitolo() == null || sfida.getTitolo().isBlank()) {
            throw new BusinessException("Il titolo della sfida è obbligatorio");
        }
        if (sfida.getDescrizione() == null || sfida.getDescrizione().isBlank()) {
            throw new BusinessException("La descrizione della sfida è obbligatoria");
        }
        try {
            sfidaDAO.create(sfida);
        } catch (SQLException e) {
            Log.error("Errore durante la creazione della sfida", e);
            throw new BusinessException("Impossibile creare la sfida", e);
        }
    }

    /**
     * Registra la partecipazione di un utente a una sfida.
     *
     * @param sfidaId  L'ID della sfida.
     * @param username L'username del partecipante.
     * @throws BusinessException Se l'utente partecipa già o si verifica un errore.
     */
    public void addParticipant(int sfidaId, String username) throws BusinessException {
        try {
            sfidaDAO.addPartecipante(sfidaId, username);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new BusinessException("Partecipi già a questa sfida", e);
            }
            Log.error("Errore durante l'aggiunta del partecipante alla sfida", e);
            throw new BusinessException("Impossibile partecipare alla sfida", e);
        }
    }

    /**
     * Rimuove la partecipazione di un utente da una sfida.
     *
     * @param sfidaId  L'ID della sfida.
     * @param username L'username del partecipante.
     * @throws BusinessException Se si verifica un errore durante l'operazione.
     */
    public void removeParticipant(int sfidaId, String username) throws BusinessException {
        try {
            sfidaDAO.removePartecipante(sfidaId, username);
        } catch (SQLException e) {
            Log.error("Errore durante la rimozione del partecipante dalla sfida", e);
            throw new BusinessException("Impossibile abbandonare la sfida", e);
        }
    }

    /**
     * Verifica se un utente sta partecipando a una sfida.
     *
     * @param sfidaId  L'ID della sfida.
     * @param username L'username dell'utente.
     * @return true se l'utente è un partecipante, false altrimenti.
     */
    public boolean isParticipating(int sfidaId, String username) {
        try {
            return sfidaDAO.isPartecipante(sfidaId, username);
        } catch (SQLException e) {
            Log.error("Errore durante la verifica della partecipazione alla sfida", e);
            return false;
        }
    }

    /**
     * Conta il numero totale di partecipanti a una sfida.
     *
     * @param sfidaId L'ID della sfida.
     * @return Il numero di partecipanti.
     */
    public int getParticipantsCount(int sfidaId) {
        try {
            return sfidaDAO.getPartecipantiCount(sfidaId);
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei partecipanti alla sfida", e);
            return 0;
        }
    }

    /**
     * Elimina una sfida dal sistema.
     *
     * @param id L'ID della sfida da eliminare.
     * @throws BusinessException Se si verifica un errore durante l'eliminazione.
     */
    public void deleteChallenge(int id) throws BusinessException {
        try {
            sfidaDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione della sfida", e);
            throw new BusinessException("Impossibile eliminare la sfida", e);
        }
    }
}
