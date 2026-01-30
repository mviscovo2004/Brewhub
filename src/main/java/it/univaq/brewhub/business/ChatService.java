package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Gruppo;
import it.univaq.brewhub.model.Messaggio;
import it.univaq.brewhub.dao.GruppoDAO;
import it.univaq.brewhub.dao.MessaggioDAO;
import it.univaq.brewhub.dao.impl.GruppoDAOImpl;
import it.univaq.brewhub.dao.impl.MessaggioDAOImpl;

import java.sql.SQLException;
import java.util.List;

/**
 * Service Layer dedicato alla gestione della messaggistica e chat.
 * Gestisce sia i messaggi privati tra utenti che le chat di gruppo.
 * Implementa il pattern Singleton.
 */
public class ChatService {

    private static ChatService instance;
    private final MessaggioDAO messaggioDAO;
    private final GruppoDAO gruppoDAO;

    /**
     * Costruttore privato che inizializza i DAO necessari.
     */
    private ChatService() {
        this.messaggioDAO = new MessaggioDAOImpl();
        this.gruppoDAO = new GruppoDAOImpl();
    }

    /**
     * Restituisce l'istanza singleton di ChatService.
     *
     * @return L'istanza unica del servizio.
     */
    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    // ==================== SEZIONE MESSAGGI ====================

    /**
     * Invia un nuovo messaggio, sia privato che in un gruppo.
     *
     * @param messaggio Il messaggio da inviare.
     * @throws BusinessException Se il contenuto è vuoto, manca il mittente o il
     *                           destinatario.
     */
    public void sendMessage(Messaggio messaggio) throws BusinessException {
        if (messaggio == null || messaggio.getContenuto() == null || messaggio.getContenuto().isBlank()) {
            throw new BusinessException("Il testo del messaggio è obbligatorio");
        }
        if (messaggio.getSender() == null) {
            throw new BusinessException("Il mittente del messaggio è obbligatorio");
        }
        // Deve esserci almeno un destinatario (chat 1-to-1) o un ID gruppo (chat di
        // gruppo)
        if (messaggio.getReceiver() == null && messaggio.getIdGruppo() == null) {
            throw new BusinessException("Specificare un destinatario o un gruppo di destinazione");
        }

        try {
            messaggioDAO.create(messaggio);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore durante l'invio del messaggio", e);
            throw new BusinessException("Impossibile inviare il messaggio", e);
        }
    }

