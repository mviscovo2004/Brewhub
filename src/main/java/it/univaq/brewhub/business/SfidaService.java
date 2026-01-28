package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Sfida;
import it.univaq.brewhub.dao.SfidaDAO;
import it.univaq.brewhub.dao.impl.SfidaDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer per la gestione delle Sfide.
 * <p>
 * Centralizza la logica relativa alle sfide tra utenti.
 * Implementa il pattern Singleton.
 * </p>
 */
public class SfidaService {

    private static SfidaService instance;
    private final SfidaDAO sfidaDAO;

    private SfidaService() {
        this.sfidaDAO = new SfidaDAOImpl();
    }

    public static synchronized SfidaService getInstance() {
        if (instance == null) {
            instance = new SfidaService();
        }
        return instance;
    }

    /**
     * Recupera tutte le sfide attive.
     * 
     * @return Lista di sfide.
     */
    public List<Sfida> getAllChallenges() {
        try {
            return sfidaDAO.findAll();
        } catch (SQLException e) {
            Log.error("Errore recupero sfide", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera una sfida per ID.
     * 
     * @param id ID della sfida.
     * @return La sfida trovata o null.
     */
    public Sfida getChallengeById(int id) {
        try {
            return sfidaDAO.findById(id);
        } catch (SQLException e) {
            Log.error("Errore recupero sfida per ID: " + id, e);
            return null;
        }
    }

    /**
     * Crea una nuova sfida.
     * 
     * @param sfida La sfida da creare.
     * @throws BusinessException Se mancano dati o errore tecnico.
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
            Log.error("Errore creazione sfida", e);
            throw new BusinessException("Impossibile creare la sfida", e);
        }
    }

    /**
     * Aggiunge un partecipante a una sfida.
     * 
     * @param sfidaId  ID della sfida.
     * @param username Username del partecipante.
     * @throws BusinessException Se si verifica un errore.
     */
    public void addParticipant(int sfidaId, String username) throws BusinessException {
        try {
            sfidaDAO.addPartecipante(sfidaId, username);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new BusinessException("Partecipi già a questa sfida", e);
            }
            Log.error("Errore aggiunta partecipante sfida", e);
            throw new BusinessException("Impossibile partecipare alla sfida", e);
        }
    }

    /**
     * Rimuove un partecipante da una sfida.
     * 
     * @param sfidaId  ID della sfida.
     * @param username Username del partecipante.
     * @throws BusinessException Se si verifica un errore.
     */
    public void removeParticipant(int sfidaId, String username) throws BusinessException {
        try {
            sfidaDAO.removePartecipante(sfidaId, username);
        } catch (SQLException e) {
            Log.error("Errore rimozione partecipante sfida", e);
            throw new BusinessException("Impossibile abbandonare la sfida", e);
        }
    }

    /**
     * Verifica se un utente partecipa a una sfida.
     * 
     * @param sfidaId  ID della sfida.
     * @param username Username dell'utente.
     * @return true se l'utente partecipa, false altrimenti.
     */
    public boolean isParticipating(int sfidaId, String username) {
        try {
            return sfidaDAO.isPartecipante(sfidaId, username);
        } catch (SQLException e) {
            Log.error("Errore verifica partecipazione sfida", e);
            return false;
        }
    }

    /**
     * Conta i partecipanti di una sfida.
     * 
     * @param sfidaId ID della sfida.
     * @return Numero di partecipanti.
     */
    public int getParticipantsCount(int sfidaId) {
        try {
            return sfidaDAO.getPartecipantiCount(sfidaId);
        } catch (SQLException e) {
            Log.error("Errore conteggio partecipanti sfida", e);
            return 0;
        }
    }

    /**
     * Elimina una sfida.
     * 
     * @param id ID della sfida da eliminare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteChallenge(int id) throws BusinessException {
        try {
            sfidaDAO.delete(id);
        } catch (SQLException e) {
            Log.error("Errore eliminazione sfida", e);
            throw new BusinessException("Impossibile eliminare la sfida", e);
        }
    }
}
