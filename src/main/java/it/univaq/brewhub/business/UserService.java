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
 * Service Layer per la gestione centralizzata degli Utenti.
 * Gestisce autenticazione (Login), registrazione (Sign-up), gestione profilo
 * e relazioni sociali (Follow System).
 * Implementa il pattern Singleton.
 */
public class UserService {

    private static UserService instance;
    private final UtenteDAO utenteDAO;
    private final it.univaq.brewhub.dao.TorrefattoreDAO torrefattoreDAO;

    /**
     * Costruttore privato.
     */
    private UserService() {
        this.utenteDAO = new UtenteDAOImpl();
        this.torrefattoreDAO = new it.univaq.brewhub.dao.impl.TorrefattoreDAOImpl();
    }

    /**
     * Restituisce l'istanza singleton di UserService.
     *
     * @return L'istanza unica del servizio.
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Effettua il login verificando le credenziali username e password.
     *
     * @param username L'username fornito.
     * @param password La password fornita (in chiaro).
     * @return L'oggetto Utente se l'autenticazione ha successo, altrimenti null.
     * @throws BusinessException Se username/password sono nulli o errore tecnico.
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
     * Registra un nuovo utente standard nel sistema.
     *
     * @param utente L'oggetto Utente da registrare.
     * @throws BusinessException Se l'utente è nullo, l'username esiste già o errore
     *                           tecnico.
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
     * Registra un nuovo Torrefattore nel sistema.
     *
     * @param t L'oggetto Torrefattore (che estende Utente) con i dettagli
     *          aziendali.
     * @throws BusinessException Se username o P.IVA sono duplicati o errore
     *                           tecnico.
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
            Log.error("Errore durante la registrazione del torrefattore", e);
            throw new BusinessException("Errore durante la registrazione", e);
        }
    }

    /**
     * Cerca utenti il cui username contiene la stringa specificata.
     *
     * @param query La stringa di ricerca.
     * @return Una lista di utenti corrispondenti.
     */
    public List<Utente> searchUsers(String query) {
        try {
            return utenteDAO.searchByUsername(query);
        } catch (SQLException e) {
            Log.error("Errore durante la ricerca utenti", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera i post salvati nell'archivio di un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di post salvati.
     */
    public List<Post> getSavedPosts(String username) {
        try {
            return utenteDAO.getArchive(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei post salvati", e);
            return Collections.emptyList();
        }
    }

    /**
     * Conta il numero di post salvati da un utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di post salvati.
     */
    public int getSavedPostsCount(String username) {
        try {
            return utenteDAO.getNumSavedPosts(username);
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei post salvati", e);
            return 0;
        }
    }

    /**
     * Elimina definitivamente un utente dal sistema.
     *
     * @param username L'username dell'utente da rimuovere.
     * @throws BusinessException Se si verifica un errore.
     */
    public void deleteUser(String username) throws BusinessException {
        try {
            utenteDAO.delete(username);
        } catch (SQLException e) {
            Log.error("Errore durante l'eliminazione dell'utente", e);
            throw new BusinessException("Impossibile eliminare l'utente", e);
        }
    }

    /**
     * Restituisce il numero totale di utenti registrati.
     *
     * @return Il conteggio totale degli utenti.
     */
    public int getTotalUsersCount() {
        try {
            return utenteDAO.countAll();
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio degli utenti", e);
            return 0;
        }
    }

    /**
     * Recupera la lista degli utenti più attivi (top N).
     *
     * @param limit Il numero massimo di utenti da restituire.
     * @return Una lista degli utenti più attivi.
     */
    public List<Utente> getTopActiveUsers(int limit) {
        try {
            return utenteDAO.findTopActiveUsers(limit);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei top utenti", e);
            return Collections.emptyList();
        }

    }

    /**
     * Aggiorna le informazioni di un utente esistente.
     *
     * @param utente L'oggetto Utente con i dati aggiornati.
     * @throws BusinessException Se si verifica un errore.
     */
    public void updateUser(Utente utente) throws BusinessException {
        try {
            utenteDAO.update(utente);
        } catch (SQLException e) {
            Log.error("Errore durante l'aggiornamento dell'utente", e);
            throw new BusinessException("Impossibile aggiornare il profilo", e);
        }
    }

    /**
     * Recupera i dettagli estesi di un torrefattore tramite username.
     *
     * @param username L'username del torrefattore.
     * @return L'oggetto Torrefattore o null.
     */
    public it.univaq.brewhub.model.Torrefattore getTorrefattoreDetails(String username) {
        try {
            return torrefattoreDAO.findByUsername(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei dettagli torrefattore", e);
            return null;
        }
    }

    /**
     * Aggiorna i dettagli specifici di un torrefattore.
     *
     * @param torrefattore L'oggetto Torrefattore aggiornato.
     * @throws BusinessException Se si verifica un errore.
     */
    public void updateTorrefattore(it.univaq.brewhub.model.Torrefattore torrefattore) throws BusinessException {
        try {
            torrefattoreDAO.update(torrefattore);
        } catch (SQLException e) {
            Log.error("Errore durante l'aggiornamento del torrefattore", e);
            throw new BusinessException("Impossibile aggiornare i dettagli del torrefattore", e);
        }
    }

    /**
     * Aggiunge un post all'archivio salvato dell'utente.
     *
     * @param username L'username dell'utente.
     * @param postId   L'ID del post da salvare.
     * @throws BusinessException Se si verifica un errore.
     */
    public void addToArchive(String username, int postId) throws BusinessException {
        try {
            utenteDAO.addToArchive(username, postId);
        } catch (SQLException e) {
            Log.error("Errore durante l'aggiunta all'archivio", e);
            throw new BusinessException("Impossibile salvare il post", e);
        }
    }

    /**
     * Rimuove un post dall'archivio salvato dell'utente.
     *
     * @param username L'username dell'utente.
     * @param postId   L'ID del post da rimuovere.
     * @throws BusinessException Se si verifica un errore.
     */
    public void removeFromArchive(String username, int postId) throws BusinessException {
        try {
            utenteDAO.removeFromArchive(username, postId);
        } catch (SQLException e) {
            Log.error("Errore durante la rimozione dall'archivio", e);
            throw new BusinessException("Impossibile rimuovere il post", e);
        }
    }

    /**
     * Verifica se un post è nell'archivio di un utente.
     *
     * @param username L'username dell'utente.
     * @param postId   L'ID del post.
     * @return true se il post è archiviato, false altrimenti.
     */
    public boolean isArchived(String username, int postId) {
        try {
            return utenteDAO.isArchived(username, postId);
        } catch (SQLException e) {
            Log.error("Errore durante la verifica dell'archivio", e);
            return false;
        }
    }

    /**
     * Segue un utente (Follow).
     *
     * @param follower L'username di chi vuole seguire.
     * @param followed L'username dell'utente da seguire.
     * @throws BusinessException Se si verifica un errore.
     */
    public void follow(String follower, String followed) throws BusinessException {
        try {
            utenteDAO.follow(follower, followed);
        } catch (SQLException e) {
            Log.error("Errore durante l'operazione di follow", e);
            throw new BusinessException("Impossibile seguire l'utente", e);
        }
    }

    /**
     * Smette di seguire un utente (Unfollow).
     *
     * @param follower L'username di chi smette di seguire.
     * @param followed L'username dell'utente che veniva seguito.
     * @throws BusinessException Se si verifica un errore.
     */
    public void unfollow(String follower, String followed) throws BusinessException {
        try {
            utenteDAO.unfollow(follower, followed);
        } catch (SQLException e) {
            Log.error("Errore durante l'operazione di unfollow", e);
            throw new BusinessException("Impossibile smettere di seguire l'utente", e);
        }
    }

    /**
     * Verifica la relazione di follow tra due utenti.
     *
     * @param follower Username dell'utente potenziale follower.
     * @param followed Username dell'utente potenziale seguito.
     * @return true se "follower" segue "followed".
     */
    public boolean isFollowing(String follower, String followed) {
        try {
            return utenteDAO.isFollowing(follower, followed);
        } catch (SQLException e) {
            Log.error("Errore durante la verifica del follow", e);
            return false;
        }
    }

    /**
     * Conta i follower di un utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di follower.
     */
    public int getFollowersCount(String username) {
        try {
            return utenteDAO.getFollowersCount(username);
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei followers", e);
            return 0;
        }
    }

    /**
     * Conta le persone seguite (following) da un utente.
     *
     * @param username L'username dell'utente.
     * @return Il numero di following.
     */
    public int getFollowingCount(String username) {
        try {
            return utenteDAO.getFollowingCount(username);
        } catch (SQLException e) {
            Log.error("Errore durante il conteggio dei following", e);
            return 0;
        }
    }

    /**
     * Recupera la lista dei follower di un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di Utenti (follower).
     */
    public List<Utente> getFollowers(String username) {
        try {
            return utenteDAO.getFollowers(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei followers", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera la lista delle persone seguite da un utente.
     *
     * @param username L'username dell'utente.
     * @return Una lista di Utenti (following).
     */
    public List<Utente> getFollowing(String username) {
        try {
            return utenteDAO.getFollowing(username);
        } catch (SQLException e) {
            Log.error("Errore durante il recupero dei following", e);
            return Collections.emptyList();
        }
    }
}