    /**
     * Recupera lo storico dei messaggi scambiati privatamente tra due utenti.
     *
     * @param user1 Username del primo utente.
     * @param user2 Username del secondo utente.
     * @return Una lista di messaggi ordinati cronologicamente.
     */
    public List<Messaggio> getPrivateMessages(String user1, String user2) {
        try {
            return messaggioDAO.getConversazione(user1, user2);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore recupero messaggi privati tra " + user1 + " e " + user2, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Recupera tutti i messaggi inviati all'interno di un gruppo specifico.
     *
     * @param groupId L'ID del gruppo.
     * @return Una lista di messaggi del gruppo.
     */
    public List<Messaggio> getGroupMessages(int groupId) {
        try {
            return messaggioDAO.getMessaggiGruppo(groupId);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore recupero messaggi gruppo " + groupId, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Ottiene la lista degli username con cui l'utente specificato ha conversazioni
     * attive.
     *
     * @param username L'username dell'utente.
     * @return Una lista di username.
     */
    public List<String> getActiveConversations(String username) {
        try {
            return messaggioDAO.getUtentiConversazioni(username);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore recupero conversazioni attive per " + username, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Contrassegna un messaggio come "letto".
     *
     * @param messageId L'ID del messaggio.
     */
    public void markAsRead(int messageId) {
        try {
            messaggioDAO.segnaComeLetto(messageId);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore durante la marcatura del messaggio come letto: " + messageId,
                    e);
        }
    }

    /**
     * Calcola il numero totale di messaggi non letti per un determinato utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di messaggi non letti.
     */
    public int getUnreadCount(String username) {
        try {
            return messaggioDAO.contaNonLetti(username);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore conteggio messaggi non letti per " + username, e);
            return 0;
        }
    }

    /**
     * Elimina l'intera conversazione privata tra due utenti.
     *
     * @param user1 Username del primo utente.
     * @param user2 Username del secondo utente.
     */
    public void deleteConversation(String user1, String user2) {
        try {
            messaggioDAO.deleteConversazione(user1, user2);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore eliminazione conversazione tra " + user1 + " e " + user2, e);
        }
    }

    // ==================== SEZIONE GRUPPI ====================

    /**
     * Crea un nuovo gruppo di chat.
     *
     * @param nome     Il nome del gruppo.
     * @param creatore L'username dell'utente che crea il gruppo.
     * @param membri   Una lista iniziale di username da aggiungere al gruppo.
     * @return L'ID del nuovo gruppo creato.
     * @throws BusinessException Se il nome del gruppo o il creatore non sono
     *                           validi.
     */
    public int createGroup(String nome, String creatore, List<String> membri) throws BusinessException {
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("Il nome del gruppo è obbligatorio");
        }
        if (creatore == null) {
            throw new BusinessException("Il creatore del gruppo è obbligatorio");
        }
        try {
            return gruppoDAO.createGruppo(nome, creatore, membri);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore durante la creazione del gruppo", e);
            throw new BusinessException("Impossibile creare il gruppo", e);
        }
    }

    /**
     * Recupera tutti i gruppi a cui partecipa un determinato utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di gruppi.
     */
    public List<Gruppo> getUserGroups(String username) {
        try {
            return gruppoDAO.getGruppiUtente(username);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore recupero gruppi per " + username, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Recupera le informazioni di un gruppo tramite il suo ID.
     *
     * @param id L'ID del gruppo.
     * @return L'oggetto Gruppo, o null se non trovato.
     */
    public Gruppo getGroupById(int id) {
        try {
            return gruppoDAO.getGruppo(id);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore recupero gruppo con ID: " + id, e);
            return null;
        }
    }

    /**
     * Aggiunge un nuovo partecipante a un gruppo esistente.
     *
     * @param groupId  L'ID del gruppo.
     * @param username L'username dell'utente da aggiungere.
     */
    public void addGroupMember(int groupId, String username) {
        try {
            gruppoDAO.addMembro(groupId, username);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore aggiunta membro " + username + " al gruppo " + groupId, e);
        }
    }

    /**
     * Rimuove un partecipante da un gruppo.
     *
     * @param groupId  L'ID del gruppo.
     * @param username L'username dell'utente da rimuovere.
     */
    public void removeGroupMember(int groupId, String username) {
        try {
            gruppoDAO.removeMembro(groupId, username);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore rimozione membro " + username + " dal gruppo " + groupId, e);
        }
    }

    /**
     * Modifica il nome di un gruppo esistente.
     *
     * @param groupId L'ID del gruppo.
     * @param newName Il nuovo nome da assegnare.
     * @throws BusinessException Se il nuovo nome è vuoto.
     */
    public void renameGroup(int groupId, String newName) throws BusinessException {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException("Il nome del gruppo non può essere vuoto");
        }
        try {
            gruppoDAO.renameGruppo(groupId, newName);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore rinomina gruppo " + groupId, e);
            throw new BusinessException("Impossibile rinominare il gruppo", e);
        }
    }

    /**
     * Elimina definitivamente un gruppo.
     *
     * @param id L'ID del gruppo da eliminare.
     */
    public void deleteGroup(int id) {
        try {
            gruppoDAO.deleteGruppo(id);
        } catch (SQLException e) {
            it.univaq.brewhub.utility.Log.error("Errore eliminazione gruppo " + id, e);
        }
    }
}
