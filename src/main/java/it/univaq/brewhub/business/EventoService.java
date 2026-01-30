package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.dao.EventoDAO;
import it.univaq.brewhub.dao.impl.EventoDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer responsabile della gestione degli eventi e delle
 * partecipazioni.
 * Gestisce la creazione, ricerca e la logica di iscrizione agli eventi.
 * Segue il pattern Singleton.
 */
public class EventoService {

    private static EventoService instance;
    private final EventoDAO eventoDAO;

    /**
     * Costruttore privato.
     */
    private EventoService() {
        this.eventoDAO = new EventoDAOImpl();
    }

    /**
     * Restituisce l'istanza unica di EventoService.
     *
     * @return L'istanza singleton.
     */
    public static synchronized EventoService getInstance() {
        if (instance == null) {
            instance = new EventoService();
        }
        return instance;
    }

    /**
     * Recupera l'elenco di tutti gli eventi disponibili.
     *
     * @return Una lista di Eventi. Restituisce una lista vuota in caso di errore.
     */
    public List<Evento> getAllEvents() {
        try {
            return eventoDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore durante il recupero degli eventi", e);
            return Collections.emptyList();
        }
    }

    /**
     * Cerca eventi filtrandoli per nome o descrizione.
     *
     * @param query La stringa di ricerca.
     * @return Una lista di eventi che corrispondono ai criteri.
     */
    public List<Evento> searchEvents(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String lowerQuery = query.toLowerCase();
        List<Evento> all = getAllEvents();
        List<Evento> result = new java.util.ArrayList<>();
        for (Evento e : all) {
            boolean matchNome = e.getNome() != null && e.getNome().toLowerCase().contains(lowerQuery);
            boolean matchDesc = e.getDescrizione() != null && e.getDescrizione().toLowerCase().contains(lowerQuery);
            if (matchNome || matchDesc) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Recupera i dettagli di un singolo evento tramite ID.
     *
     * @param id L'identificativo dell'evento.
     * @return L'oggetto Evento, o null in caso di errore.
     */
    public Evento getEventById(int id) {
        try {
            return eventoDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dell'evento con ID: " + id, e);
            return null;
        }
    }

    /**
     * Crea un nuovo evento.
     *
     * @param evento L'evento da inserire.
     * @throws BusinessException Se mancano dati obbligatori (nome o data) o si
     *                           verifica un errore.
     */
    public void createEvent(Evento evento) throws BusinessException {
        if (evento == null || evento.getNome() == null || evento.getNome().isBlank()) {
            throw new BusinessException("Il nome dell'evento è obbligatorio");
        }
        if (evento.getData() == null) {
            throw new BusinessException("La data dell'evento è obbligatoria");
        }
        try {
            eventoDAO.create(evento);
        } catch (SQLException e) {
            Log.error("Errore durante la creazione dell'evento", e);
            throw new BusinessException("Impossibile creare l'evento", e);
        }
    }

    /**
     * Iscrive un utente a un evento specifico.
     *
     * @param eventoId L'ID dell'evento.
     * @param username L'username dell'utente che vuole partecipare.
     * @throws BusinessException Se l'utente è già iscritto o si verifica un errore.
     */
    public void addParticipant(int eventoId, String username) throws BusinessException {
        try {
            eventoDAO.addPartecipante(eventoId, username);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new BusinessException("Sei già iscritto a questo evento", e);
            }
            Log.error("Errore durante l'aggiunta del partecipante", e);
            throw new BusinessException("Impossibile iscriversi all'evento", e);
        }
    }

    /**
     * Annulla l'iscrizione di un utente a un evento.
     *
     * @param eventoId L'ID dell'evento.
     * @param username L'username dell'utente che annulla l'iscrizione.
     * @throws BusinessException Se si verifica un errore durante l'operazione.
     */
    public void removeParticipant(int eventoId, String username) throws BusinessException {
        try {
            eventoDAO.removePartecipante(eventoId, username);
        } catch (SQLException e) {
            Log.error("Errore durante la rimozione del partecipante", e);
            throw new BusinessException("Impossibile annullare la partecipazione", e);
        }
    }

    /**
     * Verifica se un utente è attualmente iscritto a un evento.
     *
     * @param eventoId L'ID dell'evento.
     * @param username L'username dell'utente.
     * @return true se l'utente partecipa, false altrimenti.
     */
    public boolean isParticipating(int eventoId, String username) {
        try {
            return eventoDAO.isPartecipante(eventoId, username);
        } catch (SQLException e) {
            Log.error("Errore durante la verifica della partecipazione", e);
            return false;
        }
    }

    /**
     * Restituisce il numero totale di partecipanti a un evento.
     *
     * @param eventoId L'ID dell'evento.
     * @return Il numero di partecipanti.
     */
    public int getParticipantsCount(int eventoId) {
        try {
            return eventoDAO.getPartecipantiCount(eventoId);
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei partecipanti", e);
            return 0;
        }
    }

    /**
     * Elimina definitivamente un evento.
     *
     * @param id L'ID dell'evento da eliminare.
     * @throws BusinessException Se si verifica un errore durante l'eliminazione.
     */
    public void deleteEvent(int id) throws BusinessException {
        try {
            eventoDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione dell'evento", e);
            throw new BusinessException("Impossibile eliminare l'evento", e);
        }
    }
}
