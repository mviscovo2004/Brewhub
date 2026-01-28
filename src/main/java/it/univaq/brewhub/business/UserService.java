package it.univaq.brewhub.business;

import it.univaq.brewhub.model.Utente;
import it.univaq.brewhub.model.Post;
import it.univaq.brewhub.dao.UtenteDAO;
import it.univaq.brewhub.dao.impl.UtenteDAOImpl;
import it.univaq.brewhub.utility.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service Layer per la gestione degli Utenti.
 * <p>
 * Agisce come intermediario tra la UI (Controller/View) e il livello di
 * persistenza (DAO).
 * Gestisce la logica di business relativa agli utenti, come login,
 * registrazione e ricerca.
 * Implementa il pattern Singleton.
 * </p>
 */
public class UserService {

    private static UserService instance;
    private final UtenteDAO utenteDAO;
    private final it.univaq.brewhub.dao.TorrefattoreDAO torrefattoreDAO;

    private UserService() {
        this.utenteDAO = new UtenteDAOImpl();
        this.torrefattoreDAO = new it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Tenta il login di un utente.
     * 
     * @param username Username.
     * @param password Password in chiaro.
     * @return L'oggetto Utente se le credenziali sono valide, altrimenti null.
     * @throws BusinessException Se si verifica un errore tecnico.
     */
    public Utente login(String username, String password) throws BusinessException {
        if (username == null || password == null) {
            throw new BusinessException("Username e password non possono essere nulli");
        }
        try {
            return utenteDAO.login(username, password);
        } catch (SQLException e) {
            Log.error("Errore durante il login", e);
            throw new BusinessException("Errore di sistema durante il login", e);
        }
    }

    /**
     * Registra un nuovo utente.
     * 
     * @param utente L'utente da registrare.
     * @throws BusinessException Se l'utente esiste già o errore tecnico.
     */
    public void registerUser(Utente utente) throws BusinessException {
        if (utente == null) {
            throw new BusinessException("L'utente non può essere nullo");
        }
        try {
            utenteDAO.create(utente);
        } catch (SQLException e) {
            if (e.getMessage().contains("Username esistente")) {
                throw new BusinessException("Username già in uso", e);
            }
            Log.error("Errore durante la registrazione", e);
            throw new BusinessException("Errore durante la registrazione", e);
        }
    }

    /**
     * Registra un nuovo torrefattore.
     * 
     * @param t Il torrefattore da registrare.
     * @throws BusinessException Se l'utente esiste già o errore tecnico.
     */
    public void registerTorrefattore(it.univaq.brewhub.model.Torrefattore t) throws BusinessException {
        if (t == null) {
            throw new BusinessException("Il torrefattore non può essere nullo");
        }
        try {
            torrefattoreDAO.create(t);
        } catch (SQLException e) {
            if (e.getMessage().contains("Username esistente") || e.getMessage().contains("UNIQUE constraint failed")) {
                throw new BusinessException("Username o Partita IVA già in uso", e);
            }
            Log.error("Errore registrazione torrefattore", e);
            throw new BusinessException("Errore durante la registrazione", e);
        }
    }

    /**
     * Cerca utenti per username parziale.
     * 
     * @param query Stringa di ricerca.
     * @return Lista di utenti trovati.
     */
    public List<Utente> searchUsers(String query) {
        try {
            return utenteDAO.searchByUsername(query);
        } catch (SQLException e) {
            Log.error("Errore ricerca utenti", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post salvati da un utente.
     * 
     * @param username Username dell'utente.
     * @return Lista di post salvati.
     */
    public List<Post> getSavedPosts(String username) {
        try {
            return utenteDAO.getArchive(username);
        } catch (SQLException e) {
            Log.error("Errore recupero post salvati", e);
            return Collections.emptyList();
        }
    }

    /**
     * Conta i post salvati da un utente.
     * 
     * @param username Username dell'utente.
     * @return Numero di post salvati.
     */
    public int getSavedPostsCount(String username) {
        try {
            return utenteDAO.getNumSavedPosts(username);
        } catch (SQLException e) {
            Log.error("Errore conteggio post salvati", e);
            return 0;
        }
    }

    /**
     * Elimina un utente.
     * 
     * @param username Username dell'utente da eliminare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteUser(String username) throws BusinessException {
        try {
            utenteDAO.delete(username);
        } catch (SQLException e) {
            Log.error("Errore eliminazione utente", e);
            throw new BusinessException("Impossibile eliminare l'utente", e);
        }
    }

    /**
     * Conta il totale degli utenti.
     * 
     * @return Numero totale di utenti.
     */
    public int getTotalUsersCount() {
        try {
            return utenteDAO.countAll();
        } catch (SQLException e) {
            Log.error("Errore conteggio utenti", e);
            return 0;
        }
    }

    /**
     * Recupera i top utenti attivi.
     * 
     * @param limit Numero massimo di utenti da recuperare.
     * @return Lista di utenti più attivi.
     */
    public List<Utente> getTopActiveUsers(int limit) {
        try {
            return utenteDAO.findTopActiveUsers(limit);
        } catch (SQLException e) {
            Log.error("Errore recupero top utenti", e);
            return Collections.emptyList();
        }

    }

    /**
     * Aggiorna i dati di un utente.
     * 
     * @param utente L'utente da aggiornare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void updateUser(Utente utente) throws BusinessException {
        try {
            utenteDAO.update(utente);
        } catch (SQLException e) {
            Log.error("Errore aggiornamento utente", e);
            throw new BusinessException("Impossibile aggiornare il profilo", e);
        }
    }

    /**
     * Recupera i dettagli specifici di un torrefattore.
     * 
     * @param username Username del torrefattore.
     * @return Dettagli del torrefattore o null.
     */
    public it.univaq.brewhub.model.Torrefattore getTorrefattoreDetails(String username) {
        try {
            return torrefattoreDAO.findByUsername(username);
        } catch (SQLException e) {
            Log.error("Errore recupero dettagli torrefattore", e);
            return null;
        }
    }

    /**
     * Aggiorna i dettagli di un torrefattore.
     * 
     * @param torrefattore Dettagli aggiornati.
     * @throws BusinessException Se si verifica un errore.
     */
    public void updateTorrefattore(it.univaq.brewhub.model.Torrefattore torrefattore) throws BusinessException {
        try {
            torrefattoreDAO.update(torrefattore);
        } catch (SQLException e) {
            Log.error("Errore aggiornamento torrefattore", e);
            throw new BusinessException("Impossibile aggiornare i dettagli del torrefattore", e);
        }
    }

    public void addToArchive(String username, int postId) throws BusinessException {
        try {
            utenteDAO.addToArchive(username, postId);
        } catch (SQLException e) {
            Log.error("Errore aggiunta archivio", e);
            throw new BusinessException("Impossibile salvare il post", e);
        }
    }

    public void removeFromArchive(String username, int postId) throws BusinessException {
        try {
            utenteDAO.removeFromArchive(username, postId);
        } catch (SQLException e) {
            Log.error("Errore rimozione archivio", e);
            throw new BusinessException("Impossibile rimuovere il post", e);
        }
    }

    public boolean isArchived(String username, int postId) {
        try {
            return utenteDAO.isArchived(username, postId);
        } catch (SQLException e) {
            Log.error("Errore verifica archivio", e);
            return false;
        }
    }

    public void follow(String follower, String followed) throws BusinessException {
        try {
            utenteDAO.follow(follower, followed);
        } catch (SQLException e) {
            Log.error("Errore follow", e);
            throw new BusinessException("Impossibile seguire l'utente", e);
        }
    }

    public void unfollow(String follower, String followed) throws BusinessException {
        try {
            utenteDAO.unfollow(follower, followed);
        } catch (SQLException e) {
            Log.error("Errore unfollow", e);
            throw new BusinessException("Impossibile smettere di seguire l'utente", e);
        }
    }

    public boolean isFollowing(String follower, String followed) {
        try {
            return utenteDAO.isFollowing(follower, followed);
        } catch (SQLException e) {
            Log.error("Errore check following", e);
            return false;
        }
    }

    public int getFollowersCount(String username) {
        try {
            return utenteDAO.getFollowersCount(username);
        } catch (SQLException e) {
            Log.error("Errore conteggio followers", e);
            return 0;
        }
    }

    public int getFollowingCount(String username) {
        try {
            return utenteDAO.getFollowingCount(username);
        } catch (SQLException e) {
            Log.error("Errore conteggio following", e);
            return 0;
        }
    }

    public List<Utente> getFollowers(String username) {
        try {
            return utenteDAO.getFollowers(username);
        } catch (SQLException e) {
            Log.error("Errore recupero followers", e);
            return Collections.emptyList();
        }
    }

    public List<Utente> getFollowing(String username) {
        try {
            return utenteDAO.getFollowing(username);
        } catch (SQLException e) {
            Log.error("Errore recupero following", e);
            return Collections.emptyList();
        }
    }
}
