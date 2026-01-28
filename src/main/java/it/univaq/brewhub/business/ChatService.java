package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Gruppo;
import it.univaq.brewhub.model.Messaggio;
import it.univaq.brewhub.dao.GruppoDAO;
import it.univaq.brewhub.dao.MessaggioDAO;
import it.univaq.brewhub.dao.impl.GruppoDAOImpl;
import it.univaq.brewhub.dao.impl.MessaggioDAOImpl;
import java.util.List;

/**
 * Service Layer per la gestione della Chat (Messaggi e Gruppi).
 * <p>
 * Centralizza la logica relativa ai messaggi privati e di gruppo.
 * Implementa il pattern Singleton.
 * </p>
 */
public class ChatService {

    private static ChatService instance;
    private final MessaggioDAO messaggioDAO;
    private final GruppoDAO gruppoDAO;

    private ChatService() {
        this.messaggioDAO = new MessaggioDAOImpl();
        this.gruppoDAO = new GruppoDAOImpl();
    }

    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    // ==================== MESSAGGI ====================

    /**
     * Invia un messaggio (privato o di gruppo).
     * 
     * @param messaggio Il messaggio da inviare.
     * @throws BusinessException Se mancano dati o errore tecnico.
     */
    public void sendMessage(Messaggio messaggio) throws BusinessException {
        if (messaggio == null || messaggio.getContenuto() == null || messaggio.getContenuto().isBlank()) {
            throw new BusinessException("Il testo del messaggio è obbligatorio");
        }
        if (messaggio.getSender() == null) {
            throw new BusinessException("Il mittente è obbligatorio");
        }
        // Verifica che sia specificato almeno un destinatario (privato) o un gruppo
        if (messaggio.getReceiver() == null && messaggio.getIdGruppo() == null) {
            throw new BusinessException("Specificare un destinatario o un gruppo");
        }

        messaggioDAO.create(messaggio);
    }

    /**
     * Recupera i messaggi di una conversazione privata.
     * 
     * @param user1 Primo utente.
     * @param user2 Secondo utente.
     * @return Lista di messaggi.
     */
    public List<Messaggio> getPrivateMessages(String user1, String user2) {
        return messaggioDAO.getConversazione(user1, user2);
    }

    /**
     * Recupera i messaggi di un gruppo.
     * 
     * @param groupId ID del gruppo.
     * @return Lista di messaggi.
     */
    public List<Messaggio> getGroupMessages(int groupId) {
        return messaggioDAO.getMessaggiGruppo(groupId);
    }

    /**
     * Recupera gli utenti con cui un utente ha conversazioni attive.
     * 
     * @param username Username dell'utente.
     * @return Lista di username.
     */
    public List<String> getActiveConversations(String username) {
        return messaggioDAO.getUtentiConversazioni(username);
    }

    /**
     * Marca un messaggio come letto.
     * 
     * @param messageId ID del messaggio.
     */
    public void markAsRead(int messageId) {
        messaggioDAO.segnaComeLetto(messageId);
    }

    /**
     * Conta i messaggi non letti per un utente.
     * 
     * @param username Username dell'utente.
     * @return Numero di messaggi non letti.
     */
    public int getUnreadCount(String username) {
        return messaggioDAO.contaNonLetti(username);
    }

    /**
     * Elimina una conversazione privata.
     * 
     * @param user1 Primo utente.
     * @param user2 Secondo utente.
     */
    public void deleteConversation(String user1, String user2) {
        messaggioDAO.deleteConversazione(user1, user2);
    }

    // ==================== GRUPPI ====================

    /**
     * Crea un nuovo gruppo.
     * 
     * @param nome     Nome del gruppo.
     * @param creatore Username del creatore.
     * @param membri   Lista iniziale dei membri.
     * @return ID del gruppo creato.
     * @throws BusinessException Se mancano dati o errore tecnico.
     */
    public int createGroup(String nome, String creatore, List<String> membri) throws BusinessException {
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("Il nome del gruppo è obbligatorio");
        }
        if (creatore == null) {
            throw new BusinessException("Il creatore del gruppo è obbligatorio");
        }
        return gruppoDAO.createGruppo(nome, creatore, membri);
    }

    /**
     * Recupera tutti i gruppi di cui un utente fa parte.
     * 
     * @param username Username dell'utente.
     * @return Lista di gruppi.
     */
    public List<Gruppo> getUserGroups(String username) {
        return gruppoDAO.getGruppiUtente(username);
    }

    /**
     * Recupera un gruppo per ID.
     * 
     * @param id ID del gruppo.
     * @return Il gruppo trovato o null.
     */
    public Gruppo getGroupById(int id) {
        return gruppoDAO.getGruppo(id);
    }

    /**
     * Aggiunge un membro a un gruppo.
     * 
     * @param groupId  ID del gruppo.
     * @param username Username del membro.
     */
    public void addGroupMember(int groupId, String username) {
        gruppoDAO.addMembro(groupId, username);
    }

    /**
     * Rimuove un membro da un gruppo.
     * 
     * @param groupId  ID del gruppo.
     * @param username Username del membro.
     */
    public void removeGroupMember(int groupId, String username) {
        gruppoDAO.removeMembro(groupId, username);
    }

    /**
     * Rinomina un gruppo.
     * 
     * @param groupId ID del gruppo.
     * @param newName Nuovo nome.
     * @throws BusinessException Se il nome è vuoto.
     */
    public void renameGroup(int groupId, String newName) throws BusinessException {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException("Il nome del gruppo non può essere vuoto");
        }
        gruppoDAO.renameGruppo(groupId, newName);
    }

    /**
     * Elimina un gruppo.
     * 
     * @param id ID del gruppo da eliminare.
     */
    public void deleteGroup(int id) {
        gruppoDAO.deleteGruppo(id);
    }
}
