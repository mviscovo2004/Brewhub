package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Evento;
import it.univaq.brewhub.dao.EventoDAO;
import it.univaq.brewhub.dao.impl.EventoDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer per la gestione degli Eventi.
 * <p>
 * Centralizza la logica relativa agli eventi creati dai Torrefattori
 * e la partecipazione degli utenti.
 * Implementa il pattern Singleton.
 * </p>
 */
public class EventoService {

    private static EventoService instance;
    private final EventoDAO eventoDAO;

    private EventoService() {
        this.eventoDAO = new EventoDAOImpl();
    }

    public static synchronized EventoService getInstance() {
        if (instance == null) {
            instance = new EventoService();
        }
        return instance;
    }

    /**
     * Recupera tutti gli eventi disponibili.
     * 
     * @return Lista di eventi.
     */
    public List<Evento> getAllEvents() {
        try {
            return eventoDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore recupero eventi", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera un evento per ID.
     * 
     * @param id ID dell'evento.
     * @return L'evento trovato o null.
     */
    public Evento getEventById(int id) {
        try {
            return eventoDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore recupero evento per ID: " + id, e);
            return null;
        }
    }

    /**
     * Crea un nuovo evento.
     * 
     * @param evento L'evento da creare.
     * @throws BusinessException Se mancano dati o errore tecnico.
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
            Log.error("Errore creazione evento", e);
            throw new BusinessException("Impossibile creare l'evento", e);
        }
    }

    /**
     * Aggiunge un partecipante a un evento.
     * 
     * @param eventoId ID dell'evento.
     * @param username Username del partecipante.
     * @throws BusinessException Se si verifica un errore.
     */
    public void addParticipant(int eventoId, String username) throws BusinessException {
        try {
            eventoDAO.addPartecipante(eventoId, username);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new BusinessException("Sei già iscritto a questo evento", e);
            }
            Log.error("Errore aggiunta partecipante", e);
            throw new BusinessException("Impossibile iscriversi all'evento", e);
        }
    }

    /**
     * Rimuove un partecipante da un evento.
     * 
     * @param eventoId ID dell'evento.
     * @param username Username del partecipante.
     * @throws BusinessException Se si verifica un errore.
     */
    public void removeParticipant(int eventoId, String username) throws BusinessException {
        try {
            eventoDAO.removePartecipante(eventoId, username);
        } catch (SQLException e) {
            Log.error("Errore rimozione partecipante", e);
            throw new BusinessException("Impossibile annullare la partecipazione", e);
        }
    }

    /**
     * Verifica se un utente partecipa a un evento.
     * 
     * @param eventoId ID dell'evento.
     * @param username Username dell'utente.
     * @return true se l'utente partecipa, false altrimenti.
     */
    public boolean isParticipating(int eventoId, String username) {
        try {
            return eventoDAO.isPartecipante(eventoId, username);
        } catch (SQLException e) {
            Log.error("Errore verifica partecipazione", e);
            return false;
        }
    }

    /**
     * Conta i partecipanti di un evento.
     * 
     * @param eventoId ID dell'evento.
     * @return Numero di partecipanti.
     */
    public int getParticipantsCount(int eventoId) {
        try {
            return eventoDAO.getPartecipantiCount(eventoId);
        } catch (SQLException e) {
            Log.error("Errore conteggio partecipanti", e);
            return 0;
        }
    }

    /**
     * Elimina un evento.
     * 
     * @param id ID dell'evento da eliminare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteEvent(int id) throws BusinessException {
        try {
            eventoDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore eliminazione evento", e);
            throw new BusinessException("Impossibile eliminare l'evento", e);
        }
    }
}
